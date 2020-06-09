package com.stercomm.customers.rbs.sir.rest.util;

/**
 * Simple class to capture result of save attempts on SSRs
 * 
 * @author PETERGreaves
 *
 */
public class SRRCreateLog {
	
	@Override
	public String toString() {
		return "SRRCreateLog [routeName=" + routingRuleName + ", successOnCreate=" + successOnCreate + ", failCause=" + failCause + "]";
	}
	private String routingRuleName;
	private boolean successOnCreate;
	
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
	
	
	public boolean isSuccessOnCreate() {
		return successOnCreate;
	}
	public void setSuccessOnCreate(boolean successOnCreate) {
		this.successOnCreate = successOnCreate;
	}
	public String getFailCause() {
		return failCause;
	}
	public void setFailCause(String failCause) {
		this.failCause = failCause;
	}
	

}
