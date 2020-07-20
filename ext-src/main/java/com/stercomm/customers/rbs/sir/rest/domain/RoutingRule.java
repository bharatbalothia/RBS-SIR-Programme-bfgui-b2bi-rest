package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.Arrays;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RoutingRule {

	@Override
	public String toString() {
		return "RoutingRule [requestorDN=" + requestorDN + ", entityName=" + entityName + ", responderDN=" + responderDN
				+ ", requestType=" + Arrays.toString(requestType) + ", service=" + service + ", username=" + username
				+ ", commit=" + commit + "]";
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	@Pattern(regexp = "^(?:(?:(?:(?:cn|ou)=[^,]+,?)+),[\\s]*)*(?:o=[a-z]{6}[0-9a-z]{2}){1},[\\s]*o=swift$",message="Field does not match DN pattern validation")
	private String requestorDN;

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String entityName;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@NotNull(message="Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	@Pattern(regexp = "^(?:(?:(?:(?:cn|ou)=[^,]+,?)+),[\\s]*)*(?:o=[a-z]{6}[0-9a-z]{2}){1},[\\s]*o=swift$",message="Field does not match DN pattern validation")
	private String responderDN;

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String[] requestType;

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String service;

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String username;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	@Pattern(regexp = "gpl|sct", flags = Pattern.Flag.CASE_INSENSITIVE, message="Value must be GPL or SCT")
	private String entityType;

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	private boolean commit = true;

	public boolean isCommit() {
		return commit;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}

}
