package com.stercomm.customers.rbs.sir.rest.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.Bundle;
import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;

@Path("/bundles")
public class BundlesRestServer{

	private static Logger LOGGER = Logger.getLogger(BundlesRestServer.class.getName());
	private static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
	
	@PostConstruct
	private void init() {

		try {
			boolean logToConsole = true;
			LOGGER = setupLogging(logToConsole, System.getProperty("user.home") + "/bfgui-rest-bundles.log");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response redeliverBundle(Bundle bundle) {
		
		long bundleID = bundle.getBundleID();
		
		LOGGER.info("Attempting redelivery for  : " +bundleID);
		
		// get the delivery key
		LOGGER.info("Looking for delivery key for  : " +bundleID);
		String delKey = getDeliveryKey(bundleID);

		if (null == delKey) {
			final String errText = "No delivery key found for  : " +bundleID;
			LOGGER.severe(errText);
			Error e = new Error();
			e.setMessage(errText);
			e.setAttribute("bundleID");
			return Response.status(Status.BAD_REQUEST).entity(e).build();
		}
		// we have a delivery key...is it a duplicate?
		
		// 
		return Response.status(Status.OK).entity(null).build();
		
	}
	
	/**
	 * retrieve the delivery key for the bundle ID
	 * 
	 * @param bundleID
	 * @return the key or null if not found
	 */
	private String getDeliveryKey(long bundleID) {
		
		Connection conn=null;
		PreparedStatement spstmt = null;
		final String ssqlStr = "select delivery_key from FB_SFGLEGACY_LINK where bundle_id = ?";
		ResultSet rs = null;
		String key = null;

		try {

			conn = Conn.getConnection();
			spstmt = conn.prepareStatement(ssqlStr);
			spstmt.setLong(1, bundleID);

			rs = spstmt.executeQuery();
			boolean b = rs.first();
			if (b) {
				key=rs.getString(1);
			}
		
		} catch (Exception e) {
			LOGGER.severe("SQL Error searching the FB LegacyLink table : "+ e.getMessage());
			
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (spstmt != null) {
					spstmt.close();
				}
				if(conn!=null){
					Conn.freeConnection(conn);
				}
			} catch (SQLException se) {
				LOGGER.severe("SQL exception : " +se.getMessage());
			}
		}
		return key;
	}
	
	private Logger setupLogging(boolean logToConsole, String logFile) throws Exception {
		// Setup the logging
		System.setProperty("java.util.logging.SimpleFormatter.format", FORMAT_STRING);
		LogManager.getLogManager().reset();
		Logger thisLogger = Logger.getLogger(this.getClass().getName());
		if (logFile != null) {
			FileHandler logHandler = new FileHandler(logFile, 8 * 1024 * 1024, 2, true);
			logHandler.setFormatter(new SimpleFormatter());
			logHandler.setLevel(Level.FINEST);
			thisLogger.addHandler(logHandler);
		}

		if (logToConsole) {
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());
			consoleHandler.setLevel(Level.INFO);
			thisLogger.addHandler(consoleHandler);
		}

		thisLogger.setLevel(Level.INFO);
		return thisLogger;
	}

}
