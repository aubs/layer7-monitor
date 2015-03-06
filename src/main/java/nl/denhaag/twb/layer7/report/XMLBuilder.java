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
package nl.denhaag.twb.layer7.report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLBuilder {
    public static final String DEFAULT_NAMESPACE = "http://schemas.denhaag.nl/tw/layer7";
	public final static String STANDARD_XML_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String SEMICOLON = ":";
    private static final String XMLNS = "xmlns";
    private static final String WHITESPACE = " ";
    private static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
    private Document document = null;
    private Element root = null;
    private boolean finalDocument = false;
    private Map<String, NameSpace> namespaces = new HashMap<String, NameSpace>();

    public XMLBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        document = docBuilder.newDocument();
        addNamespace("", DEFAULT_NAMESPACE,  "layer7.xsd");
        addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance", null);
    }

    public void addNamespace(String prefix, String namespace, String location) {
        NameSpace nameSpace = new NameSpace();
        nameSpace.location = location;
        nameSpace.namespace = namespace;
        nameSpace.prefix = prefix;
        namespaces.put(namespace, nameSpace);
    }

    public Document getDocument() {
        if (!finalDocument) {
            finalDocument = true;

            addRootAttributes();

        }
        return document;
    }

    class NameSpace {

        String prefix = null;
        String namespace = null;
        String location = null;
    }

    protected void addRootAttributes() {
        String locations = "";
        Set<String> keys = namespaces.keySet();
        Iterator<String> keysIterator = keys.iterator();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            NameSpace nameSpace = namespaces.get(key);
            String attrName = XMLNS;
            if (!StringUtils.isBlank(nameSpace.prefix)) {
                attrName += SEMICOLON + nameSpace.prefix;
            }
            Attr attr = document.createAttribute(attrName);
            attr.setValue(nameSpace.namespace);
            root.setAttributeNode(attr);
            if (!StringUtils.isBlank(nameSpace.location)) {
                locations += nameSpace.namespace + WHITESPACE + nameSpace.location + WHITESPACE;
            }
        }
        if (locations.length() > 0) {
            Attr attr = document.createAttribute(XSI_SCHEMA_LOCATION);
            attr.setValue(locations);
            root.setAttributeNode(attr);
        }
    }

    protected String getQualifiedName(String namespace, String name) {
        if (!StringUtils.isBlank(namespace)) {
            NameSpace nameSpace = (NameSpace) namespaces.get(namespace);
            if (nameSpace != null) {
                if (StringUtils.isBlank(nameSpace.prefix)) {
                    return name;
                } else {
                    return nameSpace.prefix + SEMICOLON + name;
                }
            }
        }
        return name;
    }

    public void addTextElement(Element element, String name, String value) {
        if (value != null && value.trim().length() > 0) {
            Element next =  document.createElementNS(DEFAULT_NAMESPACE, name);
            next.appendChild(document.createTextNode(value));
            element.appendChild(next);
        }

    }

    public Element addElement(Element element, String name) {
        Element next = document.createElementNS(DEFAULT_NAMESPACE, name);
        element.appendChild(next);
        return next;
    }

    public void addAttributeToElement(Element element, String attributeName, String attributeValue) {
        if (attributeValue != null && attributeValue.trim().length() > 0) {
            element.setAttribute(attributeName, attributeValue);
        }
    }
    public void addAttributeToElement(Element element, String attributeName, Date attributeValue) {
        if (attributeValue != null) {
            element.setAttribute(attributeName, toXmlDateString(attributeValue));
        }
    }

	public static String toXmlDateString(Date date) {
		if (date != null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
			//simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			simpleDateFormat.applyPattern(STANDARD_XML_DATE_TIME_FORMAT);
			return simpleDateFormat.format(date);
		} else {
			return null;
		}

	}
    public Element addCDATATextElement(Element element, String name, String value) {
        if (value != null) {
            Element next = document.createElement(name);
            next.appendChild(document.createCDATASection(value));
            element.appendChild(next);
            return next;
        }
        return null;
    }

    public Element addRootElement(String name) {
        root = document.createElement(name);
        document.appendChild(root);
        return root;
    }
}
