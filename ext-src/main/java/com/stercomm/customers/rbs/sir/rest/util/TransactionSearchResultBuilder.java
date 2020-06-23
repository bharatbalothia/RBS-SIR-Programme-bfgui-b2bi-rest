package com.stercomm.customers.rbs.sir.rest.util;

import javax.xml.bind.annotation.XmlRootElement;

import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;

/**
 * 
 * A builder implementation to create a FileSearchResult
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class TransactionSearchResultBuilder {

	private int paymentID;
	private int status;
	private String type;
	private String timestamp;
	private int workflowID;
	private String settleDate;
	private double settleAmount;
	private String transactionID;
	

	public TransactionSearchResultBuilder(int id) {

		this.paymentID = id;
	}

	public TransactionSearchResult build() {

		return new TransactionSearchResult(paymentID, status, transactionID, type, timestamp, workflowID, settleDate, settleAmount);
	}
	 
	public TransactionSearchResultBuilder withTimestamp(String timestamp) {

		this.timestamp = timestamp;

		return this;
	}

	public TransactionSearchResultBuilder withWorkflowID(int workflowID) {

		this.workflowID = workflowID;

		return this;
	}

	
	public TransactionSearchResultBuilder withSettleAmount(double settleAmount) {

		this.settleAmount = settleAmount;

		return this;
	}
	

	public TransactionSearchResultBuilder withType(String type) {

		this.type = noNull(type);

		return this;
	}
	public TransactionSearchResultBuilder withTransactionID(String transactionID) {

		this.transactionID = noNull(transactionID);

		return this;
	}

	public TransactionSearchResultBuilder withSettleDate(String settleDate) {
	

		this.settleDate = noNull(settleDate);

		return this;
	}
	
	

	public TransactionSearchResultBuilder withStatus(int status) {

		this.status = status;

		return this;
	}

	

	public TransactionSearchResultBuilder withPaymentID(int pid) {

		this.paymentID = pid;

		return this;
	}
	

	private String noNull(String s) {
		
		return (s!=null?s:"");
	}

}
