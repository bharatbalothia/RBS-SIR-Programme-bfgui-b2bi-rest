package com.stercomm.customers.rbs.sir.rest.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to return the result to the client
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class SRRLogs {

	private List<SRRLog> logs;
	
	public List<SRRLog> getLogs() {
		return logs;
	}

	@XmlElementWrapper
	@XmlElement(name = "logs")
	public List<SRRLog> getSRRLogs() {
		return logs;
	}

	public void setLogs(List<SRRLog> _logs) {
		this.logs = _logs;
	}

	
	/**
	 * 
	 * Add a single log
	 * @param newLog
	 */
	
	public void appendLog(SRRLog newLog) {

		// lazy init
		if (logs == null) {

			logs = new ArrayList<SRRLog>();
		}
		logs.add(newLog);
	}

	/**
	 * add all the gogs to this one
	 * 
	 * @param newLogs
	 */
	public void appendLogs(List<SRRLog> newLogs) {

		// lazy init
		if (logs == null) {

			logs = new ArrayList<SRRLog>();
		}
		logs.addAll(newLogs);
	}

}
