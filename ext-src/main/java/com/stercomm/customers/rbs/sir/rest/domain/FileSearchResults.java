package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileSearchResults {
	
	private int total;

	@XmlElement(name ="totalRows")
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	private List<FileSearchResult> results;

	@XmlElementWrapper
	public List<FileSearchResult> getResults() {
		return results;
	}

	public void setResults(List<FileSearchResult> results) {
		this.results = results;
	}

}
