package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.stercomm.customers.rbs.sir.rest.domain.FileSearchResult;
import com.stercomm.customers.rbs.sir.rest.domain.FileSearchResults;
import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;
import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResults;
import com.stercomm.customers.rbs.sir.rest.error.Error;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchResultBuilder;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchWhereClauseBuilder;
import com.stercomm.customers.rbs.sir.rest.util.TransactionResultType;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;
import com.sterlingcommerce.woodstock.util.frame.jdbc.JDBCService;

@Path("/files")
public class FilesRestServer extends TransactionHandlingRestServer {

	private static Logger LOGGER = Logger.getLogger(FilesRestServer.class.getName());


	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.filesearch.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);
			
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForFiles(@Context UriInfo uriInfo) {
		
	
		
		String poolName=Manager.getProperties("bfgui").getProperty("file.search.pool");
		
		LOGGER.info("Pool name : " + poolName);

		Connection conn = null;
		PreparedStatement ps = null;
		

		// default num rows to return on each request
		final List<String> rowsToReturn = new ArrayList<String>();
		rowsToReturn.add("10");

		// unless a param is specified, we start from 1 each time
		final List<String> offset = new ArrayList<String>();
		offset.add("0");

		// add those if they arent there ...
		uriInfo.getQueryParameters().putIfAbsent("start", offset);
		uriInfo.getQueryParameters().putIfAbsent("rows", rowsToReturn);

		// create the pagination string...
		String pagination = getPaginationString(uriInfo.getQueryParameters());

		// now remove the pagination params so we don't try to use them in the WHERE
		uriInfo.getQueryParameters().remove("start");
		uriInfo.getQueryParameters().remove("rows");

		// now construct the queries
		StringBuffer dataQuery = new StringBuffer();
		StringBuffer totalQuery = new StringBuffer();
		
		totalQuery.append("select count(*) from sct_bundle ");

		dataQuery.append(
				"SELECT bundle_id, filename, reference, btimestamp, btype, entity_id, status, error, wf_id, message_id, "
						+ "isoutbound, isoverride, service, doc_id, "
						// this next part totals the transactions (PAYMENT AND PAYMENT_ARCHIVE rows for
						// each bundle
						+ "(select count(*) from SCT_PAYMENT p1, SCT_BUNDLE b1 where p1.bundle_id = b1.bundle_id and "
						+ "b1.bundle_id=bun.bundle_id)+"
						+ "(select count(*) from SCT_PAYMENT_ARCHIVE p2, SCT_BUNDLE b2 where p2.bundle_id = b2.bundle_id "
						+ "and b2.bundle_id=bun.bundle_id) " + "FROM SCT_BUNDLE bun ");

		// are there any query params, if so create a WHERE?
		
		String s = "(isoutbound=0 or isoutbound = 1) ";
		
		if (uriInfo.getQueryParameters().keySet().size() > 0) {
		 String where = getWhereFromParams(uriInfo.getQueryParameters());
			dataQuery.append(where);
			totalQuery.append(where);
			
			if (!uriInfo.getQueryParameters().containsKey("outbound")) {
				// current systems screens not 0 or 1 values, so do we.
				
				dataQuery.append(" and ").append(s);
				totalQuery.append(" and ").append(s);
			}
		}else {
			
			// we always want to screen even if no QS
			dataQuery.append(" where ").append(s);
			totalQuery.append(" where ").append(s);
			
		}

		// need to order by something
		String orderBy = " ORDER BY btimestamp DESC, BUNDLE_ID DESC";
		dataQuery.append(orderBy);

		// append the pagination we worked out earlier
		dataQuery.append(pagination);

		String dataQueryAsString = dataQuery.toString();
		String totalQueryAsString = totalQuery.toString();
		//to get the total
		
		LOGGER.info("Data query:"+ dataQueryAsString);
		LOGGER.info("Total query:"+ totalQueryAsString);
		

		// where we put results
		
		FileSearchResults result=new FileSearchResults();
		
		ResultSet dataRs=null;
		ResultSet totalRs=null;
		try {
			 if (null==poolName) {
				 conn = Conn.getConnection();
			 }
			 else {
				 conn = JDBCService.getConnection(poolName);
			 }
			ps = conn.prepareStatement(dataQueryAsString);
			dataRs = ps.executeQuery();
			List<FileSearchResult> results = new ArrayList<FileSearchResult>();
			
			while (dataRs.next()) {
				FileSearchResult res = toFileSearchResult(dataRs);
				results.add(res);
			}
			
			//if we had some results add them to the overall obj we are going to return
			if(!results.isEmpty()) {
				
				result.setResults(results);
			}
			// now let's get the total
			ps.close();
			ps=conn.prepareStatement(totalQueryAsString);			
			totalRs = ps.executeQuery();

			while (totalRs.next()) {
				int k = totalRs.getInt(1);
				LOGGER.info("Result of count : " + k);
				result.setTotal(k);
			}
		}

		catch (Exception e) {
			LOGGER.severe("SQL Error searching the SCT_BUNDLE table : " + e.getMessage());

		} finally {
			try {
				if (dataRs != null) {
					dataRs.close();
				}
				if (totalRs != null) {
					totalRs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					Conn.freeConnection(conn);
				}
			} catch (SQLException se) {
				LOGGER.severe("SQL exception : " + se.getMessage());
			}
		}

		return Response.status(Status.OK).entity(result).build();
	}

	@GET
	@Path("/{fileid}/transactions/{transactionid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForTransactionByID(@PathParam("fileid") String bundleid,
			@PathParam("transactionid") String transID, @Context UriInfo uriInfo) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String poolName=Manager.getProperties("bfgui").getProperty("file.search.pool");

		boolean is404 = false;

		TransactionSearchResult result = null;
		
		final List<String> rowsToReturn = new ArrayList<String>();
		rowsToReturn.add("10");

		// unless a param is specified, we start from 1 each time
		final List<String> offset = new ArrayList<String>();
		offset.add("0");

		// add those if they arent there ...
		uriInfo.getQueryParameters().putIfAbsent("start", offset);
		uriInfo.getQueryParameters().putIfAbsent("rows", rowsToReturn);

		// create the pagination string...
		String pagination = getPaginationString(uriInfo.getQueryParameters());

		// now remove the pagination params so we don't try to use them in the WHERE
		uriInfo.getQueryParameters().remove("start");
		uriInfo.getQueryParameters().remove("rows");


		// now construct the query
		StringBuffer query = new StringBuffer();

		// select p.payment_id, p.payment_bic, b.entity, b.filename, p.reference,
		// p.TRANSACTION_ID, p.type, p.isoutbound,
		// p.ptimestamp, b.wf_id, p.SETTLE_DATE, p.SETTLE_AMT, p.status FROM SCP_PAYMENT
		// p, SCT_BUNDLE b
		// where p.payment_id = ? and p.bundle_id = b.bundle_id

		query.append(
				"select p.payment_id, p.TRANSACTION_ID, p.SETTLE_DATE, p.SETTLE_AMT,  p.type, "
				+ "p.wf_id, p.status, p.bundle_id, p.doc_id, p.ptimestamp, p.isoutbound, p.payment_bic, e.entity, b.filename,  p.reference,  e.service "
				+ "FROM (select * from SCT_PAYMENT UNION select * from SCT_PAYMENT_ARCHIVE) p, SCT_BUNDLE b, SCT_ENTITY e "
				+ "where p.payment_id = ? and b.bundle_id = ? and p.bundle_id = b.bundle_id and e.entity_id = b.entity_id ");

		
		// append the pagination we worked out earlier
		query.append(pagination);
		String fullQuery = query.toString();
		LOGGER.info("Query on pool "+ poolName + " for single bundle/trans (" + bundleid + ","+transID +"): " + fullQuery);

		try {
			 if (null==poolName) {
				 conn = Conn.getConnection();
			 }
			 else {
				 conn = JDBCService.getConnection(poolName);
			 }
			 
			ps = conn.prepareStatement(fullQuery);
			ps.setInt(1, Integer.parseInt(transID));
			ps.setInt(2, Integer.parseInt(bundleid));
			
			rs = ps.executeQuery();		
			
			if (rs.next()) {
				
				result = toTransactionSearchResult(rs, TransactionResultType.DETAIL);
			}
			else {
				LOGGER.info("ResultSet is empty in Java");
				is404 = true;
				
			}

		}

		catch (Exception e) {
			LOGGER.severe("SQL Error searching the SCT PAYMENT table : " + e.getMessage());

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					Conn.freeConnection(conn);
				}
			} catch (SQLException se) {
				LOGGER.severe("SQL exception : " + se.getMessage());
			}
		}

		Status s = (!is404 ? Status.OK : Status.NOT_FOUND);

		if (s == Status.OK) {

			return Response.status(s).entity(result).build();
		} else {
			Error e = new Error();
			e.setAttribute("payment id,bundleid");
			e.setMessage("Payment id or bundle id not found");

			return Response.status(s).entity(e).build();
		}

	}

	@GET
	@Path("/{fileid}/transactions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForTransactions(@PathParam("fileid") String bundleid, @Context UriInfo uriInfo) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSet totalRs = null;

		
		String poolName=Manager.getProperties("bfgui").getProperty("file.search.pool");
		// default num rows to return on each request
		final List<String> rowsToReturn = new ArrayList<String>();
		rowsToReturn.add("10");

		// unless a param is specified, we start from 1 each time
		final List<String> offset = new ArrayList<String>();
		offset.add("0");

		// add those if they arent there ...
		uriInfo.getQueryParameters().putIfAbsent("start", offset);
		uriInfo.getQueryParameters().putIfAbsent("rows", rowsToReturn);

		// create the pagination string...
		String pagination = getPaginationString(uriInfo.getQueryParameters());

		// now remove the pagination params so we don't try to use them in the WHERE
		uriInfo.getQueryParameters().remove("start");
		uriInfo.getQueryParameters().remove("rows");

		// now construct the query
		StringBuffer dataQuery = new StringBuffer();
		dataQuery.append("SELECT payment_id, transaction_id, settle_date, settle_amt, type,  "
				+ "wf_id, status, bundle_id, doc_id, ptimestamp, isoutbound from (select * from SCT_PAYMENT UNION select * from SCT_PAYMENT_ARCHIVE) "
				+ "WHERE BUNDLE_ID = ? ORDER BY PAYMENT_ID DESC ");

		// append the pagination we worked out earlier
		dataQuery.append(pagination);
		// where we put results
		TransactionSearchResults results = new TransactionSearchResults();

		String fullQuery = dataQuery.toString();
		LOGGER.info("Query : " + fullQuery);
		
		// and for the total
		StringBuffer totalQuery = new StringBuffer();
		totalQuery.append("select count(*) from ");
		totalQuery.append("(select * from SCT_PAYMENT UNION select * from SCT_PAYMENT_ARCHIVE) ");
		totalQuery.append("WHERE BUNDLE_ID = ?");

		List<TransactionSearchResult> list=new ArrayList<TransactionSearchResult>();
		try {
			 if (null==poolName) {
				 conn = Conn.getConnection();
			 }
			 else {
				 conn = JDBCService.getConnection(poolName);
			 }
			ps = conn.prepareStatement(fullQuery);
			ps.setString(1, bundleid);
			rs = ps.executeQuery();

			while (rs.next()) {
				TransactionSearchResult result = toTransactionSearchResult(rs, TransactionResultType.SUMMARY);
				list.add(result);
			}
			results.setResults(list);
			// now let's get the total
			ps.close();
			ps=conn.prepareStatement(totalQuery.toString());		
			ps.setString(1, bundleid);
			totalRs = ps.executeQuery();

			while (totalRs.next()) {
					int k = totalRs.getInt(1);
					LOGGER.info("Result of count : " + k);
					results.setTotal(k);
			}
			
			
		}

		catch (Exception e) {
			LOGGER.severe("SQL Error searching the SCT PAYMENT & ARCHIVE tables : " + e.getMessage());

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					Conn.freeConnection(conn);
				}
			} catch (SQLException se) {
				LOGGER.severe("SQL exception : " + se.getMessage());
			}
		}

		return Response.status(Status.OK).entity(results).build();

	}

	/**
	 * Create a FileSearchResult object from a Row
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private FileSearchResult toFileSearchResult(ResultSet row) throws SQLException {

		int bundleID = row.getInt(1);
		String fname = row.getString(2);
		String ref = row.getString(3);
		long ts = row.getTimestamp(4).getTime();
		String type = row.getString(5);
		int eID = row.getInt(6);
		int status = row.getInt(7);
		String errorCode = row.getString(8);
		int wfID = row.getInt(9);
		long messageID = row.getLong(10);
		int isOutbound = row.getInt(11);
		int isOverride = row.getInt(12);
		String service = row.getString(13);
		String docID = row.getString(14);
		int total = row.getInt(15);

		boolean bOutbound = (isOutbound == 0) ? false : true;
		boolean bOverride = (isOverride == 0) ? false : true;

		String formattedTimeStamp = df.format(new java.util.Date(ts));

		FileSearchResult result = new FileSearchResultBuilder(bundleID).withErrorCode(errorCode)
				.withTimestamp(formattedTimeStamp).withReference(ref).withType(type).withEntityID(eID)
				.withService(service).withFilename(fname).withWorkflowID(wfID).withStatus(status)
				.withMessageID(messageID).withOutbound(bOutbound).withOverride(bOverride).withDocID(docID)
				.withTransactionTotal(total).build();

		return result;

	}

	

	private String getWhereFromParams(MultivaluedMap<String, String> qsparams) {

		FileSearchWhereClauseBuilder builder = new FileSearchWhereClauseBuilder();

		int numOfParams = qsparams.keySet().size();
		int numElementsSoFar = 0;

		String s = qsparams.getFirst("filename");

		if (s != null) {
			builder.withFilename(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("errorcode");

		if (s != null) {
			builder.withErrorCode(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}
		
		s = qsparams.getFirst("id");

		if (s != null) {
			int k=Integer.parseInt(s);
			builder.withBundleID(k);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}


		s = qsparams.getFirst("service");

		if (s != null) {
			builder.withService(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("reference");

		if (s != null) {
			builder.withReference(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("docid");

		if (s != null) {
			builder.withDocID(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("type");

		if (s != null) {
			builder.withType(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("outbound");

		if (s != null) {
			builder.withOutbound(Boolean.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("override");

		if (s != null) {
			builder.withOverride(Boolean.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}
		
		s= qsparams.getFirst("bp-state");
		
		
		if (s != null && s!="") {
			
			builder.withBPState(s);
				
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("messageid");

		if (s != null) {
			builder.withMessageID(Long.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("entityid");

		if (s != null) {
			builder.withEntityID(Long.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("from");

		if (s != null) {
			builder.after(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("to");

		if (s != null) {
			builder.before(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("status");

		if (s != null) {
			builder.withStatus(Integer.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("wfid");

		if (s != null) {
			builder.withWorkflowID(Integer.valueOf(s));
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}
		return builder.build();

	}

	private String getPaginationString(MultivaluedMap<String, String> par) {

		String off = par.getFirst("start");
		String rows = par.getFirst("rows");

		return " OFFSET " + off + " ROWS FETCH FIRST " + rows + " ROWS ONLY";
	}
}
