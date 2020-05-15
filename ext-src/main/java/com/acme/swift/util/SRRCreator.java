package com.acme.swift.util;

import java.util.Properties;

import com.acme.swift.domain.RoutingRule;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

public class SRRCreator {
	
	private RoutingRule rule;
	public SRRCreator(RoutingRule _rule) {
		
		rule = _rule;
	}

	public boolean execute() {
		
		boolean success=false;
		
		Properties props = new Properties();//Utils.getGISProperties("gpl");
		
	
	SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
	srro.setRequestor(rule.getRequestorDN());
	srro.setResponder(rule.getResponderDN());
	srro.setRequestType(rule.getRequestType());
	srro.setService(rule.getService());
	srro.setInvokeMode(rule.getInvokeMode());
	srro.setActionType(rule.getActionType());
	srro.setWorkflowName(rule.getWorkflowName());
	srro.setRouteName(props.getProperty("gpl.tou"));
	srro.setUsername(rule.getUsername());
	srro.setNewPriority(rule.getPriority());
	
	
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
