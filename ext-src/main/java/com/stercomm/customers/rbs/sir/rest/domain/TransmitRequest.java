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
		return "TransmitRequest [entityID=" + entityID + ", fileType=" + fileType + ", username=" + username + "]";
	}
	
	@NotNull(message = "Field cannot be null")
	@Min(value = 0,message = "Value must be < 0")
	private Integer entityID;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	@Pattern(regexp = "icf|iqf", flags = Pattern.Flag.CASE_INSENSITIVE, message="File type must be one of : icf or iqf")
	private String fileType;
	
	@NotNull(message = "Field cannot be null")
	@NotEmpty(message = "Field cannot be empty")
	private String username;
	
	public Integer getEntityID() {
		return entityID;
	}
	public void setEntityID(Integer entityID) {
		this.entityID = entityID;
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
