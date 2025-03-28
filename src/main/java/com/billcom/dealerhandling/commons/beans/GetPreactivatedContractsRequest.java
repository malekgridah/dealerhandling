package com.billcom.dealerhandling.commons.beans;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author malek.gridah
 */

@Data
public class GetPreactivatedContractsRequest implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean returnsAll = false;
    private Long maxNbContracts;

}
