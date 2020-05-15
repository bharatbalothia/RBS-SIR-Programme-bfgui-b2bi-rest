package com.acme.swift.server;

import java.io.IOException;
import java.util.Properties;

public abstract class BaseRestServer {

	protected static  Properties props = new Properties();
	static {
		
		try {
			props.load(BaseRestServer.class.getResourceAsStream("/gpl.properties"));
		}
		catch (IOException ioe) {}
	}

}
