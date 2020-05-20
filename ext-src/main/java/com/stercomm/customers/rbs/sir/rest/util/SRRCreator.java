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
	private List<SRRCreateLog> logOfCreateAttempts;

	public List<SRRCreateLog> getLogOfCreateAttempts() {
		return logOfCreateAttempts;
	}

	public SRRCreator(RoutingRule _rule) {

		rule = _rule;

		logOfCreateAttempts = new ArrayList<SRRCreateLog>();

		// gplProps = Manager.getProperties("GPL");
		gplProps = new Properties();
		try {
			gplProps.load(this.getClass().getResourceAsStream("/gpl.properties"));
			LOGGER.info("Loaded GPL properties (" + gplProps.size() + ")");
		} catch (IOException ioe) {

			ioe.printStackTrace();
		}

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
		log.setEntityName(rule.getEntityName());

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

		// check the pre-existing domain of Rules
		validate(srro, log);

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
		return log;

	}

	private String getWorkflowName(String requestType) {

		String s = gplProps.getProperty("gpl.route." + requestType);
		LOGGER.info("Determining workflow name for " + requestType);
		if ((s == null) || s.equals("")) {

			s = RoutingRule.WORKFLOW_NAME;
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
			log.setSuccessOnValidate(true);
		}

	}
}
