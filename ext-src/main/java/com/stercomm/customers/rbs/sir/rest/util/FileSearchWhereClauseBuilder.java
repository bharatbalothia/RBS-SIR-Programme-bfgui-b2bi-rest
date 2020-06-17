package com.stercomm.customers.rbs.sir.rest.util;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import com.stercomm.customers.rbs.sir.rest.domain.FileSearchWhereClause;

/**
 * 
 * A builder implementation to create a FileSearchWhereClause e.g. needs to work with different representations 
 * 
 * 
 */

@XmlRootElement
public class FileSearchWhereClauseBuilder {

	private StringBuffer buf;

	public FileSearchWhereClauseBuilder() {

		buf=new StringBuffer("WHERE ");
	}

	
	public String build() {
		
		return buf.toString();
}

	public FileSearchWhereClauseBuilder withWorkflowID(int workflowID) {

		buf.append("workflowid=" + workflowID);
		return this;
	}
	public FileSearchWhereClauseBuilder withOverride(boolean isOverride) {

		buf.append("isoverride=" + (isOverride?"1":"0"));	
		return this;
	}
	public FileSearchWhereClauseBuilder withOutbound(boolean isOutbound) {

		buf.append("isoutbound=" + (isOutbound?"1":"0"));
		return this;
	}

	public FileSearchWhereClauseBuilder withMessageID(long messageID) {

		buf.append("message_id=" + messageID);
		return this;
	}

	public FileSearchWhereClauseBuilder withLastUpdated(String lu) {

		buf.append("timestamp=" + lu);
		return this;
	}
	
	public FileSearchWhereClauseBuilder before(String before) {
		
		
		buf.append("btimestamp<to_timestamp('" + before + "','yyyy-MM-ddTHH:mmXXX')");
		return this;
	}
	
	public FileSearchWhereClauseBuilder after(String after) {
		
		buf.append("btimestamp>to_timestamp('" + after + "','yyyy-MM-ddTHH:mmXXX')");
		return this;
	}

	public FileSearchWhereClauseBuilder withType(String type) {

		buf.append("btype='" + type + "'");
		return this;
	}

	public FileSearchWhereClauseBuilder withReference(String reference) {

		buf.append("reference='" + reference + "'");
		return this;
	}
	
	public FileSearchWhereClauseBuilder withDocID(String docID) {

		buf.append("doc_id='" + docID + "'");
		return this;
	}
	
	public FileSearchWhereClauseBuilder and() {
		
		buf.append(" AND ");
		return this;
	}

	public FileSearchWhereClauseBuilder withErrorCode(String errorCode) {

		buf.append("error='" + errorCode + "'");
		return this;
	}

	public FileSearchWhereClauseBuilder withFilename(String filename) {

		buf.append("filename like '%" + filename + "%'");
		return this;
	}

	public FileSearchWhereClauseBuilder withStatus(int status) {

		buf.append("status=" + status);
		return this;
	}

	public FileSearchWhereClauseBuilder withService(String service) {

		buf.append("service='" + service+"'");
		return this;
	}

	public FileSearchWhereClauseBuilder withID(int id) {
		buf.append("bundle_id=" + id);
		return this;
	}
	public FileSearchWhereClauseBuilder withEntityID(int entityID) {

		buf.append("entity_id=" + entityID);
		return this;
	}

}
