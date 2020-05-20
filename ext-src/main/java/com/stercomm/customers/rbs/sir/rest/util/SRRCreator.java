package com.stercomm.customers.rbs.sir.rest.util;

import java.util.Properties;
import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;
import com.sterlingcommerce.woodstock.util.frame.Manager;

public class SRRCreator {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	private RoutingRule rule;
	private Properties gplProps = null;
	

	public SRRCreator(RoutingRule _rule) {

		rule = _rule;
		
		gplProps = Manager.getProperties("GPL");

	}

	public boolean execute() {

		boolean success = false;

		for (int k = 0; k < rule.getRequestType().length; k++) {
			success = createRR(rule, k);
		}

		return success;
	}


	private boolean createRR(RoutingRule rule, int rType) {

		boolean success = false;
	//	final String subsetForFormat = "gpl.ui.rtm";
	//	final String subsetForRoute = "gpl.route";
		

		SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
		srro.setRequestor(rule.getRequestorDN());
		srro.setResponder(rule.getResponderDN());
		srro.setRequestType(rule.getRequestType()[rType]);
		srro.setService(rule.getService());
		srro.setInvokeMode(rule.getInvokeMode());
		srro.setActionType(rule.getActionType());
		srro.setWorkflowName(rule.getWorkflowName());
		
		// What is the route name?
	
		
		String rrType = gplProps.getProperty("gpl.ui.rtm." + rule.getRequestType()[rType]);
		if(rrType==null || rrType.equalsIgnoreCase("")){
			rrType=rule.getRequestType()[rType].replaceAll(".", "");
		}
		
		srro.setRouteName("GPL_" + rule.getEntityName() + "_" + rn + "_RR");
		srro.setUsername(rule.getUsername());
		srro.setNewPriority(rule.getPriority());

		LOGGER.info("Trying to create new rule : " + srro.getRouteName());
		try {
			success = srro.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION);
			success = true;
			if (success) {
				LOGGER.info("Rule created : " + srro);
			}
		} catch (Exception ie) {

			LOGGER.info("Failed to created rule : " + ie.getMessage());
		}
		return success;

	}
	
	
}
