/*
 * #%L
 * Layer7 Monitor
 * %%
 * Copyright (C) 2010 - 2015 Team Applicatie Integratie (Gemeente Den Haag)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package nl.denhaag.twb.layer7.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.s9api.SaxonApiException;
import nl.denhaag.twb.layer7.policy.PolicyXslt;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Analyze Policy file from Layzer7
 *
 */
public class PolicyAnalyzer {


	private final static XPath XPATH = XPathFactory.newInstance().newXPath();
	private static XPathExpression PROTECTED_SERVICE_URL_EXPRESSION;
	private String protectedServiceUrl;

	static {
		try {
			XPATH.setNamespaceContext(new PolicyNamespaceContext());
			PROTECTED_SERVICE_URL_EXPRESSION = XPATH.compile("//L7p:HttpRoutingAssertion/L7p:ProtectedServiceUrl/@stringValue");
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param file Policy file
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws SaxonApiException 
	 */
	public PolicyAnalyzer(File policyFile) throws Exception {
		File reportFile = new File(policyFile.getParentFile(), "policy.html");
		PrintWriter printWriter = new PrintWriter(reportFile);	
		PolicyXslt.generatePolicyReport(printWriter, policyFile);
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document policyDocument = builder.parse(policyFile.getAbsolutePath());
		printWriter.flush();
		printWriter.close();
		try {
			protectedServiceUrl = (String) PROTECTED_SERVICE_URL_EXPRESSION.evaluate(policyDocument, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
		}
		
	}
	/**
	 * Get the endpoint of a service that is protected by Layer7. In other words it is the real endpoint of a webservice
	 * @return
	 */
	public String getProtectedServiceUrl(){
		return protectedServiceUrl;
	}
    private static class PolicyNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if("wsp".equals(prefix)) {
                return "http://schemas.xmlsoap.org/ws/2002/12/policy";
            }if("L7p".equals(prefix)) {
                return "http://www.layer7tech.com/ws/policy";
            }
            return null;
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        @SuppressWarnings("rawtypes")
		public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }
}
