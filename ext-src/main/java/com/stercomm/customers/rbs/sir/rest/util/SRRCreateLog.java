package com.stercomm.customers.rbs.sir.rest.util;

public class SRRCreateLog {
	
	@Override
	public String toString() {
		return "SRRCreateLog [entityName=" + entityName + ", successOnValidate=" + successOnValidate
				+ ", successOnCreate=" + successOnCreate + ", failCause=" + failCause + "]";
	}
	private String entityName;
	private boolean successOnValidate;
	private boolean successOnCreate;
	
	private String failCause;
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	public boolean isSuccessOnValidate() {
		return successOnValidate;
	}
	public void setSuccessOnValidate(boolean successOnValidate) {
		this.successOnValidate = successOnValidate;
	}
	public boolean isSuccessOnCreate() {
		return successOnCreate;
	}
	public void setSuccessOnCreate(boolean successOnCreate) {
		this.successOnCreate = successOnCreate;
	}
	public String getFailCause() {
		return failCause;
	}
	public void setFailCause(String failCause) {
		this.failCause = failCause;
	}
	

}
