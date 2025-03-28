package com.billcom.dealerhandling.commons.exception;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.QSWSDLHandler;
import org.apache.axis.utils.XMLUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;

import java.io.PrintWriter;

/**
 * @author malek.gridah
 */

public class SoapFaultWsdlHandler extends QSWSDLHandler{

    private static Logger logger = LogManager.getLogger(SoapFaultWsdlHandler.class);

    private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private void addErrorMessage(Document wsdlDoc) {
        NodeList l = wsdlDoc.getElementsByTagNameNS(WSDL_NS, "message");
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            NamedNodeMap attrs = n.getAttributes();
            Node name = attrs.getNamedItem("name");
            if (name != null && "UnexpectedError".equals(name.getTextContent())) {
                logger.info("found UnexpectedErrorFault ");

                Element e = wsdlDoc.createElementNS(WSDL_NS, "part");
                String prefix = wsdlDoc.lookupPrefix(WSDL_NS);

                logger.info("lookup of {" + WSDL_NS + "}: " + prefix);

                e.setPrefix(prefix);

                e.setAttribute("element", "impl:UnexpectedErrorFault");
                e.setAttribute("name", "UnexpectedError");
                n.appendChild(e);
            }
        }
    }

    private void addErrorElement(Document wsdlDoc) {
        Element element = wsdlDoc.createElementNS(XSD_NS, "element");
        element.setAttribute("name", "UnexpectedErrorFault");
        element.setAttribute("type", "impl:UnexpectedErrorFault");

        Element complexType = wsdlDoc.createElementNS(XSD_NS, "complexType");
        complexType.setAttribute("name", "UnexpectedErrorFault");

        Element sequence = wsdlDoc.createElementNS(XSD_NS, "sequence");

        Element elFaultcode = wsdlDoc.createElementNS(XSD_NS, "element");
        elFaultcode.setAttribute("name", "faultcode");
        elFaultcode.setAttribute("type", "xsd:string");
        sequence.appendChild(elFaultcode);

        Element elFaultstring = wsdlDoc.createElementNS(XSD_NS, "element");
        elFaultstring.setAttribute("name", "faultstring");
        elFaultstring.setAttribute("type", "xsd:string");
        sequence.appendChild(elFaultstring);

        complexType.appendChild(sequence);

        NodeList lnt = wsdlDoc.getElementsByTagNameNS(WSDL_NS, "types");
        NodeList ln = ((Element) lnt.item(0)).getElementsByTagName("schema");
        if (ln.getLength() > 0) {
            ln.item(0).appendChild(element);
            ln.item(0).appendChild(complexType);
        } else {
            logger.warn("'{" + XSD_NS + "}schema' not found");
        }
    }

    private void addAuthenticationHeader(Document wsdlDoc){
        
    }

    @Override
    public void invoke(MessageContext mc) throws AxisFault{

        // generate wsdl the normal way
        logger.info("QSWSDLHandler invoke begin");
        super.invoke(mc);
        logger.info("QSWSDLHandler invoke end");
        
        try {
            Document wsdlDoc = (Document) mc.getProperty("WSDL");
            
            // add UnexpectedError fault to the wsdl
            addErrorMessage(wsdlDoc);
            addErrorElement(wsdlDoc);

            // add the authentication to the wsdl
            addAuthenticationHeader(wsdlDoc);

            // write the WSDL
            PrintWriter writer = (PrintWriter) mc.getProperty
                (HTTPConstants.PLUGIN_WRITER);
            XMLUtils.PrettyDocumentToWriter(wsdlDoc, writer);

        } catch (Throwable ex) {
            logger.error("Error while generating WSDL", ex);
        }
    }

    @Override
    public void reportWSDL(Document doc, PrintWriter writer) {
        // override to prevent it from being used
    }

}
