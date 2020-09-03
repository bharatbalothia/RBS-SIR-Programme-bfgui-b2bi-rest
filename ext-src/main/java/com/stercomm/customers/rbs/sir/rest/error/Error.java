package com.stercomm.customers.rbs.sir.rest.error;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "error")
public class Error {
	
	private String message;
	private String attribute;
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
