package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.logging.Logger;

import com.stercomm.customers.rbs.sir.rest.server.RoutingRulesRestServer;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;
import com.sterlingcommerce.woodstock.util.frame.Manager;

/**
 * 
 * A builder implementation to wrap the SRR create in something more flexible.  Some logic to look up
 * against properties for workflow name and resolved request type
 * 
 * @author PETERGreaves
 *
 */
public class SWIFTRoutingRule {
	
	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	public static class Builder {
	
		private String requestorDN;
		private String responderDN;
		private String service;
		private String requestType;
		private String invokeMode;
		private String actionType;
		private String workflowName;
		private String routeName;
		private String userName;
		private int priority;
		

		public Builder() {

		}


		public Builder withService(String service) {

			this.service = service;

			return this;
		}

		public Builder withResponderDN(String respDN) {

			this.responderDN = respDN;

			return this;
		}

		public Builder withWorkflowName(String unresolvedRequestType) {

			//resolve the unresolved request name to a workflow type
			this.workflowName = getWorkflowName(unresolvedRequestType);

			return this;
		}

		public Builder withRequestorDN(String reqDN) {

			this.requestorDN = reqDN;

			return this;
		}

		public Builder withRequestType(String unresolvedRequestType) {

			this.requestType = resolvedRequestType(unresolvedRequestType);
			return this;
		}

		public Builder withActionType(String actionType) {

			this.actionType = actionType;
			return this;
		}

		public Builder withInvokeMode(String invokeMode) {

			this.invokeMode = invokeMode;
			return this;
		}
		
		public Builder toRouteName(String entityName, String unresolvedRequestType) {
			
			final String prefix=Manager.getProperties("GPL").getProperty("route.name.prefix");
			final String suffix=Manager.getProperties("GPL").getProperty("route.name.suffix");
			final String sep = Manager.getProperties("GPL").getProperty("route.name.separator");
			
			this.routeName= prefix + entityName + sep + unresolvedRequestType + suffix;
			return this;
			
		}
		public Builder withUserName(String userName) {

			this.userName = userName;
			return this;
		}
		
		public Builder withPriority(int p) {

			this.priority = p;
			return this;
		}

		public SWIFTNetRoutingRuleObj build() {

			SWIFTNetRoutingRuleObj srro = new SWIFTNetRoutingRuleObj();
			
			srro.setService(this.service);
			srro.setRequestType(this.requestType);
			srro.setWorkflowName(this.workflowName);
			srro.setRequestor(this.requestorDN);
			srro.setResponder(this.responderDN);
			srro.setActionType(this.actionType);
			srro.setInvokeMode(this.invokeMode);
			srro.setRouteName(this.routeName);
			srro.setUsername(this.userName);
			srro.setPriority(this.priority);
			
			return srro;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}

		private String resolvedRequestType(String s) {

			// resolve the request type
			

			LOGGER.info("Establishing the resolved request type from : " + s);
			String resolvedReqType = Manager.getProperties("GPL").getProperty("ui.rtm." + s);
			
			if (resolvedReqType == null || resolvedReqType.equalsIgnoreCase("")) {
				resolvedReqType = s.replaceAll(".", "");

				LOGGER.info("Resolved to : " + resolvedReqType);
			}
			return resolvedReqType;
		}

		private String getWorkflowName(String requestType) {
			
			// resolve the request type to a workflow (BP) name 
			// or use the default
			
			LOGGER.info("Establishing the workflow name from request type : " + requestType);

			String s = Manager.getProperties("GPL").getProperty("route." + requestType);

			if ((s == null) || s.equals("")) {
				String wf = Manager.getProperties("GPL").getProperty("routingrule.default.workflow");
				LOGGER.info("No workflow match in properties : using default workflow name (" + wf + ")");
				s = wf;
			}
			else {
				
				LOGGER.info("Resolved request-type specific workflow to : " + s);
			}

			return s;
		}

	}

}
