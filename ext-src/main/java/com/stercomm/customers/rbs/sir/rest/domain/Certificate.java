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
	@XmlElement(name = "certificate-name")
	private String certName;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message="Field cannot be empty")
	@XmlElement(name = "certificate-body")
	private String certBody;

	public String getCertName() {
		return certName;
	}

	
	public void setCertName(String certName) {
		this.certName = certName;
	}

	public String getCertBody() {
		return certBody;
	}

	public void setCertBody(String certBody) {
		this.certBody = certBody;
	}

	@Override
	public String toString() {
		return "Certificate [certName=" + certName + ", certBody=" + certBody + "]";
	}
	
}
