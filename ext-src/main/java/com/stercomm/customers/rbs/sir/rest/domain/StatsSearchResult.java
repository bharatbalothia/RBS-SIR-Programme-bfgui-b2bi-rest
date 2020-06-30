package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

import com.stercomm.customers.rbs.sir.rest.util.StatsResultType;

/**
 * 
 * A FileSearchResult
 * 
 * @author PETERGreaves
 *
 */

@XmlRootElement
public class StatsSearchResult {

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	public StatsSearchResult(int count, StatsResultType type) {

		this.count = count;
		this.type=type;

	}

	
	public StatsResultType getType() {
		return type;
	}


	private int count;
	private final StatsResultType type;
	

}
