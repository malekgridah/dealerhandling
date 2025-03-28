package com.billcom.dealerhandling.soap;


import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractRequest;
import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractResponse;
import com.billcom.dealerhandling.commons.beans.ContractListResponse;
import com.billcom.dealerhandling.commons.beans.GetPreactivatedContractsRequest;
import com.billcom.dealerhandling.commons.manager.CMSManager;
import com.billcom.dealerhandling.service.DealerService;

/**
 * @author malek.gridah
 * * @since 1.2
 */

public class DealerHandling {

    CMSManager cmsManager;

    public DealerHandling() {
        cmsManager = new CMSManager();
    }

    public ContractListResponse getPreactivatedContracts(GetPreactivatedContractsRequest getPreactivatedContractsRequest) {
        Class<DealerService> serviceClass = DealerService.class;
        return (ContractListResponse) cmsManager.callServiceMethod(serviceClass, "getPreactivatedContracts", getPreactivatedContractsRequest);
    }

    public CheckPreactivatedContractResponse checkPreactivatedContract(CheckPreactivatedContractRequest checkPreactivatedContractRequest) {
        Class<DealerService> serviceClass = DealerService.class;
        CheckPreactivatedContractResponse response = (CheckPreactivatedContractResponse) cmsManager.callServiceMethod(serviceClass, "checkPreactivatedContract", checkPreactivatedContractRequest);
        return response;
    }
}
