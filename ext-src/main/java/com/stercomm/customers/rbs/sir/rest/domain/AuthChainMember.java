package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthChainMember {

	@Override
	public String toString() {
		return "AuthChainMember [subjectDN=" + subjectDN + ", certificateName=" + certificateName+"]";
	}
	private String subjectDN;
	private String certificateName;
	
	public String getSubjectDN() {
		return subjectDN;
	}
	public void setSubjectDN(String subjectDN) {
		this.subjectDN = subjectDN;
	}
	public String getCertificateName() {
		return certificateName;
	}
	public void setCertificateName(String certificateName) {
		this.certificateName = certificateName;
	}
	
}
