package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

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
	private String settleDate;
	private double settleAmount;
	private int workflowID;
	private String entity;
	private String paymentBIC;
	private String filename;
	private String reference;
	private Boolean isoutbound;  
	


	private String fileID;
	private String service;
	private String timestamp;
	private String docID;


	

	

	
	@Override
	public String toString() {
		return "TransactionSearchResult [id=" + id + ", status=" + status + ", transactionID=" + transactionID
				+ ", type=" + type + ", settleDate=" + settleDate + ", settleAmount=" + settleAmount + ", workflowID="
				+ workflowID + ", entity=" + entity + ", paymentBIC=" + paymentBIC + ", filename=" + filename
				+ ", reference=" + reference + ", isoutbound=" + isoutbound + ", fileID=" + fileID + ", service="
				+ service + ", timestamp=" + timestamp + ", docID=" + docID + "]";
	}

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public TransactionSearchResult(int id, int status, String transactionID, String type,
			  int workflowID, String settleDate, double settleAmount, String fileID, String docID) {
			
		this.id = id;
		this.status = status;	
		this.settleAmount=settleAmount;
		this.settleDate=settleDate;
		this.transactionID = transactionID;
		this.workflowID = workflowID;
		this.type = type;
		this.fileID=fileID;
		this.docID=docID;
	}

	public TransactionSearchResult(int id, int status, String transactionID, String type,
			  int workflowID, String settleDate, double settleAmount, String fileID, boolean isoutbound, String ref, String filename, String paymentBIC, String entity, String service, String timestamp, String docID) {
			
		this(id, status, transactionID, type, workflowID, settleDate, settleAmount, fileID, docID);
		this.isoutbound = isoutbound;
		this.reference  = ref;	
		this.filename=filename;
		this.paymentBIC=paymentBIC;
		this.entity = entity;
		this.service=service;
		this.timestamp=timestamp;
		
	}
	

	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getEntity() {
		return entity;
	}





	public void setEntity(String entity) {
		this.entity = entity;
	}




	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getPaymentBIC() {
		return paymentBIC;
	}





	public void setPaymentBIC(String paymentBIC) {
		this.paymentBIC = paymentBIC;
	}




	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getFilename() {
		return filename;
	}





	public void setFilename(String filename) {
		this.filename = filename;
	}




	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getReference() {
		return reference;
	}





	public void setReference(String reference) {
		this.reference = reference;
	}




	@JsonSerialize(include = Inclusion.NON_NULL)
	public Boolean isIsoutbound() {
		return isoutbound;
	}





	public void setIsoutbound(boolean isoutbound) {
		this.isoutbound = isoutbound;
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



	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}

}
