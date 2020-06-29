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
	private int workflowID;
	private String settleDate;
	private double settleAmount;
	private String transactionID;
	private boolean isoutbound;
	private String entity;
	private String ref;
	private String paymentBIC;
	private TransactionResultType rowType;
	private String filename;
	

	public TransactionSearchResultBuilder(int id, TransactionResultType type) {

		this.paymentID = id;
		this.rowType=type;
	}

	public TransactionSearchResult build() {

		if (rowType == TransactionResultType.SUMMARY) {
		
			return new TransactionSearchResult(paymentID, status, transactionID, type, workflowID, settleDate, settleAmount);
		}
		else {
			
			return new TransactionSearchResult(paymentID, status, transactionID, type, workflowID, settleDate, settleAmount, isoutbound, ref, filename, paymentBIC, entity);
		}
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

	
	public TransactionSearchResultBuilder withFilename(String filename) {

		this.filename = noNull(filename);

		return this;
	}
	
	public TransactionSearchResultBuilder withPaymentBIC(String paymentBIC) {

		this.paymentBIC = noNull(paymentBIC);

		return this;
	}
	
	public TransactionSearchResultBuilder withReference(String ref) {

		this.ref = noNull(ref);

		return this;
	}
	
	public TransactionSearchResultBuilder withEntity(String e) {

		this.entity = noNull(e);

		return this;
	}
	
	public TransactionSearchResultBuilder withIsoutbound(boolean b) {

		this.isoutbound = b;

		return this;
	}
	

	private String noNull(String s) {
		
		return (s!=null?s:"");
	}

}
