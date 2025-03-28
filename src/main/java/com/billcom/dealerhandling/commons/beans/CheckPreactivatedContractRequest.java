package com.billcom.dealerhandling.commons.beans;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author malek.gridah
 */

@Data
@NoArgsConstructor
public class CheckPreactivatedContractRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String smNum;
    private String dirNum;
    private Long dealerId;
}
