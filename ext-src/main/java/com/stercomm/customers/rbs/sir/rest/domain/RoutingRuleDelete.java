package com.stercomm.customers.rbs.sir.rest.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RoutingRuleDelete {
	
	private String entityName;

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	

}
