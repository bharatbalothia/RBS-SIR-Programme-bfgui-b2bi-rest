package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TransactionSearchResults {
	
	private int total;

	@XmlElement(name ="totalRows")
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	private List<TransactionSearchResult> results;

	@XmlElementWrapper
	public List<TransactionSearchResult> getResults() {
		return results;
	}

	public void setResults(List<TransactionSearchResult> results) {
		this.results = results;
	}

}
