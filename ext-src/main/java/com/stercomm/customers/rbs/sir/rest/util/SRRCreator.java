package com.stercomm.customers.rbs.sir.rest.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

public class SRRCreator {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	private RoutingRule rule;
	private Properties gplProps = null;
	private List<SWIFTNetRoutingRuleObj> createdRules;

	public SRRCreator(RoutingRule _rule) {

		rule = _rule;
		
		createdRules = new ArrayList<SWIFTNetRoutingRuleObj>();

		//gplProps = Manager.getProperties("GPL");
		gplProps = new Properties();
		try{
			gplProps.load(this.getClass().getResourceAsStream("/gpl.properties"));
			LOGGER.info("Loaded GPL properties (" + gplProps.size() + ")" );
		}
		catch(IOException ioe) {
			
			ioe.printStackTrace();
		}

	}

	public boolean execute() {

		boolean success = false;

		for (int k = 0; k < rule.getRequestType().length; k++) {
			LOGGER.info("Creating rule for " + rule.getEntityName() + " [" + rule.getRequestType()[k] + "]");
			success = createRR(rule, k);
		}

		return success;
	}

	private boolean createRR(RoutingRule rule, int rType) {

		boolean success = false;
		// final String subsetForFormat = "gpl.ui.rtm";
		// final String subsetForRoute = "gpl.route";

		String unresolvedRequestType = rule.getRequestType()[rType];

		SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
		srro.setRequestor(rule.getRequestorDN());
		srro.setResponder(rule.getResponderDN());

		srro.setService(rule.getService());
		srro.setInvokeMode(rule.getInvokeMode());
		srro.setActionType(rule.getActionType());

		// work out the wf name from the request type, add it back to the rule and srro
		rule.setWorkflowName(getWorkflowName(unresolvedRequestType));
		srro.setWorkflowName(rule.getWorkflowName());

		// What is the req type?

		String resolvedReqType = gplProps.getProperty("gpl.ui.rtm." + unresolvedRequestType);
		if (resolvedReqType == null || resolvedReqType.equalsIgnoreCase("")) {
			resolvedReqType = unresolvedRequestType.replaceAll(".", "");
		}

		srro.setRequestType(resolvedReqType);

		srro.setRouteName("GPL_" + rule.getEntityName() + "_" + unresolvedRequestType + "_RR");
		srro.setUsername(rule.getUsername());
		srro.setNewPriority(rule.getPriority());

		LOGGER.info("Trying to create new rule : " + srro.getRouteName());
		try {
			createdRules.add(srro);
			
			if (rule.isCommit()) {

				success = srro.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION);
				if (success) {
					LOGGER.info("Rule created : " + srro);
				}
			}
		} catch (Exception ie) {

			LOGGER.info("Failed to created rule : " + ie.getMessage());
		}
		return success;

	}

	private String getWorkflowName(String requestType) {

		String s = gplProps.getProperty("gpl.route." + requestType);
		LOGGER.info("Determining workflow name for "  +requestType);
		if ((s == null) || s.equals("")) {

			s = RoutingRule.WORKFLOW_NAME;
			LOGGER.info("Using default workflow name : "  +s);
		}

		return s;
	}
	
	public List<SWIFTNetRoutingRuleObj> getCreatedRules(){
		
		return createdRules;
		
	}
}
