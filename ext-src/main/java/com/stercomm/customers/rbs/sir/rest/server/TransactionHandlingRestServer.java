package com.stercomm.customers.rbs.sir.rest.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;
import com.stercomm.customers.rbs.sir.rest.util.TransactionResultType;
import com.stercomm.customers.rbs.sir.rest.util.TransactionSearchResultBuilder;

public class TransactionHandlingRestServer extends BaseRestServer{
	
	
	/**
	 * Create a TransactionSearchResult object from a Row
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	protected TransactionSearchResult toTransactionSearchResult(ResultSet row, TransactionResultType resultType)
			throws SQLException {

		// returned for summary

		int paymentID;
		String transactionID=null;
		long settleDate=0L;
		double settleAmt;
		String type=null;
		int wfid;
		int status;
		String fileID;
		
		// returned for detail only (in addition to summary)
		String bic=null;
		String entity=null;
		String filename=null;
		String ref=null;
		int ob = 0;
		String service=null;
		long timestamp;
		String formattedTimestamp=null;
		String docID;
		
		
		// p.payment_id, p.TRANSACTION_ID, p.SETTLE_DATE, p.SETTLE_AMT, 
		// p.type, p.status,p.wf_id, p.payment_bic, e.entity, b.filename, p.reference, p.isoutbound
		
		 paymentID = row.getInt(1);
		 transactionID = row.getString(2);
		 Timestamp sdTimestamp =row.getTimestamp(3); 
		 if (null!=sdTimestamp) {
			 settleDate = sdTimestamp.getTime();
		 }
		 settleAmt = row.getDouble(4);
		 type = row.getString(5);
		 wfid = row.getInt(6);
		 status = row.getInt(7);
		 fileID=row.getString(8);
		 docID=row.getString(9);
		 timestamp=row.getTimestamp(10).getTime();
		 formattedTimestamp = df.format(new java.util.Date(timestamp));
		 
		if (resultType == TransactionResultType.DETAIL) {
			 bic = row.getString(11);
			 entity = row.getString(12);
			 filename = row.getString(13);
			 ref = row.getString(14);
			 ob = row.getInt(15);
			 service=row.getString(16); 
		}
		
		
		
		// settlement data might be 0 here
		String formattedSettleDate = "";	
		if (settleDate!=0) {		
			formattedSettleDate=df.format(new java.util.Date(settleDate));
		}
		

		TransactionSearchResult result = null;

		if (resultType == TransactionResultType.SUMMARY) {

			result = new TransactionSearchResultBuilder(paymentID, resultType).withTransactionID(transactionID)
					.withSettleAmount(settleAmt).withSettleDate(formattedSettleDate).withType(type).withStatus(status)
					.withWorkflowID(wfid).withFileID(fileID).withDocID(docID).withTimestamp(formattedTimestamp).build();
		} else if (resultType == TransactionResultType.DETAIL) {

			result = new TransactionSearchResultBuilder(paymentID, resultType).withTransactionID(transactionID)
					.withSettleAmount(settleAmt).withSettleDate(formattedSettleDate).withType(type).withStatus(status)
					.withWorkflowID(wfid).withEntity(entity).withPaymentBIC(bic).withFilename(filename)
					.withReference(ref).withIsoutbound(ob == 1 ? true : false).withFileID(fileID).withService(service).withTimestamp(formattedTimestamp).withDocID(docID).build();
		}
		return result;

	}
}
