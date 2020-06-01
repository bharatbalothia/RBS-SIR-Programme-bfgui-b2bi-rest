/*******************************************************************
 Copyright (c) 2006 Sterling Commerce, Inc. All Rights Reserved.

 ** Trade Secret Notice **

 This software, and the information and know-how it contains, is
 proprietary and confidential and constitutes valuable trade secrets
 of Sterling Commerce, Inc., its affiliated companies or its or
 their licensors, and may not be used for any unauthorized purpose
 or disclosed to others without the prior written permission of the
 applicable Sterling Commerce entity. This software and the
 information and know-how it contains have been provided
 pursuant to a license agreement which contains prohibitions
 against and/or restrictions on its copying, modification and use.
 Duplication, in whole or in part, if and when permitted, shall
 bear this notice and the Sterling Commerce, Inc. copyright
 legend. As and when provided to any governmental entity,
 government contractor or subcontractor subject to the FARs,
 this software is provided with RESTRICTED RIGHTS under
 Title 48 CFR 52.227-19.
 Further, as and when provided to any governmental entity,
 government contractor or subcontractor subject to DFARs,
 this software is provided pursuant to the customary
 Sterling Commerce license, as described in Title 48
 CFR 227-7202 with respect to commercial software and commercial
 software documentation.
**********************************************************************
* Current Version
* ================
* Revision:   $Revision: $
* Date/time:  $Date: $
**********************************************************************/
package com.stercomm.customers.rbs.sir.rest.util;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.exception.CreateDirectoryException;
import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
//import com.sterlingcommerce.woodstock.util.Util;
import com.sterlingcommerce.woodstock.util.frame.Manager;

@SuppressWarnings("unchecked")
public class Utils {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	private static final long serialVersionUID = 1L;

	private static final ThreadLocal threadSession = new ThreadLocal();

	private Utils() {
	}

	public static Utils getInstance() {
		Utils utils = (Utils) threadSession.get();

		if (utils == null) {
			synchronized (Utils.class) {
				utils = (Utils) threadSession.get();

				if (utils == null) {
					utils = new Utils();
					threadSession.set(utils);
				}
			}
		}
		return utils;
	}

	public static String getGISProperty(String propertyFile, String key) {
		return Utils.getInstance().getGISPropertyI(propertyFile, key);
	}

	public String getGISPropertyI(String propertyFile, String key) {
		Properties propsFile = null;
		String value;
		try {
			propsFile = Manager.getProperties(propertyFile);
			if (propsFile == null) {
				Manager.refreshPropertiesFromDisk(propertyFile);
				propsFile = Manager.getProperties(propertyFile);
			}
			value = propsFile.getProperty(key);
		} catch (Exception e) {
			String msg = "could not load GIS property '" + key + "' from file '" + propertyFile + "'";
			LOGGER.severe(msg);
			throw new RuntimeException(msg, e);
		}
		return value;
	}

	public static Properties getGISProperties(String propertyFile) {
		Utils utils = getInstance();
		return utils.getGISPropertiesI(propertyFile);
	}

	public Properties getGISPropertiesI(String propertyFile) {
		Properties propsFile = null;
		try {
			propsFile = Manager.getProperties(propertyFile);
			if (propsFile == null) {
				Manager.refreshPropertiesFromDisk(propertyFile);
				propsFile = Manager.getProperties(propertyFile);
			}
		} catch (Exception e) {
			String msg = "could not load GIS property file '" + propertyFile + "'";
			LOGGER.severe(msg);
			throw new RuntimeException(msg, e);
		}
		return propsFile;
	}

	public static boolean parseSWIFTDN(String reqDN, String respDN) {
		boolean ret = true;
		try {
			String dir = createSWIFTDirectoryPath(reqDN, respDN);
			if (dir.equalsIgnoreCase("")) {
				ret = false;
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public static String createSWIFTDirectoryPath(String reqDN, String respDN) {

		String swiftDir = readSWIFTDN(respDN) + "/" + readSWIFTDN(reqDN);
		return swiftDir;
	}

	/**
	 * Given a rule object, use the DNs and the base storage path to create a
	 * directory
	 * 
	 * Throw an exception if the dir cannot be created.
	 * 
	 * If the dir already exists, nothing happens - the existing directory is
	 * untouched
	 * 
	 * @param rule
	 * @throws CreateDirectoryException
	 */
	public static void createSWIFTDirectory(RoutingRule rule) throws CreateDirectoryException {

		final String reqDN = rule.getRequestorDN();
		final String respDN = rule.getResponderDN();
		boolean dirCreated = false;

		final String dirToCreate = Utils.createSWIFTDirectoryPath(reqDN, respDN);
		final String basePath = Manager.getProperties("sfg").getProperty("sharedstorage.path");

		File swiftDir = new File(basePath + File.separator + dirToCreate);

		// does it exist already?
		boolean dirExists = swiftDir.exists();

		// if so, just log and return

		if (dirExists) {
			LOGGER.info("SWIFT dir at " + swiftDir.getAbsolutePath() + " already exists, so not creating it.");
			return;
		}

		// it doesn't exist, so let's try to create it
		if (!swiftDir.exists()) {
			dirCreated = swiftDir.mkdirs();
		}

		// were we successful?
		if (dirCreated) {

			// yes
			LOGGER.info("Created new path at : " + swiftDir.getAbsolutePath());
			return;
		} else {

			// no
			throw new CreateDirectoryException("Could not create dir at : " + swiftDir.getAbsolutePath());
		}
	}

	public static String readSWIFTDN(String dn) {

		final String SWIFT_DN_END = ",o=swift";

		String respstub = dn.substring(0, dn.indexOf(SWIFT_DN_END));
		String[] tok = respstub.split(",");

		StringBuffer sb = new StringBuffer();
		for (int i = tok.length - 1; i >= 0; i--) {
			String fld = tok[i].replaceAll("ou=", "").trim();
			fld = fld.replaceAll("o=", "").trim();
			fld = fld.replaceAll("cn=", "").trim();
			if (!fld.equalsIgnoreCase("")) {
				sb.append(fld);
				if (i > 0) {
					sb.append("_");
				}
			}
		}

		return sb.toString();

	}

}
