package com.stercomm.customers.rbs.sir.rest.util;

import javax.xml.bind.annotation.XmlRootElement;

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

		buf.append("wf_id=" + workflowID);
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

	public FileSearchWhereClauseBuilder before(String before) {
		
		
		buf.append("btimestamp<to_timestamp('" + before + "','yyyy-MM-dd\"T\"HH24:mi:ss')");
		return this;
	}
	
	public FileSearchWhereClauseBuilder after(String after) { 

		buf.append("btimestamp>to_timestamp('" + after + "','yyyy-MM-dd\"T\"HH24:mi:ss')");
		return this;
	}

	public FileSearchWhereClauseBuilder withType(String type) {

		buf.append("upper(btype) like upper('" + type + "%')");
		return this;
	}

	public FileSearchWhereClauseBuilder withReference(String reference) {

		buf.append("upper(reference) like upper('" + reference + "%')");
		
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

		buf.append("upper(filename) like upper('" + filename + "%')");
		return this;
	}

	public FileSearchWhereClauseBuilder withStatus(int status) {

		buf.append("status=" + status);
		return this;
	}

	public FileSearchWhereClauseBuilder withService(String service) {

		buf.append("upper(service) like upper('" + service+"%')");
		return this;
	}

	public FileSearchWhereClauseBuilder withID(int id) {
		buf.append("bundle_id=" + id);
		return this;
	}
	public FileSearchWhereClauseBuilder withEntityID(long entityID) {

		buf.append("entity_id=" + entityID);
		return this;
	}

}
