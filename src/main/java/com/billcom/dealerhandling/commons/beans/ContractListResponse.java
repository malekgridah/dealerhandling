package com.billcom.dealerhandling.commons.beans;

import com.lhs.ws.beans.v2.ContractsSearchBeanOut;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @author malek.gridah
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContractListResponse extends OperationResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContractsSearchBeanOut contractList;
}
