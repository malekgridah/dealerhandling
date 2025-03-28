package com.billcom.dealerhandling.commons.beans;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author malek.gridah
 */

@Data
public class OperationResponse implements Serializable {

    
    @Serial
    private static final long serialVersionUID = 1L;

    public OperationResponse() {
    }

    private Boolean isSuccessful;
    private String bscsErrorCode;
    private String comment;


    public void setIsSuccessful(Boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }
    
    @Override
	public String toString() {
		return "BaseWSResponse [bscsErrorCode=" + bscsErrorCode + ", comment="
				+ comment + ", isSuccessful=" + isSuccessful + "]";
	}
}

