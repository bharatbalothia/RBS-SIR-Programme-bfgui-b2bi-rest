package com.acme.swift.util;


/**
 * @author RPatel
 *
 */
public class Constants {
	public static final String DB_CONNECTION_POOL = "DB_CONNECTION_POOL";
	public static final String GIS_CONNECTION_POOL_NAME = "DB_CONNECTION_POOL";
	public static final String MESSAGE_RESOURCES = "MESSAGE_RESOURCES";
	public static final String LOCALE = "LOCALE";
	public static final String ERROR_MAP_KEY = "ERROR_MAP_KEY";
	public static final String SETTINGS_BEANS_MAP = "SETTINGS_BEANS_MAP";
	public static final String SQL_QUERIES = "SQL_QUERIES";
	public static final String SYSDATE = "SYSDATE";
	public static final String MESSAGE_TYPES_MAP = "MESSAGE_TYPES_MAP";
	public static final String TRACKING_ID = "TRACKING_ID";
	public static final String TRACKING_BEAN = "TRACKING_BEAN";
	public static final String ERROR_BEAN = "ERROR_BEAN";
	public static final String BULK_COUNT = "BULK_COUNT";
	public static final String BULK_RESPONSE_BEAN = "BULK_RESPONSE_BEAN";
	public static final String CONFIG_PARAM_PREFIX = "config_";
	public static final String HOME_BEANS = "HOME_BEANS";
	
	
	//sql constants
	//public static final String SQL_WHERE = "sqlw";
//	public static final String EQUALS = "eq";
//	public static final String LESS_THAN = "lt";
//	public static final String GREATER_THAN = "gt";
//	public static final String LESS_THAN_EQUALS = "le";
//	public static final String GREATER_THAN_EQUALS = "ge";
//	public static final String NOT_EQUAL = "ne";
	
	//these define whether the where clause should use '='. '
	public static final int EQUALS = 2;
	public static final int LESS_THAN = 3;
	public static final int GREATER_THAN = 4;
	public static final int LESS_THAN_EQUALS = 5;
	public static final int GREATER_THAN_EQUALS = 6;
	public static final int NOT_EQUAL = 7;
	public static final int LIKE = 8;
	public static final int LIKE_BEFORE = 9;
	public static final int LIKE_AFTER = 10;
	
	//these tell what type to convert the value to, either String or Integer
	public static final int INTEGER_TYPE = 101;
	public static final int STRING_TYPE = 102;
	public static final int DATETIME_TYPE = 103;
	public static final String STATUS_MAP = "STATUS_MAP";
	public static final String TRACKING_DETAIL_REQUEST_PARAMS = "TRACKING_DETAIL_REQUEST_PARAMS";
	public static final String SETTINGS_BEANS_MAP_NAME2BEAN = "SETTINGS_BEANS_MAP_NAME2BEAN";
	


	
	


	

	
}
