package com.stercomm.customers.rbs.sir.rest.util;

public class SRRLog {
	
	private String routingRuleName;
	//@XmlAttribute(required=true)
	private String failCause;
	
	//@XmlAttribute(required=true)
	private int code;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getRoutingRuleName() {
		return routingRuleName;
	}
	public void setRoutingRuleName(String routingRuleName) {
		this.routingRuleName = routingRuleName;
	}
	
	
	
	public String getFailCause() {
		return failCause;
	}
	public void setFailCause(String failCause) {
		this.failCause = failCause;
	}
	
}
