package com.stercomm.customers.rbs.sir.rest.domain;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RoutingRule {
	
	public static final String INVOKE_METHOD = "SYNC";
	public static final String INVOKE_TYPE = "BP";
	public static final String WORKFLOW_NAME = "FB_GPL_SWIFT_Route";
	
	
	@Override
	public String toString() {
		return "RoutingRule [requestorDN=" + requestorDN + ", entityName=" + entityName + ", responderDN=" + responderDN
				+ ", requestType=" + requestType + ", service=" + service + ", workflowName=" + workflowName
				+ ", username=" + username + ", invokeMode=" + invokeMode + ", actionType=" + actionType + ", priority="
				+ priority + ", commit=" + commit + "]";
	}
	public String getRequestorDN() {
		return requestorDN;
	}
	public void setRequestorDN(String requestorDN) {
		this.requestorDN = requestorDN;
	}
	public String getResponderDN() {
		return responderDN;
	}
	public void setResponderDN(String responderDN) {
		this.responderDN = responderDN;
	}
	public String[] getRequestType() {
		return requestType;
	}
	public void setRequestType(String[] requestType) {
		this.requestType = requestType;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getWorkflowName() {
		return workflowName;
	}
	
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getInvokeMode() {
		return invokeMode;
	}
	public void setInvokeMode(String invokeMode) {
		this.invokeMode = invokeMode;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority; 
	}
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String requestorDN;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String entityName;
	
	
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String responderDN;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String[] requestType;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String service;
	
	private String workflowName;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String username;
	
	@Pattern(regexp = "sync|async", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Field values can be async or sync")
	private String invokeMode = INVOKE_METHOD;
	
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String actionType = INVOKE_TYPE;
	
	private int priority = 0;
	
	private boolean commit=false;

	public boolean isCommit() {
		return commit;
	}
	public void setCommit(boolean commit) {
		this.commit = commit;
	}

}
