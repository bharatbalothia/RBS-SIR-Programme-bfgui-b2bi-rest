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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
public class FilesRestServer extends BaseRestServer{

	private static Logger LOGGER = Logger.getLogger(FilesRestServer.class.getName());
	private static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
	
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");

	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSearchForFiles(@Context UriInfo uriInfo) {

		String fName=uriInfo.getQueryParameters().getFirst("filename");
		
		 
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		final int rowsToReturn=10;

		List<FileSearchResult> results = new ArrayList<FileSearchResult>();
		
		String query = "SELECT bundle_id, filename, reference, btimestamp, btype, entity_id, status, error, wf_id, message_id, "
				+ "isoutbound, isoverride, service, doc_id FROM SCT_BUNDLE "+
				new FileSearchWhereClauseBuilder().withFilename(fName).build();
		
		LOGGER.info("Query : " + query);
		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(query);
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
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private FileSearchResult toResult(ResultSet row) throws SQLException{
		
		LOGGER.info("Row : " + row);
		
		int bundleID = row.getInt(1);
		String fname=row.getString(2);
		String ref=row.getString(3);
		long ts =row.getTimestamp(4).getTime();
		String type=row.getString(5);
		int eID = row.getInt(6);
		int status = row.getInt(7);
		String errorCode=row.getString(8);
		int wfID = row.getInt(9);
		long messageID =row.getLong(10);
		int isOutbound=row.getInt(11);
		int isOverride=row.getInt(12);
		String service=row.getString(13);
		String docID=row.getString(14);
		
		boolean bOutbound=(isOutbound==0)?false:true;
		boolean bOverride=(isOverride==0)?false:true;
		
		String formattedTimeStamp = df.format(new java.util.Date(ts));
		
		FileSearchResult result = new FileSearchResultBuilder(bundleID).withErrorCode(errorCode).withLastUpdated(formattedTimeStamp).withReference(ref)
				.withType(type).withEntityID(eID).withService(service)
				.withFilename(fname).withWorkflowID(wfID).withStatus(status)
				.withMessageID(messageID).withOutbound(bOutbound).withOverride(bOverride).withDocID(docID)
				.build();
		
		return result;

	}

}
