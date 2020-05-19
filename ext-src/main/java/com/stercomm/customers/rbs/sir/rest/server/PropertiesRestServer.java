package com.stercomm.customers.rbs.sir.rest.server;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/properties")
public class PropertiesRestServer{

	private static Logger LOGGER = Logger.getLogger(PropertiesRestServer.class.getName());

	private static Properties gplProperties = null;

	@GET
	@Path("{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Properties getProperties(@PathParam("type") String type) {

		Properties gplProperties = null;
		if (type.equalsIgnoreCase("gpl")) {

			if (null == gplProperties) {
				try {
					gplProperties = new Properties();
					gplProperties.load(this.getClass().getResourceAsStream("/gpl.properties"));
				} catch (IOException ioe) {

					LOGGER.fine("Can't load gpl.properties" + ioe.getMessage());
				}

			}
		}
		return gplProperties;

	}
}
