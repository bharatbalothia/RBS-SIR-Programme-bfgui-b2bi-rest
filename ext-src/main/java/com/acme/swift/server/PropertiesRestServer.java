package com.acme.swift.server;

import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/properties")
public class PropertiesRestServer extends BaseRestServer{

	
	
	private static Logger LOGGER = Logger.getLogger(PropertiesRestServer.class.getName());

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Properties getProperties() {

		return props;
	}
	//comment

	
	

}
