package com.billcom.dealerhandling.commons.manager;

import com.billcom.connectionpools.config.SpringContext;
import com.billcom.connectionpools.config.properties.ServerConnectionSettings;
import com.billcom.dealerhandling.commons.beans.OperationResponse;
import com.billcom.dealerhandling.commons.connection.CMSConnection;
import com.billcom.dealerhandling.commons.connection.CMSException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.CompletionStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author malek.gridah
 */

public class CMSManager {

    private  final Logger logger = LogManager.getLogger(CMSManager.class);
    private  Map<Class, Class> implementations = new HashMap<>();
    private  String custoPackage = null;
    private  Integer automaticRetryNumber = null;
    private  int BASIC_EXECUTION_ATTEMPTS = 1;

    private  Class getImplementation(Class c) {
        if (!implementations.containsKey(c)) {
            try{
                logger.debug("Looking for a customization of {} in {}", c.getName(), custoPackage);
                Class<?> i = Class.forName(custoPackage + "." + c.getSimpleName());
                implementations.put(c, i);
                logger.debug("Found :{}", i.getName());
            }catch(ClassNotFoundException cnfe){
                logger.debug("None found");
                implementations.put(c, c);
            }
        }

        return implementations.get(c);
    }

    public OperationResponse callServiceMethod(Class backend, String methodName, Object parameter) {

        logger.debug("=>callServiceMethod {}", methodName);

        OperationResponse response = null;
        CMSConnection connection;
        boolean isCommited;

        Method backendMethod ;
        Method getInstanceMethod ;
        int tryMore=0;

        automaticRetryNumber = SpringContext.getBean(ServerConnectionSettings.class).getApplication().getRetryAttempts();


        if(automaticRetryNumber!=null){
            tryMore = automaticRetryNumber;
            if (tryMore<0)
                tryMore=0;
            logger.debug("tryMore parameter was overwritten by: [{}] and is adjusted to: [{}]", automaticRetryNumber, tryMore);
        }
        tryMore=tryMore + BASIC_EXECUTION_ATTEMPTS;
        logger.debug("Total number of attempts is set to: [{}]", tryMore);

        while (tryMore>0){
            isCommited = false;
            connection = null;
            response=null;

            IService beInstance = null;

            try {
                Class implementation = getImplementation(backend);

                try {
                    getInstanceMethod = implementation.getMethod("getInstance");

                    Class responseClass = getInstanceMethod.getReturnType();
                    if (!responseClass.equals(implementation)) {
                        throw new Exception("Problem with getInstance() method, "
                                + "return type is " + responseClass.getName() + " but should be " + implementation.getName());
                    }

                } catch (NoSuchMethodException nsme) {
                    String message = "The method <" + implementation.getSimpleName() + "."
                            + "getInstance()> was not found. \n"
                            + "--> Make sure you've implemented it in that backend class.";
                    response = new OperationResponse();
                    throw new CMSException("Wrong method name or parameter", message, nsme);
                } catch (Throwable t) {

                    response = new OperationResponse();
                    throw new CMSException("Internal error", t.getMessage(), t);

                }

                try {
                    backendMethod = backend.getMethod(methodName, CMSConnection.class, parameter.getClass());

                    Class responseClass = backendMethod.getReturnType();
                    if (!OperationResponse.class.isAssignableFrom(responseClass)) {
                        throw new Exception("Method not compatible, "
                                + "return Type " + responseClass
                                + " is not a subtype of OperationResponse");
                    }

                    response = (OperationResponse) responseClass.newInstance();

                } catch (NoSuchMethodException nsme) {
                    String message = "The method <" + backend.getSimpleName() + "."
                            + methodName + "(" + CMSConnection.class.getSimpleName() + ", "
                            + parameter.getClass().getSimpleName() + ")> was not found. \n"
                            + "--> Make sure you're calling the right backend method.";
                    response = new OperationResponse();
                    throw new CMSException("Wrong method name or parameter", message, nsme);
                } catch (Throwable t) {
                    response = new OperationResponse();
                    throw new CMSException("Internal error", t.getMessage(), t);
                }

                Object[] params = new Object[]{};
                try {
                    beInstance = (IService) getInstanceMethod.invoke(null, params);
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }

                connection = new CMSConnection();

                params = new Object[]{connection, parameter};
                try {
                    response = (OperationResponse) backendMethod.invoke(beInstance, params);
                    tryMore=0; // Stop the loop
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }

                connection.commit();
                isCommited = true;

                response.setIsSuccessful(Boolean.TRUE);
            } catch (CMSException wse) {

                tryMore=0; // Stop the loop

                response.setIsSuccessful(Boolean.FALSE);
                response.setComment(wse.prettyPrint());
                response.setBscsErrorCode(wse.getErrorCode());

                logger.fatal(wse.toLogString());

            } catch (Throwable t) {
                boolean flag = true;
                if (t instanceof org.omg.CORBA.SystemException) {
                    org.omg.CORBA.SystemException systemException = (org.omg.CORBA.SystemException)t;
                    if (systemException.completed.value()== CompletionStatus._COMPLETED_MAYBE){
                        tryMore=0;
                        logger.fatal("CMS command MAYBE Completed for the method name [{}], No more attempts.", methodName);
                        logger.fatal("Minor code : "+systemException.minor);
                        response.setIsSuccessful(Boolean.FALSE);
                        response.setComment("Unexpected error " + t.getClass().getName() + ": " + t.getMessage() + ". Check the log for more details");
                        flag = false;
                    }
                    if (systemException.completed.value()== CompletionStatus._COMPLETED_YES){
                        tryMore=0;
                        logger.error("CMS command YES Completed for the method name [{}], No more attempts.", methodName);
                        logger.error("Minor code : {}", systemException.minor);
                        response.setIsSuccessful(Boolean.TRUE);
                        response.setComment("CMS command completed with error " + t.getClass().getName() + ": " + t.getMessage() + ". Check the log for more details");
                        flag = false;
                    }
                    if (systemException.completed.value()== CompletionStatus._COMPLETED_NO){
                        logger.fatal("CMS command NOT Completed for the method name [{}]... Proceeding to RETRY", methodName);
                        logger.fatal("Minor code : "+systemException.minor);
                        flag = true;
                    }
                }
                if(flag) {
                    logger.fatal("Unexpected error", t);

                    tryMore --;
                    logger.debug("Failed to execute an CMS command for the method name [{}], still: [{}] attempts.", methodName, tryMore);

                    if (tryMore == 0)
                    {
                        logger.fatal("Failed to execute an CMS command for the method name [{}], No more attempts.", methodName);
                        // build the error response that will show up in the SOAP message
                        response.setIsSuccessful(Boolean.FALSE);
                        response.setComment("Unexpected error " + t.getClass().getName() + ": " + t.getMessage() + ". Check the log for more details");
                    }
                }
            } finally {
                logger.debug("rollback");
                if ((connection != null)) {
                    if (!isCommited) {
                        connection.rollback();
                    }
                    connection.release();
                }
            }
        }
        logger.debug("<= callServiceMethod {}", methodName);
        return response;

    }
}

