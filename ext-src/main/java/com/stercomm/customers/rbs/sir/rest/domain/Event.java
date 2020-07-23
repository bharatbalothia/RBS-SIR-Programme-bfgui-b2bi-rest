package com.stercomm.customers.rbs.sir.rest.domain;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Event {
	

	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String actionBy;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	
	@Pattern(regexp = "create|update|delete",flags = Pattern.Flag.CASE_INSENSITIVE,  message="Action Type must be one of : create, update, delete")
	private String actionType;
	
	@Pattern(regexp = "requested|approved|rejected", flags = Pattern.Flag.CASE_INSENSITIVE, message="Type must be one of : approved, rejected, requested")
	private String eventType;
	
	@Pattern(regexp = "Entity|Trusted Certificate",flags = Pattern.Flag.CASE_INSENSITIVE, message="Type must be one of : entity, trusted certificate")
	private String type;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private String changeID;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	private	String actionValue;
	
	
	public String getActionBy() {
		return actionBy;
	}
	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getType() {
		return type;
	}
	public void setType(String objectType) {
		this.type = objectType;
	}
	
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getActionValue() {
		return actionValue;
	}
	public void setActionValue(String actionValue) {
		this.actionValue = actionValue;
	}
	public String getChangeID() {
		return changeID;
	}
	public void setChangeID(String changeID) {
		this.changeID = changeID;
	}
	@Override
	public String toString() {
		return "Event [actionBy=" + actionBy + ", actionType=" + actionType + ", eventType=" + eventType + ", type="
				+ type + ", changeID=" + changeID + ", actionValue=" + actionValue + "]";
	}
	
	

	
}
