package com.stercomm.customers.rbs.sir.rest.domain;

import java.util.Arrays;

public class FileSearchWhereClause {
	
	private int id;
	private int status;
	private String filename;
	private String[] errorCode;
	private String reference;
	private String type;
	private String service;
	private String lastUpdated;
	private int workflowID;
	private int entityID;
	private int messageID;
	private boolean isOutbound;
	private boolean isOverride;
	
	
	
	
	@Override
	public String toString() {
		return "FileSearchWhereClause [id=" + id + ", status=" + status + ", filename=" + filename + ", errorCode="
				+ Arrays.toString(errorCode) + ", reference=" + reference + ", type=" + type + ", service=" + service
				+ ", lastUpdated=" + lastUpdated + ", workflowID=" + workflowID + ", entityID=" + entityID
				+ ", messageID=" + messageID + ", isOutbound=" + isOutbound + ", isOverride=" + isOverride + ", docID="
				+ docID + "]";
	}
	
	
	public FileSearchWhereClause(int id, String[] errorCode, int status, String filename, String reference,
			String lastUpdated, String service, int workflowID, String type, int entityID, int messageID,
			boolean isOutbound, boolean isOverride, String docID) {
		super();
		this.id = id;
		this.status = status;
		this.filename = filename;
		this.errorCode = errorCode;
		this.reference = reference;
		this.type = type;
		this.service = service;
		this.lastUpdated = lastUpdated;
		this.workflowID = workflowID;
		this.entityID = entityID;
		this.messageID = messageID;
		this.isOutbound = isOutbound;
		this.isOverride = isOverride;
		this.docID = docID;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String[] getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String[] errorCode) {
		this.errorCode = errorCode;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public int getWorkflowID() {
		return workflowID;
	}
	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}
	public int getEntityID() {
		return entityID;
	}
	public void setEntityID(int entityID) {
		this.entityID = entityID;
	}
	public int getMessageID() {
		return messageID;
	}
	public void setMessageID(int messageID) {
		this.messageID = messageID;
	}
	public boolean isOutbound() {
		return isOutbound;
	}
	public void setOutbound(boolean isOutbound) {
		this.isOutbound = isOutbound;
	}
	public boolean isOverride() {
		return isOverride;
	}
	public void setOverride(boolean isOverride) {
		this.isOverride = isOverride;
	}
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	private String docID;

}
