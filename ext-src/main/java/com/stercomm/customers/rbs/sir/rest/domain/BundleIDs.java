package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BundleIDs {
	private long[] bundleID;
	private String[] arrivedFileKey;

	public String[] getArrivedFileKey() {
		return arrivedFileKey;
	}

	public void setArrivedFileKey(String[] arrivedFileKey) {
		this.arrivedFileKey = arrivedFileKey;
	}

	public long[] getBundleID() {
		return bundleID;
	}

	public void setBundleID(long[] bundleID) {
		this.bundleID = bundleID;
	}
}
