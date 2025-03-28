package com.billcom.dealerhandling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author malek.gridah
 * @since 1.1
 */

@Data
@Component
@ConfigurationProperties("dealers.config")
public class DealerConfig {

    private Long preactivatedMaxContracts;
    private String preactivatedCustomerLastNamePrefix;
    private String preactivatedRatePlanPrefixes;
    private String preactivatedDealers;

    public String[] getPreactivatedRatePlanPrefixes() {
        return preactivatedRatePlanPrefixes.split(",");
    }

    public String[] getPreactivatedDealers() {
        return preactivatedDealers.split(",");
    }
}
