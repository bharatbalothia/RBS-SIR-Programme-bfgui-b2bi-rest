package com.stercomm.customers.rbs.sir.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;
import com.sterlingcommerce.woodstock.util.frame.Manager;

public class SRRCreator {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	// the rule that passed validation from the caller,
	// that we want to use to create the routing rule
	private RoutingRule rule;
	
	private List<SRRCreateLog> logOfCreateAttempts;

	public List<SRRCreateLog> getLogOfCreateAttempts() {
		return logOfCreateAttempts;
	}

	public SRRCreator(RoutingRule _rule) {

		rule = _rule;
		logOfCreateAttempts = new ArrayList<SRRCreateLog>();

	}

	public boolean execute() {

		boolean success = false;

		for (int k = 0; k < rule.getRequestType().length; k++) {
			LOGGER.info("Creating rule for " + rule.getEntityName() + " [" + rule.getRequestType()[k] + "]");
			logOfCreateAttempts.add(createRoutingRule(rule, k));
		}

		return success;
	}

	private SRRCreateLog createRoutingRule(RoutingRule rule, int rType) {

		SRRCreateLog log = new SRRCreateLog();
		log.setRouteName(rule.getEntityName());

		final String unresolvedRequestType = rule.getRequestType()[rType];

		SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
		srro.setRequestor(rule.getRequestorDN());
		srro.setResponder(rule.getResponderDN());

		srro.setService(rule.getService());
		srro.setInvokeMode("BP");
		srro.setActionType("SYNC");

		// work out the wf name from the request type, add it back to the rule and the srro
	
		srro.setWorkflowName(getWorkflowName(unresolvedRequestType));

		// What is the req type?

		String resolvedReqType=Manager.getProperties("GPL").getProperty("ui.rtm."+unresolvedRequestType);
		if (resolvedReqType == null || resolvedReqType.equalsIgnoreCase("")) {
			resolvedReqType = unresolvedRequestType.replaceAll(".", "");
		}

		srro.setRequestType(resolvedReqType);

		final String prefix=Manager.getProperties("GPL").getProperty("route.name.prefix");
		final String suffix=Manager.getProperties("GPL").getProperty("route.name.suffix");
		final String sep = Manager.getProperties("GPL").getProperty("route.name.separator");
		
		srro.setRouteName(prefix + rule.getEntityName() + sep + unresolvedRequestType + suffix);
		srro.setUsername(rule.getUsername());
		srro.setNewPriority(0);

		// check the pre-existing domain of Rules
		validate(srro, log);
		/*
		if (!log.isSuccessOnValidate()) {

			LOGGER.severe("Cannot create SRR object");
		} else {
			try { //let's see if we can

				if (rule.isCommit() && log.isSuccessOnValidate()) { // are going to try to save it..we know we can
					LOGGER.info("Trying to create new rule : " + srro.getRouteName());
					log.setSuccessOnCreate(srro.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION));

					if (log.isSuccessOnCreate()) { // did we manage to save it?
						LOGGER.info("SRR object created with id : " + srro.getobjectID());
					}
				}
			} catch (Exception ie) {

				LOGGER.info("Failed to created rule : " + ie.getMessage());
			}
		}
		*/
		return log;

	}

	private String getWorkflowName(String requestType) {

	//	String s = gplProps.getProperty("gpl.route." + requestType);
		String s =Manager.getProperties("GPL").getProperty("route." + requestType);
		LOGGER.info("Determining workflow name for " + requestType);
		if ((s == null) || s.equals("")) {

			s = Manager.getProperties("GPL").getProperty("routing.default.workflow");
			LOGGER.info("Using default workflow name : " + s);
		}

		return s;
	}

	private void validate(SWIFTNetRoutingRuleObj srro, SRRCreateLog log) {

		String ruleID = srro.exists(srro.getRequestor(), srro.getResponder(), srro.getService(), srro.getRequestType());

		if (ruleID != null) {

			// we have a record
			// lets check the name is the same!
			LOGGER.info("Found SRR Object with ID: " + ruleID);

			String checkID = srro.checkExistsByName(srro.getRouteName());

			if (checkID == null) {
				String s = "Routing Rule with the required Route Name was not found but a rule exists already with the same parameters";

				LOGGER.severe(s);
				log.setFailCause(s);

			}
			else {
				String s = "Routing Rule exists already with the same name.";

				LOGGER.severe(s);
				log.setFailCause(s);
			}
			

		} else {

			LOGGER.info("No existing SRR Object found for " + srro.getRouteName());
			//log.setSuccessOnValidate(true);
		}

	}
}
