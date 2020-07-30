package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;


@XmlRootElement
public class CertificateValidationResponse {
	
	private List<AuthChainMember> chain;
	
	private boolean valid=false;
	private boolean selfSigned=false;
	private Error error;
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public boolean isSelfSigned() {
		return selfSigned;
	}
	
	public void setSelfSigned(boolean selfSigned) {
		this.selfSigned = selfSigned;
	}

	@XmlElementWrapper
	@XmlElement(name = "chain")
	@JsonSerialize(include = Inclusion.NON_NULL)
	public List<AuthChainMember> getChain() {
		return chain;
	}

	public void setChain(List<AuthChainMember> _chain) {
		this.chain = _chain;
	}
	
	

}
