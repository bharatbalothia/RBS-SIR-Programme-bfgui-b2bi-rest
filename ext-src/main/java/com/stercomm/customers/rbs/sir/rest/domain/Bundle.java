package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Bundle {
	private long bundleID;

	public long getBundleID() {
		return bundleID;
	}

	public void setBundleID(long bundleID) {
		this.bundleID = bundleID;
	}
}
