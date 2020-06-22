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
import java.util.Map;
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

import com.stercomm.customers.rbs.sir.rest.domain.FileSearchResult;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchResultBuilder;
import com.stercomm.customers.rbs.sir.rest.util.FileSearchWhereClauseBuilder;
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
		query.append(
				"SELECT bundle_id, filename, reference, btimestamp, btype, entity_id, status, error, wf_id, message_id, "
						+ "isoutbound, isoverride, service, doc_id FROM SCT_BUNDLE ");

		// are there any query params, if so create a WHERE?
		if (uriInfo.getQueryParameters().keySet().size() > 0) {
			String where = getWhereFromParams(uriInfo.getQueryParameters());
			query.append(where);
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
				FileSearchResult result = toResult(rs);
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

	/**
	 * Create a FileSearchResult object from a Row
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private FileSearchResult toResult(ResultSet row) throws SQLException {

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

		boolean bOutbound = (isOutbound == 0) ? false : true;
		boolean bOverride = (isOverride == 0) ? false : true;

		String formattedTimeStamp = df.format(new java.util.Date(ts));

		FileSearchResult result = new FileSearchResultBuilder(bundleID).withErrorCode(errorCode)
				.withTimestamp(formattedTimeStamp).withReference(ref).withType(type).withEntityID(eID)
				.withService(service).withFilename(fname).withWorkflowID(wfID).withStatus(status)
				.withMessageID(messageID).withOutbound(bOutbound).withOverride(bOverride).withDocID(docID).build();

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
