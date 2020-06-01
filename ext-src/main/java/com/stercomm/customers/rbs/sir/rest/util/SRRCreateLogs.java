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
public class SRRCreateLogs {

	private List<SRRCreateLog> logs;
	
	public List<SRRCreateLog> getLogs() {
		return logs;
	}

	@XmlElementWrapper
	@XmlElement(name = "logs")
	public List<SRRCreateLog> getSRRCreateLogs() {
		return logs;
	}

	public void setLogs(List<SRRCreateLog> _logs) {
		this.logs = _logs;
	}

	
	/**
	 * 
	 * Add a single log
	 * @param newLog
	 */
	
	public void appendLog(SRRCreateLog newLog) {

		// lazy init
		if (logs == null) {

			logs = new ArrayList<SRRCreateLog>();
		}
		logs.add(newLog);
	}

	/**
	 * add all the gogs to this one
	 * 
	 * @param newLogs
	 */
	public void appendLogs(List<SRRCreateLog> newLogs) {

		// lazy init
		if (logs == null) {

			logs = new ArrayList<SRRCreateLog>();
		}
		logs.addAll(newLogs);
	}

}
