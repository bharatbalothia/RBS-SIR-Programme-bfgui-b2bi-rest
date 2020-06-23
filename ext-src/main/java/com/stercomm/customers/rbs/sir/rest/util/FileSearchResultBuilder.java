package com.stercomm.customers.rbs.sir.rest.util;

import javax.xml.bind.annotation.XmlRootElement;

import com.stercomm.customers.rbs.sir.rest.domain.FileSearchResult;

/**
 * 
 * A builder implementation to create a FileSearchResult
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class FileSearchResultBuilder {

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
	private int transactionTotal;

	public FileSearchResultBuilder(int id) {

		this.id = id;
	}

	public FileSearchResult build() {

		return new FileSearchResult(id, errorCode,status,filename,reference,timestamp, service, workflowID,type, entityID, messageID, isOverride, isOutbound, docID, transactionTotal);
	}
	 
	public FileSearchResultBuilder withTimestamp(String timestamp) {

		this.timestamp = timestamp;

		return this;
	}

	public FileSearchResultBuilder withWorkflowID(int workflowID) {

		this.workflowID = workflowID;

		return this;
	}
	public FileSearchResultBuilder withOverride(boolean isOverride) {

		this.isOverride = isOverride;

		return this;
	}
	
	public FileSearchResultBuilder withTransactionTotal(int total) {

		this.transactionTotal = total;

		return this;
	}
	
	public FileSearchResultBuilder withOutbound(boolean isOutbound) {

		this.isOutbound = isOutbound;

		return this;
	}

	public FileSearchResultBuilder withMessageID(long messageID) {

		this.messageID = messageID;

		return this;
	}

	

	public FileSearchResultBuilder withType(String type) {

		this.type = noNull(type);

		return this;
	}

	public FileSearchResultBuilder withReference(String reference) {
	

		this.reference = noNull(reference);

		return this;
	}
	
	public FileSearchResultBuilder withDocID(String docID) {

		
		this.docID = noNull(docID);
		return this;
	}

	public FileSearchResultBuilder withErrorCode(String errorCode) {
		

		this.errorCode = noNull(errorCode);

		return this;
	}

	public FileSearchResultBuilder withFilename(String filename) {

		this.filename = noNull(filename);

		return this;
	}

	public FileSearchResultBuilder withStatus(int status) {

		this.status = status;

		return this;
	}

	public FileSearchResultBuilder withService(String service) {

		this.service = noNull(service);

		return this;
	}

	public FileSearchResultBuilder withID(int id) {

		this.id = id;

		return this;
	}
	public FileSearchResultBuilder withEntityID(int entityID) {

		this.entityID = entityID;

		return this;
	}

	private String noNull(String s) {
		
		return (s!=null?s:"");
	}

}
