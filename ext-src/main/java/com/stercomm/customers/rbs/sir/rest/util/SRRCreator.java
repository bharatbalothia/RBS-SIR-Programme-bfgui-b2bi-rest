package com.stercomm.customers.rbs.sir.rest.util;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

public class SRRCreator {
	
	private RoutingRule rule;
	public SRRCreator(RoutingRule _rule) {
		
		rule = _rule;
	}

	public boolean execute() {
		
	boolean success=false;
		
	
	SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
	srro.setRequestor(rule.getRequestorDN());
	srro.setResponder(rule.getResponderDN());
	srro.setRequestType(rule.getRequestType());
	srro.setService(rule.getService());
	srro.setInvokeMode(rule.getInvokeMode());
	srro.setActionType(rule.getActionType());
	srro.setWorkflowName(rule.getWorkflowName());
	srro.setRouteName("gpl.route");
	srro.setUsername(rule.getUsername());
	srro.setNewPriority(rule.getPriority());
	
	System.out.println("(HERE)" +rule);
	try{
		success=srro.saveSWIFTNetRoutingRule(1);
	}
	catch (Exception ie) {
		
		ie.printStackTrace();
	}
	return success;
	}
	
private String getRequestType () {
	
	return "";
	
}
}
