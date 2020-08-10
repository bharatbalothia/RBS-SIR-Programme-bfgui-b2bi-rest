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
	
	public FileSearchWhereClauseBuilder withBundleID( int id) {

		buf.append("bundle_id=" + id);
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
	
	public FileSearchWhereClauseBuilder withBPState(String state) {
		
		/*
		 * rbs.sfg.sct.bundle.search.bpState.red= and bun.status < 0 
		rbs.sfg.sct.bundle.search.bpState.green= and (bun.status=100 or bun.status=200) 
		rbs.sfg.sct.bundle.search.bpState.amber= and bun.status > 0 and bun.status != 100 and bun.status != 200 
		 */
		if ("red".equalsIgnoreCase(state)) {
			buf.append("status < 0");
			
		} else if ("amber".equalsIgnoreCase(state)) {
			
			buf.append("status > 0 and status != 100 and status !=200");
		}
		else if ("green".equalsIgnoreCase(state)) {
			buf.append("status = 100 or status = 200");	
		}
		else  {
			buf.append("status is not null");
			
		}
		
		
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
