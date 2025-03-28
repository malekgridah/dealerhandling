package com.billcom.dealerhandling.commons.exception;


import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.utils.XMLUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * @author malek.gridah
 */

public class SoapFaultHandler extends BasicHandler {
    private static Logger logger = LogManager.getLogger(SoapFaultHandler.class);
    private static String DEFAULT_NAMESPACE = "http://alu.services.ws.lhs.com";
    private ThreadLocal<String> namespace = new ThreadLocal();
    private ThreadLocal<String> inputFault = new ThreadLocal();

    public SoapFaultHandler() {
    }

    public void invoke(MessageContext mc) throws AxisFault {
        logger.info("Call to invoke()");
        boolean namespaceIsSet = false;

        try {
            if (mc == null) {
                logger.warn("Message Context is null");
                return;
            }

            Message m = mc.getCurrentMessage();
            if (m == null) {
                logger.warn("Current Message is null");
                return;
            }

            SOAPBody b = m.getSOAPBody();
            if (b != null) {
                Node n = b.getFirstChild();
                if (n == null) {
                    logger.warn("First Child is null");
                    return;
                }

                String uri = n.getNamespaceURI();
                if (uri == null) {
                    logger.warn("Namespace URI is null");
                    return;
                }

                this.namespace.set(uri);
                namespaceIsSet = true;
                logger.info("using namespace=" + uri);
                return;
            }

            logger.warn("SOAP Body is null");
        } catch (SOAPException var10) {
            logger.error(var10);
            this.inputFault.set(var10.getMessage());
            return;
        } finally {
            if (!namespaceIsSet) {
                logger.warn("using default namespace=\"" + DEFAULT_NAMESPACE + "\"");
                this.namespace.set(DEFAULT_NAMESPACE);
            }

        }

    }

    public void onFault(MessageContext msgContext) {
        logger.info("Call to onFault()");
        logger.info("namespace=" + (String)this.namespace.get());
        SOAPMessage msg = msgContext.getResponseMessage();
        SOAPBody body = null;

        try {
            body = msg.getSOAPBody();
            SOAPFault originalFault = body.getFault();
            String faultCode = "";
            String originalFaultString = null;
            String faultString = null;
            if (this.inputFault.get() != null) {
                originalFaultString = (String)this.inputFault.get();
            } else if (originalFault == null) {
                originalFaultString = XMLUtils.ElementToString(body);
            } else {
                originalFaultString = originalFault.getFaultString();
            }

            faultString = originalFaultString;
            logger.info(originalFaultString);
            if (originalFaultString != null) {
                String sqlReq = null;
                int i1 = originalFaultString.indexOf("Call:");
                int i2 = originalFaultString.indexOf("Query:");
                int i3 = 0;
                if (i1 != -1 && i2 > i1) {
                    sqlReq = originalFaultString.substring(i1 + "Call:".length(), i2);
                }

                i1 = originalFaultString.lastIndexOf("Error code:");
                if (i1 != -1) {
                    i2 = originalFaultString.indexOf(10, i1);
                    if (i2 > i1) {
                        faultCode = originalFaultString.substring(i1 + "Error code:".length(), i2);
                    }
                }

                i1 = originalFaultString.lastIndexOf("Error message:");
                if (i1 != -1) {
                    i2 = originalFaultString.indexOf("ClassName:", i1);
                    i3 = originalFaultString.indexOf("Error code:", i1);
                    if (i3 != -1 && i3 < i2) {
                        i2 = i3;
                    }

                    if (i2 > i1) {
                        faultString = originalFaultString.substring(i1 + "Error message:".length(), i2).trim();
                    }
                }

                if (sqlReq != null) {
                    faultString = faultString + "\nSQL Query related: " + sqlReq.trim();
                }
            }

            body.removeContents();
            Name n = new PrefixedQName((String)this.namespace.get(), "UnexpectedErrorFault", "alu");
            SOAPFault fault = body.addFault(n, "Unexpected error while executing BSCS webservice operation");
            Detail d = fault.addDetail();
            DetailEntry de = d.addDetailEntry(n);
            Name fc = new PrefixedQName((String)this.namespace.get(), "faultcode", "alu");
            SOAPElement ec = de.addChildElement(fc);
            ec.addTextNode(faultCode);
            Name fs = new PrefixedQName((String)this.namespace.get(), "faultstring", "alu");
            SOAPElement es = de.addChildElement(fs);
            es.addTextNode(faultString);
        } catch (Throwable var19) {
            logger.error("Error while setting the error", var19);
        } finally {
            this.namespace.set((String) null);
            this.inputFault.set((String) null);
        }

    }
}
