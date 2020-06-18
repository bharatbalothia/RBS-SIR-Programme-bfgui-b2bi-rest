package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A FileSearchResult
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class FileSearchResult {

	public FileSearchResult(int id, String errorCode, int status, String filename, String reference, String timestamp,
			String service, int workflowID, String type, int entityID, long messageID, boolean isOverride, boolean isOutbound, String docID) {

		this.id = id;
		this.errorCode = errorCode;
		this.status = status;
		this.filename = filename;
		this.reference = reference;
		this.timestamp = timestamp;
		this.service = service;
		this.workflowID = workflowID;
		this.type = type;
		this.entityID = entityID;
		this.messageID = messageID;
		this.isOverride=isOverride;
		this.isOutbound=isOutbound;
		this.docID=docID;

	}

	@Override
	public String toString() {
		return "FileSearchResult [id=" + id + ", status=" + status + ", filename=" + filename + ", errorCode="
				+ errorCode + ", reference=" + reference + ", type=" + type + ", service=" + service + ", timestamp="
				+ timestamp + ", workflowID=" + workflowID + ", entity id = " + entityID + ", messageID=" + messageID
				+ ", isOverride=" + isOverride + ", isOutbound=" + isOutbound + ", doc id=" +docID+"]";
	}

	private int id;
	private int status;
	private String filename;
	private String errorCode;
	private String reference;
	private String type;
	private String service;
	private String timestamp;
	private int workflowID;
	private int entityID;
	private long messageID;
	private boolean isOutbound;
	private boolean isOverride;
	private String docID;

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

	public long getMessageID() {
		return messageID;
	}

	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}

	public int getEntityID() {
		return entityID;
	}

	public void setEntityID(int entityID) {
		this.entityID = entityID;
	}

	public int getId() {
		return id;
	}

	public int getStatus() {
		return status;
	}

	public String getFilename() {
		return filename;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getReference() {
		return reference;
	}

	public String getType() {
		return type;
	}

	public String getService() {
		return service;
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

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setTimestamp(String ts) {
		this.timestamp = ts;
	}

	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}

}
