package com.stercomm.customers.rbs.sir.rest.util;

/**
 * Simple class to capture result of save attempts on SSRs
 * 
 * @author PETERGreaves
 *
 */
public class SRRCreateLog extends SRRLog{
	
	@Override
	public String toString() {
		return "SRRCreateLog [routeName=" + this.getRoutingRuleName() + ", successOnCreate=" + successOnCreate + ", failCause=" + this.getFailCause()+ ", code=" + this.getCode()+"]";
	}
	
	private boolean successOnCreate;
	
	
	
	public boolean isSuccessOnCreate() {
		return successOnCreate;
	}
	public void setSuccessOnCreate(boolean successOnCreate) {
		this.successOnCreate = successOnCreate;
	}
	
	

}
