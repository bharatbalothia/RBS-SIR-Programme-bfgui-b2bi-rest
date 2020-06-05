package com.stercomm.customers.rbs.sir.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

/**
 * Utility task to validate candidate rules. The rules that
 * 
 * @author PETERGreaves
 *
 */
public class SRRValidator {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	public List<SWIFTNetRoutingRuleObj> validatedList(List<SWIFTNetRoutingRuleObj> in, SRRCreateLogs logs) {

		List<SWIFTNetRoutingRuleObj> listOut = new ArrayList<SWIFTNetRoutingRuleObj>();
		LOGGER.info("Validation start : SRR candidate count pre-validation : " + in.size());
		for (SWIFTNetRoutingRuleObj swiftNetRoutingRuleObj : in) {

			// case 1. does the route exist by name?  
			String routeName = swiftNetRoutingRuleObj.getRouteName();
			String existsByName = swiftNetRoutingRuleObj.checkExistsByName(routeName);
			// case 2 or with similar properties
			String existingSimilarRuleID = swiftNetRoutingRuleObj.exists(swiftNetRoutingRuleObj.getRequestor(),swiftNetRoutingRuleObj.getResponder(),swiftNetRoutingRuleObj.getService(),swiftNetRoutingRuleObj.getRequestType());

			if (null != existsByName) {
				LOGGER.severe("A rule called " + routeName +" already exists..skipping this SRR candidate.");
				SRRCreateLog alreadyExistsLog = new SRRCreateLog();
				alreadyExistsLog.setFailCause("A route already exists with this name.");
				alreadyExistsLog.setSuccessOnCreate(false);
				alreadyExistsLog.setRouteName(routeName);
				alreadyExistsLog.setCode(409); //conflict
				LOGGER.info("Adding the log in validate");
				logs.appendLog(alreadyExistsLog);
				
			} 
			// case 2...?
			else if (null!=existingSimilarRuleID) {
				
				String msg = "A rule called " + routeName +" was not found, but a rule exists already with the same parameters.";
				LOGGER.severe(msg);
				SRRCreateLog alreadyExistsLog = new SRRCreateLog();
				alreadyExistsLog.setFailCause(msg);
				alreadyExistsLog.setSuccessOnCreate(false);
				alreadyExistsLog.setRouteName(routeName);
				alreadyExistsLog.setCode(409); //conflict
				LOGGER.info("Adding the log in validate");
				logs.appendLog(alreadyExistsLog);
			}
	
			else {
				LOGGER.info("SRR passed validation, queueing for create : " + routeName);
				listOut.add(swiftNetRoutingRuleObj);
			}
		}

		LOGGER.info("Validation complete : SRR candidate count post-validation : " + listOut.size());
		return listOut;

	}
}
