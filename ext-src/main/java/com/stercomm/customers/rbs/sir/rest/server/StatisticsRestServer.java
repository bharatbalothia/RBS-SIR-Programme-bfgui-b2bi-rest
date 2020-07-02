package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.StatsSearchResult;
import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.stercomm.customers.rbs.sir.rest.util.StatsResultType;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;

@Path("/statistics")
public class StatisticsRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(StatisticsRestServer.class.getName());

	private static final String QUERY_SYSTEM_ERRORS = "SELECT ( SELECT COUNT(*) FROM WORKFLOW_CONTEXT WFC_MAIN_1 WHERE ACTIVITYINFO_ID = 0 AND STEP_ID = 0 AND WORKFLOW_ID IN (SELECT WF_ID FROM ARCHIVE_INFO WHERE ARCHIVE_FLAG=-1 ) AND 1=( SELECT MAX( CASE WHEN BASIC_STATUS = 1 THEN 1 WHEN BASIC_STATUS = 10001 THEN 1 WHEN BASIC_STATUS = 200 THEN 1 "
			+ "WHEN BASIC_STATUS = 10200 THEN 1 WHEN BASIC_STATUS = 100 THEN 1 WHEN BASIC_STATUS = 10100 THEN 1 WHEN "
			+ "BASIC_STATUS = 300 THEN 1 WHEN BASIC_STATUS = 10300  THEN 1 WHEN BASIC_STATUS = 450 THEN 1 WHEN BASIC_STATUS = 10450"
			+ "THEN 1 WHEN BASIC_STATUS = 400 THEN 1 WHEN BASIC_STATUS = 10400 THEN 1 WHEN BASIC_STATUS = 2 THEN 1 WHEN "
			+ "BASIC_STATUS = 10002 THEN 1 ELSE 0 END ) "
			+ "FROM WORKFLOW_CONTEXT WFC_STATUS_1 WHERE WFC_STATUS_1.WORKFLOW_ID = WFC_MAIN_1.WORKFLOW_ID) ) AS FATAL_ERROR FROM DUAL";

	private static final String QUERY_SCT_TX_HOUR = "SELECT COUNT(*) AS transCount "
			+ "FROM SCT_PAYMENT trans WHERE trans.PTIMESTAMP > (SYSDATE-(1/24)) AND PAYMENT_ID>0  AND ( ISOUTBOUND =0 OR ISOUTBOUND = 1)";
	private static final String QUERY_SCT_TX_DAY = "SELECT COUNT(*) AS transCount FROM SCT_PAYMENT trans WHERE "
			+ "trans.PTIMESTAMP > (SYSDATE-1) AND PAYMENT_ID>0 AND ( ISOUTBOUND =0 OR ISOUTBOUND = 1 )";
	private static final String QUERY_SCT_TX_WEEK = "SELECT SUM(tc) as transCount FROM "
			+ "(SELECT COUNT(*) AS tc FROM SCT_PAYMENT trans WHERE trans.PTIMESTAMP > (SYSDATE-7) AND PAYMENT_ID>0 AND "
			+ "( ISOUTBOUND =0 OR ISOUTBOUND = 1 )" + " UNION "
			+ "SELECT COUNT(*) AS tc FROM SCT_PAYMENT_ARCHIVE trans WHERE trans.PTIMESTAMP > (SYSDATE-7) AND "
			+ "PAYMENT_ID>0 AND ( ISOUTBOUND =0 OR ISOUTBOUND = 1 ))";

	private static final String QUERY_SCT_FILE_HOUR = "SELECT COUNT(*) AS bundleCount FROM SCT_BUNDLE bun WHERE bun.BTIMESTAMP > (SYSDATE-(1/24)) AND SERVICE='SCT' AND ( ISOUTBOUND = 0 OR ISOUTBOUND =1 )";
	private static final String QUERY_SCT_FILE_DAY = "SELECT COUNT(*) AS bundleCount FROM SCT_BUNDLE bun WHERE bun.BTIMESTAMP > (SYSDATE-1) AND SERVICE='SCT' AND ( ISOUTBOUND =0 OR ISOUTBOUND =1)";
	private static final String QUERY_SCT_FILE_WEEK = "SELECT COUNT(*) AS bundleCount FROM SCT_BUNDLE bun WHERE bun.BTIMESTAMP > (SYSDATE-7) AND SERVICE='SCT' AND ( ISOUTBOUND =0 OR ISOUTBOUND =1 )";

	private static final String QUERY_SCT_PAW_HOUR="SELECT SUM(tc) as transCount FROM ("+
			"SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT trans WHERE trans.PTIMESTAMP > (SYSDATE-(1/24)) AND PAYMENT_ID>0 AND ISOUTBOUND =2 " + 
			"UNION SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT_ARCHIVE trans WHERE trans.PTIMESTAMP > (SYSDATE-(1/24)) AND PAYMENT_ID>0 AND ISOUTBOUND =2)";
	private static final String QUERY_SCT_PAW_DAY="SELECT SUM(tc) as transCount FROM (" + 
			"SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT trans WHERE trans.PTIMESTAMP > (SYSDATE-1) AND PAYMENT_ID>0 AND ISOUTBOUND =2 " + 
			"UNION SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT_ARCHIVE trans WHERE trans.PTIMESTAMP > (SYSDATE-1) AND PAYMENT_ID>0 AND ISOUTBOUND =2)";
	private static final String QUERY_SCT_PAW_WEEK="SELECT SUM(tc) as transCount FROM (" + 
			"SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT trans WHERE trans.PTIMESTAMP > (SYSDATE-7) AND PAYMENT_ID>0 AND ISOUTBOUND =2 " + 
			"UNION SELECT COUNT(*) AS tc " + 
			"FROM SCT_PAYMENT_ARCHIVE trans WHERE trans.PTIMESTAMP > (SYSDATE-7) AND PAYMENT_ID>0 AND ISOUTBOUND =2)";
	
	private static final String QUERY_SCT_ALERTS_FILE="SELECT count(*) AS bundleCount FROM SCT_BUNDLE bun where bun.STATUS < 0 AND SERVICE='SCT'";
	private static final String QUERY_SCT_ALERTS_TX="SELECT count(*) AS transCount FROM SCT_PAYMENT pay where pay.STATUS < 0 AND MESSAGE_ID>0";
	
	private Map<StatsResultType, String> sctStatsQueryMap = null;
	private Map<StatsResultType, String> sctAlertsQueryMap = null;

	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.statistics.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sctStatsQueryMap = new HashMap<StatsResultType, String>();
		sctStatsQueryMap.put(StatsResultType.SCT_FILES_HOUR, QUERY_SCT_FILE_HOUR);
		sctStatsQueryMap.put(StatsResultType.SCT_FILES_DAY, QUERY_SCT_FILE_DAY);
		sctStatsQueryMap.put(StatsResultType.SCT_FILES_WEEK, QUERY_SCT_FILE_WEEK);
		sctStatsQueryMap.put(StatsResultType.SCT_TX_HOUR, QUERY_SCT_TX_HOUR);
		sctStatsQueryMap.put(StatsResultType.SCT_TX_DAY, QUERY_SCT_TX_DAY);
		sctStatsQueryMap.put(StatsResultType.SCT_TX_WEEK, QUERY_SCT_TX_WEEK);
		sctStatsQueryMap.put(StatsResultType.SCT_PAW_HOUR, QUERY_SCT_PAW_HOUR);
		sctStatsQueryMap.put(StatsResultType.SCT_PAW_DAY, QUERY_SCT_PAW_DAY);
		sctStatsQueryMap.put(StatsResultType.SCT_PAW_WEEK, QUERY_SCT_PAW_WEEK);
		
		sctAlertsQueryMap = new HashMap<StatsResultType, String>();
		sctAlertsQueryMap.put(StatsResultType.SCT_ALERTS_FILE, QUERY_SCT_ALERTS_FILE);
		sctAlertsQueryMap.put(StatsResultType.SCT_ALERTS_TX, QUERY_SCT_ALERTS_TX);
		
		
	}

	@GET
	@Path("/system-errors")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatistics() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		StatsSearchResult result = new StatsSearchResult(0, StatsResultType.FATAL_ERROR);

		try {
			conn = Conn.getConnection();
			ps = conn.prepareStatement(QUERY_SYSTEM_ERRORS);
			rs = ps.executeQuery();

			while (rs.next()) {
				result.setCount(rs.getInt(1));

			}
		}

		catch (Exception e) {
			LOGGER.severe("SQL Error generating stats : " + e.getMessage());

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

		return Response.status(Status.OK).entity(result).build();

	}

	/**
	 * Gets stats for the relevant period
	 * 
	 * @param period
	 * @return
	 */
	@GET
	@Path("/sct-traffic")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSCTStatistics() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<StatsSearchResult> results = new ArrayList<StatsSearchResult>();
		LOGGER.info("stats queries for " + sctStatsQueryMap.keySet());
		for (StatsResultType type : sctStatsQueryMap.keySet()) {

			try {
				conn = Conn.getConnection();
				ps = conn.prepareStatement(sctStatsQueryMap.get(type));
				rs = ps.executeQuery();

				while (rs.next()) {
					StatsSearchResult s = new StatsSearchResult(rs.getInt(1), type);
					results.add(s);
				}
				
			}

			catch (Exception e) {
				LOGGER.severe("SQL Error generating stats : " + e.getMessage());

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
		}
		return Response.status(Status.OK).entity(results).build();

	}
	/**
	 * Gets stats for the relevant period
	 * 
	 * @param period
	 * @return
	 */
	@GET
	@Path("/sct-alerts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSCTAlerts() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<StatsSearchResult> results = new ArrayList<StatsSearchResult>();
	
		for (StatsResultType type : sctAlertsQueryMap.keySet()) {

			try {
				conn = Conn.getConnection();
				ps = conn.prepareStatement(sctAlertsQueryMap.get(type));
				rs = ps.executeQuery();

				while (rs.next()) {
					StatsSearchResult s = new StatsSearchResult(rs.getInt(1), type);
					results.add(s);
				}
				
			}

			catch (Exception e) {
				LOGGER.severe("SQL Error generating alert stats : " + e.getMessage());

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
		}
		return Response.status(Status.OK).entity(results).build();

	}

}
