package com.stercomm.customers.rbs.sir.rest.domain;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RoutingRule {
	
	
	@Override
	public String toString() {
		return "RoutingRule [requestorDN=" + requestorDN + ", responderDN=" + responderDN + ", requestType="
				+ requestType + ", service=" + service + ", workflowName=" + workflowName + ", routeName=" + routeName
				+ ", username=" + username + ", invokeMode=" + invokeMode + ", actionType=" + actionType + ", priority="
				+ priority + "]";
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
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
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
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
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
	private String responderDN;
	
	
	private String requestType;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String service;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String workflowName;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String routeName;
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String username;
	
	@Pattern(regexp = "sync|async", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Field values can be async or sync")
	private String invokeMode;
	
	
	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String actionType;
	
	@Min(value = 0L,	message="Field value must be 0 or 1")
	@Max(value = 1L, message="Field value must be 0 or 1")
	private int priority;
	
	private boolean forReal=false;

	public boolean isForReal() {
		return forReal;
	}
	public void setForReal(boolean forReal) {
		this.forReal = forReal;
	}

}
