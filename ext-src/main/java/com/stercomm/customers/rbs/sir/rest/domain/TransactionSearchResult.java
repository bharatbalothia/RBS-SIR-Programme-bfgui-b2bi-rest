package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A transaction search Result
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class TransactionSearchResult {
	

	private int id;
	private int status;
	private String transactionID;
	private String type;
	private String timestamp;
	private String settleDate;
	private double settleAmount;
	private int workflowID;



	public TransactionSearchResult(int id, int status, String transactionID, String timestamp,
			 String type, int workflowID, String settleDate, double settleAmount) {

			
		this.id = id;
		this.status = status;
		this.timestamp = timestamp;
		this.settleAmount=settleAmount;
		this.settleDate=settleDate;
		this.transactionID = transactionID;
		this.workflowID = workflowID;
		this.type = type;
	}

	

	

	@Override
	public String toString() {
		return "TransactionSearchResult [id=" + id + ", status=" + status + ", transactionID=" + transactionID
				+ ", type=" + type + ", timestamp=" + timestamp + ", settleDate=" + settleDate + ", settleAmount="
				+ settleAmount + ", workflowID=" + workflowID + "]";
	}





	public String getTransactionID() {
		return transactionID;
	}


	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}


	public String getSettleDate() {
		return settleDate;
	}


	public void setSettleDate(String settleDate) {
		this.settleDate = settleDate;
	}


	public double getSettleAmount() {
		return settleAmount;
	}


	public void setSettleAmount(double settleAmount) {
		this.settleAmount = settleAmount;
	}


	public int getId() {
		return id;
	}

	public int getStatus() {
		return status;
	}

	

	public String getType() {
		return type;
	}

	
	public String getTimestamp() {
		return timestamp;
	}

	public int getWorkflowID() {
		return workflowID;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
	public void setType(String type) {
		this.type = type;
	}

	

	public void setTimestamp(String ts) {
		this.timestamp = ts;
	}

	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}

}
