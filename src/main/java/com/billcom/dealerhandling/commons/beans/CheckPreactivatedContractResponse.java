package com.billcom.dealerhandling.commons.beans;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author malek.gridah
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class CheckPreactivatedContractResponse extends OperationResponse {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean isContractFound;
    private boolean isPreactivatedContract;
    private String smNum;
    private String dirNum;
    private String rpDes;

    public void setIsContractFound(boolean isContractFound) {
        this.isContractFound = isContractFound;
    }

    public void setIsPreactivatedContract(boolean isPreactivatedContract) {
        this.isPreactivatedContract = isPreactivatedContract;
    }

}
