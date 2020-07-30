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
	private String certificateBody;

	
	public String getCertificateBody() {
		return certificateBody;
	}

	public void setCertificateBody(String certBody) {
		this.certificateBody = certBody;
	}

	
}
