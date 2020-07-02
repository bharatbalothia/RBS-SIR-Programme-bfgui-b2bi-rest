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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;
import com.stercomm.customers.rbs.sir.rest.util.TransactionResultType;
import com.stercomm.customers.rbs.sir.rest.util.TransactionSearchWhereClauseBuilder;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;

@Path("/transactions")
public class TransactionsRestServer extends BaseRestServer {
	
	private static Logger LOGGER = Logger.getLogger(TransactionsRestServer.class.getName());
	
	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.transactions.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForTransactions(@Context UriInfo uriInfo) {

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

		query.append(
				"select p.payment_id, p.transaction_id,p.settle_date,p.settle_amt,p.type,p.status,p.wf_id, p.bundle_id ");
		query.append("from sct_payment p, sct_entity e, sct_bundle b ");
		query.append("where b.bundle_id=p.bundle_id and e.entity_id = b.entity_id ");

		// are there any query params, if so create a WHERE?
		if (uriInfo.getQueryParameters().keySet().size() > 0) {
			String where = getWhereFromParams(uriInfo.getQueryParameters());
			query.append(" and ");
			query.append(where);
		}

		// need to order by something
		String orderBy = " ORDER BY p.ptimestamp DESC, p.payment_id DESC";
		query.append(orderBy);

		// append the pagination we worked out earlier
		query.append(pagination);

		String fullQuery = query.toString();
		LOGGER.info("Query : " + fullQuery);

		// where we put results
		List<TransactionSearchResult> results = new ArrayList<TransactionSearchResult>();
		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(fullQuery);
			rs = ps.executeQuery();

			while (rs.next()) {
				TransactionSearchResult result = toTransactionSearchResult(rs, TransactionResultType.SUMMARY);
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

	
	private String getWhereFromParams(MultivaluedMap<String, String> qsparams) {
		
		
		

		TransactionSearchWhereClauseBuilder builder = new TransactionSearchWhereClauseBuilder();

		int numOfParams = qsparams.keySet().size();
		int numElementsSoFar = 0;

		
		
		String s = qsparams.getFirst("entity");

		if (s != null) {
			builder.withEntity(s);
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
		
		
		s = qsparams.getFirst("status");

		if (s != null) {
			builder.withStatus(Integer.valueOf(s));
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
		
		

		s = qsparams.getFirst("transactionid");

		if (s != null) {
			builder.withTransactionID(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}



		s = qsparams.getFirst("paymentbic");

		if (s != null) {
			builder.withPaymentBIC(s);
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

	
		s = qsparams.getFirst("settlementFrom");

		if (s != null) {
			builder.withSettlementAfter(s);
			numElementsSoFar++;
			if (numElementsSoFar < numOfParams) {
				builder.and();
			}
		}

		s = qsparams.getFirst("settlementTo");

		if (s != null) {
			builder.withSettlementBefore(s);
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
