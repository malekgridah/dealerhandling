package com.billcom.dealerhandling.commons.connection;


import com.billcom.connectionpools.config.SpringContext;
import com.billcom.connectionpools.config.properties.ServerConnectionSettings;
import com.billcom.connectionpools.soiprovider.SOIAccessor;
import com.billcom.connectionpools.soiprovider.SOIFacade;
import com.lhs.ccb.cfw.cda.session.*;

import com.lhs.ccb.common.soi.SVLObject;
import com.lhs.ccb.common.soi.ServiceAccessor;
import com.lhs.ccb.func.ect.ComponentException;
import com.lhs.ws.beans.BaseSOIBean;
import com.lhs.ws.beans.BeanSOIConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;

/**
 * @author malek.gridah
 */

public class CMSConnection {
    private final Logger _log = LogManager.getLogger(CMSConnection.class);

    private ServiceAccessor access = null;
    private SOIAccessor accessData = null;


    public CMSConnection() throws CMSException {
        ServerConnectionSettings.SoiProperties appSetting = SpringContext.getBean(ServerConnectionSettings.class).getSoi();
        this.connect(appSetting.getName(), String.valueOf(appSetting.getVersion()));
    }

    public CMSConnection(String soiServer, String soiVersion) throws CMSException {
        this.connect(soiServer, soiVersion);
    }

    private void connect(String soiServer, String soiVersion) throws CMSException {
        _log.info("=> connect() : {}, {}", soiServer, soiVersion);
        _log.info("Retrieving CMS connection");

        try {
            this.accessData = SOIFacade.getInstance().retrieveSOIAccessData(soiServer,soiVersion);
        } catch (SecurityException var4) {
            throw new CMSException("Connection error", "User not authenticated properly", var4);
        } catch (ComponentException var5) {
            throw new CMSException("Connection error", var5.getMessage(), var5.getErrorCode(), var5);
        } catch (ConnectionFailedException var6) {
            throw new CMSException("Connection error", "Unable to connect", var6);
        }

        this.access = this.accessData.getServiceAccessor();
        _log.info("<= connect() : success");
    }

    public SVLObject execute(String CMSCommandName, SVLObject svlIn) throws CMSException {
        _log.info("=> execute : " + CMSCommandName);
        SVLObject svlOut = null;
        if (this.access == null) {
            throw new CMSException("CMS Connection error", "not connected", (Throwable)null);
        } else {
            try {
                _log.info("Input values:\n" + svlIn.toString());
                svlOut = this.access.execute(CMSCommandName, svlIn);
                _log.info("Output values:\n" + svlOut.toString());
            } catch (ComponentException var6) {
                String msg = "Error while executing '" + CMSCommandName + "'";
                _log.error(msg, var6);
                throw new CMSException(msg, var6.getMessage(), var6.getErrorCode(), var6);
            }

            _log.info("<= execute : " + CMSCommandName);
            return svlOut;
        }
    }

    public SVLObject execute(String CMSCommandName, BaseSOIBean input) throws CMSException {
        SVLObject svlIn = BeanSOIConverter.getSVLObject(input);
        SVLObject svlOut = this.execute(CMSCommandName, svlIn);
        return svlOut;
    }

    public BaseSOIBean executeAndConvertOutput(String CMSCommandName, BaseSOIBean input, Class outputClass) throws CMSException {
        SVLObject svlOut = this.execute(CMSCommandName, input);

        try {
            Constructor constructor = outputClass.getConstructor(SVLObject.class);
            return (BaseSOIBean)constructor.newInstance(svlOut);
        } catch (Exception var6) {
            throw new CMSException("Data error", "Unable to read out response from command " + CMSCommandName, var6);
        }
    }

    public BaseSOIBean executeAndConvertOutput(String CMSCommandName, SVLObject input, Class outputClass) throws CMSException {
        SVLObject svlOut = this.execute(CMSCommandName, input);

        try {
            Constructor constructor = outputClass.getConstructor(SVLObject.class);
            return (BaseSOIBean)constructor.newInstance(svlOut);
        } catch (Exception var6) {
            throw new CMSException("Data error", "Unable to read out response from command " + CMSCommandName, var6);
        }
    }

    public SVLObject execute(String cmdName, BaseSOIBean input, Class inputClass) throws CMSException {
        BaseSOIBean filteredInput = null;

        SVLObject svlOut;
        try {
            svlOut = BeanSOIConverter.getSVLObject(input);
            Class[] vTypes = new Class[]{SVLObject.class};
            Constructor gen = inputClass.getConstructor(vTypes);
            Object[] args = new Object[]{svlOut};
            filteredInput = (BaseSOIBean)gen.newInstance(args);
        } catch (Exception var9) {
            String cname = null;
            if (inputClass != null) {
                cname = inputClass.getName();
            }

            throw new CMSException("Data error", "Error while building " + cmdName + " request with class " + cname, var9);
        }

        svlOut = this.execute(cmdName, filteredInput);
        return svlOut;
    }

    public void commit() throws CMSException {
        _log.info("=> commit()");

        try {
            this.access.commit();
        } catch (ComponentException var2) {
            throw new CMSException("Error while trying to commit the transaction", var2.getMessage(), var2.getErrorCode(), var2);
        }

        _log.info("<= commit() : success");
    }

    public void rollback() {
        _log.info("=> rollback()");

        try {
            this.access.rollback();
        } catch (ComponentException var2) {
            _log.error("Error while rolling back: {}", var2.toString());
        }

        _log.info("<= rollback() : success");
    }

    public void release() {
        this.accessData.release();
        this.access = null;
        this.accessData = null;
    }
}
