package com.acme.swift.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
public class Errors {

	private List<Error> errors;

	@XmlElementWrapper
	@XmlElement(name = "error")
	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> _errors) {
		this.errors = _errors;
	}
	
	public int size() {
		int retval = 0;
		if (errors != null) {
			
			retval = errors.size();
		}
		
		return retval;
	}

}
