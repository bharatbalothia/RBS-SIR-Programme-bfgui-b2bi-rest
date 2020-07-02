package com.stercomm.customers.rbs.sir.rest.util;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A builder implementation to create a TransactionSearchWhereClause e.g. needs to work with different representations 
 * 
 * 
 */

@XmlRootElement
public class TransactionSearchWhereClauseBuilder {

	private StringBuffer buf;

	public TransactionSearchWhereClauseBuilder() {

		buf=new StringBuffer();
	}

	
	public String build() {
		
		return buf.toString();
}

	public TransactionSearchWhereClauseBuilder withWorkflowID(int workflowID) {

		buf.append("p.wf_id=" + workflowID);
		return this;
	}
	

	public TransactionSearchWhereClauseBuilder withSettlementBefore(String before) {
		
		
		buf.append("p.SETTLE_DATE<to_timestamp('" + before + "','yyyy-MM-dd\"T\"HH:mi:ss')");
		return this;
	}
	
	public TransactionSearchWhereClauseBuilder withSettlementAfter(String after) { 

		buf.append("p.SETTLE_DATE>to_timestamp('" + after + "','yyyy-MM-dd\"T\"HH:mi:ss')");
		return this;
	}

	public TransactionSearchWhereClauseBuilder withType(String type) {

		buf.append("upper(p.type) like upper('" + type + "%')");
		return this;
	}

	public TransactionSearchWhereClauseBuilder withReference(String reference) {

		buf.append("upper(p.reference) like upper('" + reference + "%')");
		
		return this;
	}
	
	public TransactionSearchWhereClauseBuilder withEntity(String entity) {

		buf.append("upper(e.entity) like upper('" + entity + "%')");
		
		return this;
	}
	
	
	public TransactionSearchWhereClauseBuilder withPaymentBIC(String paymentBIC) {

		buf.append("upper(p.PAYMENT_BIC) like upper('" + paymentBIC + "%')");
		
		return this;
	}
	
	
	public TransactionSearchWhereClauseBuilder and() {
		
		buf.append(" AND ");
		return this;
	}


	
	public TransactionSearchWhereClauseBuilder withStatus(int status) {

		buf.append("p.status=" + status);
		return this;
	}

	public TransactionSearchWhereClauseBuilder withService(String service) {

		buf.append("upper(p.service) like upper('" + service+"%')");
		return this;
	}

	public TransactionSearchWhereClauseBuilder withTransactionID(String id) {
		buf.append("upper(p.TRANSACTION_ID) lke upper('" + id +"%')");
		return this;
	}


}
