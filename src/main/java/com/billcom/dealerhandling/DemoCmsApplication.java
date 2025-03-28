package com.billcom.dealerhandling;

import com.billcom.connectionpools.annotations.EnableServerConnectionPools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author malek.gridah
 * * @since 1.3
 */

@SpringBootApplication
@EnableServerConnectionPools
public class DemoCmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoCmsApplication.class, args);
    }

}
