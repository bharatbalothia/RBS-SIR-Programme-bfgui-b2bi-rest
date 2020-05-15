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
package com.acme.swift.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

//import com.sterlingcommerce.woodstock.util.Util;
import com.sterlingcommerce.woodstock.util.frame.Manager;

@SuppressWarnings("unchecked")
public class Utils {

	
	private static Logger LOGGER = Logger.getLogger(Utils.class.getName());
	
	private static final long serialVersionUID = 1L;
/*	
		public static String getGISProperty(String propertyFile, String key){
		return Utils.getInstance().getGISPropertyI(propertyFile, key);
	}
	
	public String getGISPropertyI(String propertyFile, String key){
		Properties propsFile = null;	
		String value;		
		try {
			propsFile = Manager.getProperties(propertyFile);
			if(propsFile==null) {
				Manager.refreshPropertiesFromDisk(propertyFile);
				propsFile = Manager.getProperties(propertyFile);
			}
			value = propsFile.getProperty(key);
		} catch(Exception e){
			String msg = "could not load GIS property '"+key+"' from file '"+propertyFile+"'";
			LOGGER.finest(msg);
			throw new RuntimeException(msg, e);
		} 
		return value;
	}
	
	public static Properties getGISProperties(String propertyFile){
		Utils utils = getInstance();
		return utils.getGISPropertiesI(propertyFile);
	}
	
	public Properties getGISPropertiesI(String propertyFile){
		Properties propsFile = null;		
		try {
			propsFile = Manager.getProperties(propertyFile);
			if(propsFile==null) {
				Manager.refreshPropertiesFromDisk(propertyFile);
				propsFile = Manager.getProperties(propertyFile);
			}
		}
		catch(Exception e){
			String msg = "could not load GIS property file '"+propertyFile+"'";
			LOGGER.finest(msg);
			throw new RuntimeException(msg, e);
		} 
		return propsFile;
	}
	
	
	
	
	/*
	public String getGISUsername(HttpServletRequest request){
		return (String)request.getSession().getAttribute(Constants.GIS_USERNAME);
	}
	*/
	
	/*
	
	public static boolean adminAudit(HttpServletRequest request, Entity entity){
		Utils utils = getInstance();
		return utils.adminAuditI(request, entity);
	}
	
	
	public boolean adminAuditI(HttpServletRequest request, Entity entity){
		
		String actionType = entity.isCreateBean()?"Create Entity":"Edit Entity";
		String objectName = entity.getEntity();
		String actionValue = entity.toString();
		//TODO: change actionValue to XML;
		
		return adminAudit(request, actionType, objectName, actionValue);
		
	}
	*/
	
	/**
	 * Changes to Entity and overrides/terminates actions from the File Monitor should 
	 * be logged in the Admin Audit. Code to use is 
	 * DmiVisEventFactory.fireAdminAuditEvent(6, ExceptionLevel.NORMAL, Util.createGUID(), 
	 * System.currentTimeMillis(), <user>, <action_type>, "SCT", <object_name>, <action_value>); 
	 * 
	 * For Entity chnages <action_type>="Edit or Create Entity", 
	 * <object_name>=<entity> and 
	 * <action_value>=<entity>. 
	 * 
	 * For Terminate action, 
	 * 
	 * <action_type>="Terminate File", 
	 * <object_name>=WFID+"_"+BundleID and 
	 * <action_value>=WFID+"_"+BundleID
	 *
	 * public static void fireAdminAuditEvent(
     *       int numericTag, ExceptionLevel exceptionLevel,
     *       String adminAuditId, long time, String principal, String actionType,
     *       String objectType, String objectName, String actionValue)  throws InvalidEventException {
	 *
	 *
	 * @return
	 */
	/*
	public boolean adminAudit(HttpServletRequest request,String actionType, String objectName, String actionValue){
		
		int numericTag = 6;
		ExceptionLevel exceptionLevel = ExceptionLevel.EXCEPTIONAL;
		String adminAuditId = Util.createGUID();
		long time = System.currentTimeMillis();
		String principal = getGISUsername(request);
		String objectType = "SCT";
		 
		return adminAudit(numericTag, exceptionLevel, adminAuditId, time, principal, actionType, objectType, objectName, actionValue);
		
		//6, ExceptionLevel.NORMAL, Util.createGUID(), System.currentTimeMillis(), <user>, <action_type>, "SCT", <object_name>, <action_value>
	
	}
	

	public boolean adminAudit(int numericTag,
		 ExceptionLevel exceptionLevel,
		 String adminAuditId, 
		 long time, 
		 String principal, 
		 String actionType,
		 String objectType, 
		 String objectName, 
		 String actionValue){
		
		
		try {
			DmiVisEventFactory.fireAdminAuditEvent(numericTag, exceptionLevel, adminAuditId, time, principal, actionType, objectType, objectName, actionValue);
		} catch (InvalidEventException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		return true;
		
	}
	*/
	
	public static boolean parseSWIFTDN(String reqDN, String respDN){
		boolean ret = true;
		try {
			String dir = createSWIFTDirectory(reqDN,respDN); 
			if(dir.equalsIgnoreCase("")){
				ret = false;
			}
		} catch (Exception e){
			ret = false;
		}
		return ret;
	}

	public static String createSWIFTDirectory(String reqDN, String respDN){

		String swiftDir = readSWIFTDN(respDN) + "/" + readSWIFTDN(reqDN);
		return swiftDir;
	}

	public static String readSWIFTDN(String dn){

		final String SWIFT_DN_END = ",o=swift";

		String respstub = dn.substring(0, dn.indexOf( SWIFT_DN_END ));
		String[] tok = respstub.split(",");

		StringBuffer sb = new StringBuffer();
		for(int i=tok.length-1;i>=0;i--){
			String fld = tok[i].replaceAll("ou=", "").trim();
			fld = fld.replaceAll("o=", "").trim();
			fld = fld.replaceAll("cn=", "").trim();
			if(!fld.equalsIgnoreCase("")){
				sb.append(fld);
				if(i>0){
					sb.append("_");
				}
			}
		}

		return sb.toString();

	}
	
}
