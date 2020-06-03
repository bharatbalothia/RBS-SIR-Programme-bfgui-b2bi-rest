package com.stercomm.customers.rbs.sir.rest.server;

import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sterlingcommerce.woodstock.util.frame.Manager;

@Path("/properties")
public class PropertiesRestServer{

	private static Logger LOGGER = Logger.getLogger(PropertiesRestServer.class.getName());
	private static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
	
	@PostConstruct
	private void init() {

		try {
			boolean logToConsole = true;
			LOGGER = setupLogging(logToConsole, System.getProperty("user.home") + "/bfgui-rest-properties.log");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}


	@GET
	@Path("/{context}")
	@Produces(MediaType.APPLICATION_JSON)
	public Properties getPropertiesByManager(@PathParam("context") String ctx) {
		LOGGER.info("Returning properties for context : " +ctx);

	//	Properties props = Manager.getProperties();
		Properties props= Manager.getProperties(ctx);
		
		if (props.isEmpty()) {
			
			props = new Properties();
			LOGGER.info("Returning empty properties for context "+ctx+" (might be a client error");
		}
		else {
			int k = props.size();
			LOGGER.info("Returning "+k+" properties for context "+ctx);
					
		}
		return props;

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
