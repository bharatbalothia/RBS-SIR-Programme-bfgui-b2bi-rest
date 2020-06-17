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
	private String lastUpdated;
	private int workflowID;
	private int entityID;
	private long messageID;
	private boolean isOutbound;
	private boolean isOverride;
	private String docID;

	public FileSearchResultBuilder(int id) {

		this.id = id;
	}

	public FileSearchResult build() {

		return new FileSearchResult(id, errorCode,status,filename,reference,lastUpdated, service, workflowID,type, entityID, messageID, isOverride, isOutbound, docID);
	}

	public FileSearchResultBuilder withWorkflowID(int workflowID) {

		this.workflowID = workflowID;

		return this;
	}
	public FileSearchResultBuilder withOverride(boolean isOverride) {

		this.isOverride = isOverride;

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

	public FileSearchResultBuilder withLastUpdated(String lu) {

		this.lastUpdated = lu;

		return this;
	}

	public FileSearchResultBuilder withType(String type) {

		this.type = type;

		return this;
	}

	public FileSearchResultBuilder withReference(String reference) {

		this.reference = reference;

		return this;
	}
	
	public FileSearchResultBuilder withDocID(String docID) {

		this.docID = docID;

		return this;
	}

	public FileSearchResultBuilder withErrorCode(String errorCode) {

		this.errorCode = errorCode;

		return this;
	}

	public FileSearchResultBuilder withFilename(String filename) {

		this.filename = filename;

		return this;
	}

	public FileSearchResultBuilder withStatus(int status) {

		this.status = status;

		return this;
	}

	public FileSearchResultBuilder withService(String service) {

		this.service = service;

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

}
