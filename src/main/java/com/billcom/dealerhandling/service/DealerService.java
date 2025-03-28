package com.billcom.dealerhandling.service;

import com.billcom.connectionpools.config.SpringContext;
import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractRequest;
import com.billcom.dealerhandling.commons.beans.CheckPreactivatedContractResponse;
import com.billcom.dealerhandling.commons.beans.ContractListResponse;
import com.billcom.dealerhandling.commons.beans.GetPreactivatedContractsRequest;
import com.billcom.dealerhandling.commons.connection.CMSConnection;
import com.billcom.dealerhandling.commons.connection.CMSException;
import com.billcom.dealerhandling.commons.manager.IService;
import com.billcom.dealerhandling.config.DealerConfig;
import com.lhs.ws.beans.v2.*;
import com.lhs.ws.beans.v2.contract_devices_read.PortsBeanOut;
import com.lhs.ws.beans.v2.contract_services_read.ServicesBeanOut;
import com.lhs.ws.beans.v2.contracts_search.ContractsBeanOut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @author malek.gridah
 * * @since 1.2
 */

public class DealerService implements IService {
    private static final Logger logger = LogManager.getLogger(DealerService.class);
    private static final String ANY_CHARACTER_PATTERN = "*";
    private static DealerService instance = null;
    DealerConfig config;

    protected DealerService() {
        config = SpringContext.getBean(DealerConfig.class);
    }

    public static DealerService getInstance() {
        if (instance == null) {
            instance = new DealerService();
        }
        return instance;
    }

    public ContractListResponse getPreactivatedContracts(CMSConnection connection, GetPreactivatedContractsRequest getPreactivatedContractsRequest) throws CMSException {
        logger.info("===> getPreactivatedContracts()");
        ContractListResponse response = new ContractListResponse();
        Long nbContracts;

        if (getPreactivatedContractsRequest.getReturnsAll() == Boolean.TRUE) {
            nbContracts = config.getPreactivatedMaxContracts();
        } else {
            nbContracts = getPreactivatedContractsRequest.getMaxNbContracts();
            if (nbContracts == null) {
                throw new CMSException("Arguments error", "The maximum number of contracts must be populated if returnsAll flag is set to null or false");
            }
        }

        ContractsSearchBeanIn csbi = new ContractsSearchBeanIn();
        csbi.setAdrLname(config.getPreactivatedCustomerLastNamePrefix() + ANY_CHARACTER_PATTERN);
        csbi.setSrchCount(nbContracts);
        ContractsSearchBeanOut out = (ContractsSearchBeanOut)connection.executeAndConvertOutput("CONTRACTS.SEARCH", csbi, ContractsSearchBeanOut.class);
        ContractsBeanOut[] var10;
        int var9 = (var10 = out.getContracts()).length;

        for(ContractsBeanOut contract : out.getContracts()) {
            ContractDevicesReadBeanIn devicesIn = new ContractDevicesReadBeanIn();
            devicesIn.setCoId(contract.getCoId());
            ContractDevicesReadBeanOut devicesOut = (ContractDevicesReadBeanOut)connection.executeAndConvertOutput("CONTRACT_DEVICES.READ", devicesIn, ContractDevicesReadBeanOut.class);
            contract.setSmNum(devicesOut.getCoStmedno());
            PortsBeanOut[] ports = devicesOut.getPorts();
            if (ports != null && ports.length != 0) {
                contract.setPortNum(ports[0].getPortNum());
            }
        }

        response.setContractList(out);
        logger.info("<=== getPreactivatedContracts()");
        return response;
    }

    public CheckPreactivatedContractResponse checkPreactivatedContract(CMSConnection connection, CheckPreactivatedContractRequest checkPreactivatedContractRequest) throws CMSException {
        String smNum = checkPreactivatedContractRequest.getSmNum();
        String dirNum = checkPreactivatedContractRequest.getDirNum();
        Long dealerId = checkPreactivatedContractRequest.getDealerId();

        logger.info("=> checkPreActivatedContract() :\nsmNum = {}\ndirNum = {}\ndealerId = {}", smNum, dirNum, dealerId);

        CheckPreactivatedContractResponse response = new CheckPreactivatedContractResponse();
        response.setIsContractFound(false);
        response.setIsPreactivatedContract(false);
        response.setSmNum(smNum);
        response.setDirNum(dirNum);

        if (smNum == null && dirNum == null) {
            throw new CMSException("Invalid parameter", "At least SIM or directory number must be provided");
        } else {
            ContractsSearchBeanIn csbi = new ContractsSearchBeanIn();
            csbi.setSmNum(smNum != null ? smNum + "*" : null);
            csbi.setDirnum(dirNum);
            csbi.setSearcher("ContractSearchWithoutHistory");
            ContractsSearchBeanOut csbo = (ContractsSearchBeanOut)connection
                    .executeAndConvertOutput("CONTRACTS.SEARCH", csbi, ContractsSearchBeanOut.class);
            ContractsBeanOut[] contracts = csbo.getContracts();

            if (contracts != null && contracts.length != 0) {
                logger.info(contracts.length + " contract(s) found: need to check if one is preActivated...");
                response.setIsContractFound(true);
                String preActivatedFilterPattern = config.getPreactivatedCustomerLastNamePrefix();
                ContractsBeanOut preactivatedContract = null;

                for (ContractsBeanOut contract : contracts) {
                    String adrLname = contract.getAdrLname();
                    if (adrLname != null && adrLname.startsWith(preActivatedFilterPattern)) {
                        preactivatedContract = contract;
                        break;
                    }
                }

                if (preactivatedContract == null) {
                    logger.info("No preActivated contract in contracts found");
                    response.setIsPreactivatedContract(false);
                } else {
                    logger.info("PreActivated contract found with coId = {}", preactivatedContract.getCoId());
                    RateplansReadBeanIn rrbi = new RateplansReadBeanIn();
                    rrbi.setRpcode(preactivatedContract.getRpcode());
                    RateplansReadBeanOut rrbo = (RateplansReadBeanOut)connection
                            .executeAndConvertOutput("RATEPLANS.READ", rrbi, RateplansReadBeanOut.class);

                    String rpDes = rrbo.getNumRp()[0].getRpDes();
                    logger.info("rpCode = {}, rpDes = {}", preactivatedContract.getRpcode(), rpDes);
                    boolean rpDesMatches = false;
                    List<String> preactivatedRatePlanPrefixes = Arrays.asList(config.getPreactivatedRatePlanPrefixes());


                    ContractServicesReadBeanIn servicesReadBeanIn = new ContractServicesReadBeanIn();
                    servicesReadBeanIn.setCoId(preactivatedContract.getCoId());

                    ContractServicesReadBeanOut servicesReadBeanOut = (ContractServicesReadBeanOut) connection
                            .executeAndConvertOutput("CONTRACT_SERVICES.READ", servicesReadBeanIn, ContractServicesReadBeanOut.class);


                    List<String> matchingServices = Arrays.stream(servicesReadBeanOut.getServices())
                            .map(ServicesBeanOut::getSncodePub)
                            .filter(dc -> preactivatedRatePlanPrefixes.stream()
                                    .anyMatch(prefix -> dc.toLowerCase().startsWith(prefix.toLowerCase()))) // Ignores case
                            .toList();

                    if (preactivatedRatePlanPrefixes.isEmpty()) {
                        rpDesMatches = true;
                    } else if (!matchingServices.isEmpty()){
                        logger.info("Service description matches configured service offers prefix.");
                        rpDesMatches = true;
                    }


                    logger.info("The following preActivated service offers prefixes are configured: {}",
                            Arrays.toString(config.getPreactivatedRatePlanPrefixes()));


                    logger.info("rpDesMatches = {}", rpDesMatches);
                    ContractDevicesReadBeanIn cdrbi = new ContractDevicesReadBeanIn();
                    cdrbi.setCoId(preactivatedContract.getCoId());
                    ContractDevicesReadBeanOut cdrbo = (ContractDevicesReadBeanOut)connection
                            .executeAndConvertOutput("CONTRACT_DEVICES.READ", cdrbi, ContractDevicesReadBeanOut.class);
                    Long coStmedDealerId = cdrbo.getCoStmedDealerId();
                    logger.info("Contract dealer id = {}", coStmedDealerId);
                    boolean dealerMatches = false;
                    String[] preactivatedDealers = config.getPreactivatedDealers();
                    logger.info("The following preActivated dealers are configured: {} + input dealer = {}",
                            Arrays.toString(preactivatedDealers), dealerId);
                    logger.info("effected dealer {}" ,coStmedDealerId);

                    if (coStmedDealerId != null)
                        if (!coStmedDealerId.equals(dealerId) && preactivatedDealers != null) {
                            for(String dealer : preactivatedDealers) {
                                if (coStmedDealerId.toString().compareTo(dealer) == 0) {
                                    logger.info("Contract dealer id matches {} configured generic dealer", dealer);
                                    dealerMatches = true;
                                    break;
                                }
                            }
                        } else {
                            dealerMatches = true;
                        }

                    logger.info("dealerMatches = {}", dealerMatches);
                    boolean isPreactivatedContract = rpDesMatches && dealerMatches;
                    logger.info("rpDesMatches = {} and dealerMatches = {} => isPreActivatedContract = {}",
                            rpDesMatches, dealerMatches, isPreactivatedContract);
                    response.setIsPreactivatedContract(isPreactivatedContract);
                    response.setDirNum(preactivatedContract.getDirnum());
                    response.setSmNum(cdrbo.getCoStmedno());
                    response.setRpDes(rpDes);
                }
            } else {
                logger.info("No contract found for smNum and/or dirNum");
                response.setIsContractFound(false);
            }

            logger.info("<= checkPreActivatedContract()");
            return response;
        }
    }
}
