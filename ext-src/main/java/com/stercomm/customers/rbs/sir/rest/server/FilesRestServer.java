package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchResultBuilder;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchWhereClauseBuilder;
import com.stercomm.customers.rbs.sir.rest.util.TransactionSearchResultBuilder;
import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;

@Path("/files")
public class FilesRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(FilesRestServer.class.getName());

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

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

		// now contstruct the query
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT bundle_id, filename, reference, btimestamp, btype, entity_id, status, error, wf_id, message_id, "
					+ "isoutbound, isoverride, service, doc_id, "
					+ "(select count(*) from SCT_PAYMENT p1, SCT_BUNDLE b1 where p1.bundle_id = b1.bundle_id and "
					+ "b1.bundle_id=bun.bundle_id)+"
					+ "(select count(*) from SCT_PAYMENT_ARCHIVE p2, SCT_BUNDLE b2 where p2.bundle_id = b2.bundle_id "
					+ "and b2.bundle_id=bun.bundle_id) "
					+ "FROM SCT_BUNDLE bun ");

		// are there any query params, if so create a WHERE?
		if (uriInfo.getQueryParameters().keySet().size() > 0) {
			String where = getWhereFromParams(uriInfo.getQueryParameters());
			query.append(where);
			if (!uriInfo.getQueryParameters().containsKey("outbound")) {
				// current systems screens not 0 or 1 values, so do we.
				query.append(" and (isoutbound=0 or isoutbound = 1) ");
			}
		}

		// need to order by something
		String orderBy = " ORDER BY BUNDLE_ID DESC";
		query.append(orderBy);

		// append the pagination we worked out earlier
		query.append(pagination);

		String fullQuery = query.toString();
		LOGGER.info("Query : " + fullQuery);

		// where we put results
		List<FileSearchResult> results = new ArrayList<FileSearchResult>();
		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(fullQuery);
			rs = ps.executeQuery();

			while (rs.next()) {
				FileSearchResult result = toFileSearchResult(rs);
				results.add(result);
			}
		}

		catch (Exception e) {
			LOGGER.severe("SQL Error searching the SCT_BUNDLE table : " + e.getMessage());

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

	@GET
	@Path("/{fileid}/transactions/{transactionid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForTransactionByID(@PathParam("fileid") String bundleid,
			@PathParam("transactionid") String transID, @Context UriInfo uriInfo) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		boolean is404=false;
		
		TransactionSearchResult result = null;

		// now construct the query
		StringBuffer query = new StringBuffer();
		
		//select p.payment_id, p.payment_bic, b.entity, b.filename, p.reference, p.TRANSACTION_ID, p.type, p.isoutbound, 
		// p.ptimestamp, b.wf_id, p.SETTLE_DATE, p.SETTLE_AMT, p.status  FROM SCP_PAYMENT p, SCT_BUNDLE b 
		//where p.payment_id = ?  and p.bundle_id = b.bundle_id
		
		query.append("select p.payment_id, p.payment_bic, b.entity, b.filename, p.reference, p.TRANSACTION_ID, p.type, p.isoutbound," + 
				"		 p.ptimestamp, b.wf_id, p.SETTLE_DATE, p.SETTLE_AMT, p.status  FROM SCP_PAYMENT p, SCT_BUNDLE b " + 
				"		where p.payment_id = ?  and b.bundle_id = ? and p.bundle_id = b.bundle_id ");

		String fullQuery = query.toString();
		LOGGER.info("Query : " + fullQuery);

		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(fullQuery);
			ps.setLong(1,  Long.parseLong(transID));
			ps.setLong(2, Long.parseLong(bundleid));
			rs = ps.executeQuery();

			if (rs.next() == false) {
				LOGGER.info("ResultSet is empty in Java");
				is404=true;
			} else {
				do {
					result = toTransactionResult(rs);
				} while (rs.next());
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

		Status s = (!is404?Status.OK:Status.NOT_FOUND);
		
		if (s==Status.OK) {
			
			return Response.status(s).entity(result).build();
		}
		else {
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
		StringBuffer query = new StringBuffer();
		query.append("SELECT payment_id, transaction_id, settle_date, settle_amt, type,  "
				+ "status, wf_id from (select * from SCT_PAYMENT UNION select * from SCT_PAYMENT_ARCHIVE) "
				+ "WHERE BUNDLE_ID = ? ORDER BY PAYMENT_ID DESC ");

		// append the pagination we worked out earlier
		query.append(pagination);
		// where we put results
		List<TransactionSearchResult> results = new ArrayList<TransactionSearchResult>();

		String fullQuery = query.toString();
		LOGGER.info("Query : " + fullQuery);

		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(fullQuery);
			ps.setString(1, bundleid);
			rs = ps.executeQuery();

			while (rs.next()) {
				TransactionSearchResult result = toTransactionResult(rs);
				results.add(result);
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

	/**
	 * Create a TransactionSearchResult object from a Row
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private TransactionSearchResult toTransactionResult(ResultSet row) throws SQLException {

		int paymentID = row.getInt(1);
		String transactionID = row.getString(2);
		long ts = row.getTimestamp(3).getTime();
		double settleAmt = row.getDouble(4);
		String type = row.getString(5);
		int status = row.getInt(6);
		int wfid = row.getInt(7);

		String formattedSettleDate = df.format(new java.util.Date(ts));

		TransactionSearchResult result = new TransactionSearchResultBuilder(paymentID).withTransactionID(transactionID)
				.withSettleAmount(settleAmt).withSettleDate(formattedSettleDate).withType(type).withStatus(status)
				.withWorkflowID(wfid).build();

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
