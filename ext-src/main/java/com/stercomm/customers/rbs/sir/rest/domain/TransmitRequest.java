package com.stercomm.customers.rbs.sir.rest.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TransmitRequest {
	
	
	
	@Override
	public String toString() {
		return "TransmitRequest [entityID=" + entity + ", fileType=" + fileType + ", username=" + username + "]";
	}
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String entity;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	@Pattern(regexp = "icf|iqf", flags = Pattern.Flag.CASE_INSENSITIVE, message="File type must be one of : icf or iqf")
	private String fileType;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String username;
	
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

}
