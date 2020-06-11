package com.stercomm.customers.rbs.sir.rest.util;

/**
 * Simple class to capture result of update attempts on SSRs
 * 
 * @author PETERGreaves
 *
 */
public class SRRUpdateLog extends SRRLog{
	
	@Override
	public String toString() {
		return "SRRCreateLog [routeName=" + this.getRoutingRuleName() + ", updateAction=" + updateAction + ", failCause=" + this.getFailCause()+ ", code=" + this.getCode()+"]";
	}
	
	public String getUpdateAction() {
		return updateAction;
	}

	public void setUpdateAction(String updateAction) {
		this.updateAction = updateAction;
	}

	private String updateAction;
	

}
