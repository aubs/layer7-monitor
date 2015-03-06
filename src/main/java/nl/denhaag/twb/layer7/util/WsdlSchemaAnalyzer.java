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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Analyze WSDL and Schema
 *
 */
public class WsdlSchemaAnalyzer {


	private final static XPath XPATH = XPathFactory.newInstance().newXPath();
	private static XPathExpression WSDL_IMPORT_EXPRESSION;
	private static XPathExpression XSD_IMPORT_EXPRESSION;	
	private static XPathExpression XSD_INCLUDE_EXPRESSION;
	private List<String> importOrIncludeLocations = new ArrayList<String>();

	static {
		try {
			XPATH.setNamespaceContext(new WsdlSchemaNamespaceContext());
			WSDL_IMPORT_EXPRESSION = XPATH.compile("//wsdl:import/@location");
			XSD_IMPORT_EXPRESSION = XPATH.compile("//xsd:import/@schemaLocation");
			XSD_INCLUDE_EXPRESSION = XPATH.compile("//xsd:include/@schemaLocation");
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
	public WsdlSchemaAnalyzer(String wsdlSchemaContent) throws Exception {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(wsdlSchemaContent));
		Document document = builder.parse(is);
		try {
			addLocations(document, WSDL_IMPORT_EXPRESSION);
			addLocations(document, XSD_IMPORT_EXPRESSION);
			addLocations(document, XSD_INCLUDE_EXPRESSION);
		} catch (XPathExpressionException e) {
		}
		
	}
	private void addLocations(Document document, XPathExpression xpathExpression ) throws XPathExpressionException{
		NodeList importsOrIncludes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < importsOrIncludes.getLength(); i++){
			Node node = importsOrIncludes.item(i);
			String url = node.getTextContent();
			if (StringUtils.isNotBlank(url)){
				importOrIncludeLocations.add(url);
			}
		}
	}

    /**
	 * @return the importOrIncludeLocations
	 */
	protected List<String> getImportOrIncludeLocations() {
		return importOrIncludeLocations;
	}

	private static class WsdlSchemaNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if("xsd".equals(prefix)) {
                return "http://www.w3.org/2001/XMLSchema";
            }else if("wsdl".equals(prefix)) {
                return "http://schemas.xmlsoap.org/wsdl/";
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
