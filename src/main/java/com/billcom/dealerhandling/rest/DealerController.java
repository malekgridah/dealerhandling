package com.billcom.dealerhandling.rest;

import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractRequest;
import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractResponse;
import com.billcom.dealerhandling.commons.beans.ContractListResponse;
import com.billcom.dealerhandling.commons.beans.GetPreactivatedContractsRequest;
import com.billcom.dealerhandling.commons.manager.CMSManager;
import com.billcom.dealerhandling.service.DealerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author malek.gridah
 * * @since 1.0
 */

@RestController
@RequestMapping("api")
public class DealerController {

    CMSManager cmsManager;

    public DealerController() {
        cmsManager = new CMSManager();

    }

    @PostMapping("getPreactivatedContracts")
    public ResponseEntity<ContractListResponse> getPreactivatedContracts(@RequestBody GetPreactivatedContractsRequest getPreactivatedContractsRequest) {
        Class<DealerService> serviceClass = DealerService.class;
        try {
            ContractListResponse response = (ContractListResponse) cmsManager.callServiceMethod(serviceClass, "getPreactivatedContracts", getPreactivatedContractsRequest);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("checkPreactivatedContract")
    public ResponseEntity<CheckPreactivatedContractResponse> checkPreactivatedContract(@RequestBody CheckPreactivatedContractRequest checkPreactivatedContractRequest) {
        Class<DealerService> serviceClass = DealerService.class;
        try {
            CheckPreactivatedContractResponse response =
                    (CheckPreactivatedContractResponse) cmsManager.callServiceMethod(
                            serviceClass, "checkPreactivatedContract", checkPreactivatedContractRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
