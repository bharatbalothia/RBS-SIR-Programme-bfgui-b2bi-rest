package com.stercomm.customers.rbs.sir.rest.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.stercomm.customers.rbs.sir.rest.domain.TransactionSearchResult;
import com.stercomm.customers.rbs.sir.rest.util.TransactionResultType;
import com.stercomm.customers.rbs.sir.rest.util.TransactionSearchResultBuilder;

public class BaseRestServer {
	
	protected static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
	protected final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected Logger setupLogging(boolean logToConsole, String logFile) throws Exception {
		// Setup the logging
		System.setProperty("java.util.logging.SimpleFormatter.format", FORMAT_STRING);
		LogManager.getLogManager().reset();
		Logger thisLogger = Logger.getLogger(this.getClass().getName());
		if (logFile != null) {
			FileHandler logHandler = new FileHandler(logFile, 8 * 1024 * 1024, 2, true);
			logHandler.setFormatter(new SimpleFormatter());
			logHandler.setLevel(Level.FINEST);
			thisLogger.addHandler(logHandler);
		}

		if (logToConsole) {
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());
			consoleHandler.setLevel(Level.INFO);
			thisLogger.addHandler(consoleHandler);
		}

		thisLogger.setLevel(Level.INFO);
		return thisLogger;
	}

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
		long settleDate;
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
		 settleDate = row.getTimestamp(3).getTime();
		 settleAmt = row.getDouble(4);
		 type = row.getString(5);
		 wfid = row.getInt(7);
		 status = row.getInt(6);
		 fileID=row.getString(8);
		 docID=row.getString(9);
		 
		if (resultType == TransactionResultType.DETAIL) {
			 bic = row.getString(10);
			 entity = row.getString(11);
			 filename = row.getString(12);
			 ref = row.getString(13);
			 ob = row.getInt(14);
			 service=row.getString(15);
			 timestamp=row.getTimestamp(16).getTime();
			 formattedTimestamp = df.format(new java.util.Date(timestamp));
		}
		
		String formattedSettleDate = df.format(new java.util.Date(settleDate));
		
	

		TransactionSearchResult result = null;

		if (resultType == TransactionResultType.SUMMARY) {

			result = new TransactionSearchResultBuilder(paymentID, resultType).withTransactionID(transactionID)
					.withSettleAmount(settleAmt).withSettleDate(formattedSettleDate).withType(type).withStatus(status)
					.withWorkflowID(wfid).withFileID(fileID).withDocID(docID).build();
		} else if (resultType == TransactionResultType.DETAIL) {

			result = new TransactionSearchResultBuilder(paymentID, resultType).withTransactionID(transactionID)
					.withSettleAmount(settleAmt).withSettleDate(formattedSettleDate).withType(type).withStatus(status)
					.withWorkflowID(wfid).withEntity(entity).withPaymentBIC(bic).withFilename(filename)
					.withReference(ref).withIsoutbound(ob == 1 ? true : false).withFileID(fileID).withService(service).withTimestamp(formattedTimestamp).withDocID(docID).build();
		}
		return result;

	}
}
