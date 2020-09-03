package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TransmitResponse {
	
	private String wfcID;

	public String getWfcID() {
		return wfcID;
	}

	public void setWfcID(String id) {
		this.wfcID = id;
	}


}
