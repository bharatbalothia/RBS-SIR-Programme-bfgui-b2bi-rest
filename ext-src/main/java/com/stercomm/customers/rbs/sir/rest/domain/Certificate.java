package com.stercomm.customers.rbs.sir.rest.domain;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Certificate {
	

	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	@XmlElement(name = "certificate-body")
	private String certBody;

	
	public String getCertBody() {
		return certBody;
	}

	public void setCertBody(String certBody) {
		this.certBody = certBody;
	}

	
}
