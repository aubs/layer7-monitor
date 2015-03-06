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
package nl.denhaag.twb.layer7.rss;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.denhaag.twb.layer7.db.RetrievedService;
import nl.denhaag.twb.layer7.util.DeletedPolicyData;
import nl.denhaag.twb.layer7.util.NewChangedPolicyData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.l7tech.gateway.api.ServiceDetail;
import com.l7tech.gateway.api.ServiceDetail.HttpMapping;
import com.l7tech.gateway.api.ServiceDetail.ServiceMapping;
import com.l7tech.gateway.api.ServiceMO;

/**
 * Compose email messages with information of the retrieved services.
 * 
 * 
 */
public class RSSLogger {

	private static final String PUB_DATE = "pubDate";
	private static final SimpleDateFormat RSS_PUB_DATE = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
	private static final long FOUR_WEEKS = 1000l*60l*60l*24l*7l*4l;
	private int numberOfNewServices = 0;
	private int numberOfChangedServices = 0;
	private int numberOfDeletedServices = 0;
	private Date timestamp;
	private Document document;
	private File rssFile;
	private String url;
	private String envName;

	/**
	 * Default contructor
	 * 
	 * @param timestamp
	 *            Current time
	 * @param mailProperties
	 *            Properties for sending emails
	 * @param baseDir
	 *            Layer7 base directory.
	 */
	public RSSLogger(Date timestamp, String envName, String url, File rssFile) {
		this.rssFile = rssFile;
		this.timestamp = timestamp;
		this.url = url +  envName + "/";
		this.envName = envName;
		initRss();
	}


	public void addNewOrChangedService(NewChangedPolicyData policyData) {
		numberOfChangedServices++;
		appendNewOrChangedService(policyData);
	}

	/**
	 * Add delete service to the email message.
	 * 
	 * @param service
	 *            Deleted service
	 */
	public void addDeletedService(DeletedPolicyData policyData) {
		numberOfDeletedServices++;
		appendDeletedService(policyData);
	}

	private void appendNewOrChangedService(NewChangedPolicyData policyData) {
		ServiceMO service = policyData.getNewChangedService();
		ServiceDetail serviceDetail = service.getServiceDetail();
		Element channelElement = (Element) document.getElementsByTagName("channel").item(0);
		Element itemElement = document.createElement("item");
		Element titleElement = document.createElement("title");

		if (policyData.getOldService() == null){
			titleElement.setTextContent("ADDED: " + policyData.getNewChangedService().getId() + " - "+ policyData.getNewChangedService().getServiceDetail().getName());
		}else {
			String prefix = null;
			if (policyData.getOldService() != null && service.getVersion().intValue() > policyData.getOldService().getVersion().intValue()) {
				prefix = "SERVICE CHANGED: ";
			}

			Long newPolicyVersion = (Long) serviceDetail.getProperties().get("policyRevision");
			if (policyData.getOldService() != null && newPolicyVersion.intValue() > policyData.getOldService().getPolicyVersion().intValue()) {
				if (prefix == null){
					prefix = "POLICY CHANGED: ";
				}else {
					prefix = "ALL CHANGED: ";
				}
			}			
			titleElement.setTextContent(prefix + policyData.getNewChangedService().getId() + " - "+  policyData.getNewChangedService().getServiceDetail().getName());
		}
		itemElement.appendChild(titleElement);
		Element descriptionElement = document.createElement("description");
		descriptionElement.appendChild(document.createCDATASection(getInfoNewOrChangedService(policyData)));
		itemElement.appendChild(descriptionElement);	
		
		Element pubDateElement = document.createElement(PUB_DATE);
		pubDateElement.setTextContent(RSS_PUB_DATE.format(timestamp));
		itemElement.appendChild(pubDateElement);
		if (channelElement.getChildNodes().getLength() > 0){
			Node firstNode = channelElement.getFirstChild();
			channelElement.insertBefore(itemElement, firstNode);
		}else {
			
			channelElement.appendChild(itemElement);
		}
	}

	private void appendDeletedService(DeletedPolicyData policyData) {
		Element channelElement = (Element) document.getElementsByTagName("channel").item(0);
		Element itemElement = document.createElement("item");
		Element titleElement = document.createElement("title");
		titleElement.setTextContent("DELETED: " + policyData.getOldService().getId() + " - "+ policyData.getOldService().getName());
		itemElement.appendChild(titleElement);
		
		Element descriptionElement = document.createElement("description");
		descriptionElement.appendChild(document.createCDATASection(getInfoDeletedService(policyData.getOldService())));
		itemElement.appendChild(descriptionElement);	
		
		Element pubDateElement = document.createElement(PUB_DATE);
		pubDateElement.setTextContent(RSS_PUB_DATE.format(timestamp));
		itemElement.appendChild(pubDateElement);	
		
		channelElement.appendChild(itemElement);		
	}

	private String getInfoNewOrChangedService(NewChangedPolicyData policyData) {
		StringBuilder builder = new StringBuilder();
		ServiceMO service = policyData.getNewChangedService();
		ServiceDetail serviceDetail = service.getServiceDetail();
		builder.append("<br/>ID: ");
		builder.append(service.getId());
		builder.append("<br/>Service:&nbsp;<b>");
		builder.append(service.getServiceDetail().getName());
		builder.append("</b><br/>Enabled:&nbsp;");
		builder.append(service.getServiceDetail().getEnabled());
		HttpMapping httpMapping = null;
		for (ServiceMapping serviceMapping : serviceDetail.getServiceMappings()) {
			if (serviceMapping instanceof HttpMapping) {
				httpMapping = (HttpMapping) serviceMapping;

			}
		}
		builder.append("<br/>Policy manager path(folder): <b>");
		builder.append(policyData.getFolderName());		
		builder.append("</b><br/>Resolution path: <b>");
		builder.append(httpMapping.getUrlPattern());
		builder.append("</b><br/>Protected endpoint: <b>");
		builder.append(policyData.getPolicyAnalyzer().getProtectedServiceUrl());
		builder.append("</b><br/>Version: ");
		if (policyData.getOldService() != null && service.getVersion().intValue() > policyData.getOldService().getVersion().intValue()) {
			builder.append(policyData.getOldService().getVersion() + " -> ");
		}
		builder.append(service.getVersion());
		builder.append("<br/>Policy version: ");
		Long newPolicyVersion = (Long) serviceDetail.getProperties().get("policyRevision");
		if (policyData.getOldService() != null && newPolicyVersion.intValue() > policyData.getOldService().getPolicyVersion().intValue()) {
			builder.append(policyData.getOldService().getPolicyVersion() + " -> ");
		}
		builder.append(newPolicyVersion);
		builder.append("<br/>WS-Security: ");
		builder.append(serviceDetail.getProperties().get("wssProcessingEnabled"));
		builder.append("<br/>SOAP version: ");
		builder.append(serviceDetail.getProperties().get("soapVersion"));
		builder.append("<br/>SOAP: ");
		builder.append(serviceDetail.getProperties().get("soap"));
		builder.append("<br/>Internal: ");
		builder.append(serviceDetail.getProperties().get("internal"));
		builder.append("<br/>Allowed HTTP methods: ");
		for (int i = 0; i < httpMapping.getVerbs().size(); i++) {
			if (i > 0) {
				builder.append(",");
			}
			builder.append(httpMapping.getVerbs().get(i));
		}
		builder.append("<br/>Number of WSDL's and XSD's: <b>");
		builder.append(policyData.getNumberOfXsdWsdls());
		builder.append("</b>");
		builder.append("<br/><a href=\""+url + policyData.getZipName() + "\" target=\"_blank\">Download<b>");
		builder.append("<br/><a href=\""+url + policyData.getWebName() + "\" target=\"_blank\">View<b>");
		return builder.toString();
	}

	private String getInfoDeletedService(RetrievedService service) {
		StringBuilder builder = new StringBuilder();
		builder.append("ID:");
		builder.append(service.getId());
		builder.append("<br/>Service:");
		builder.append(service.getName());
		builder.append("<br/>Version:");
		builder.append(service.getVersion());
		builder.append("<br/>Policy version:");
		builder.append(service.getPolicyVersion());
		return builder.toString();
	}

	private void initRss() {
		if (document == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setValidating(true);
			factory.setIgnoringElementContentWhitespace(true);
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				if (rssFile.exists()) {
					document = builder.parse(rssFile);
				} else {
					document = builder.newDocument();
					createChannel();
					
				}
				cleanUp();
				// Do something with the document here.
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void cleanUp(){
		NodeList nodeList = document.getElementsByTagName(PUB_DATE);
		Date expireDate = new Date(timestamp.getTime() - FOUR_WEEKS);
		int size = nodeList.getLength();
		for (int i=size ; i >=0; i--){
			Node node = nodeList.item(i);
			try {
				Date rssPubDate = RSS_PUB_DATE.parse(node.getTextContent());		
				if (expireDate.after(rssPubDate)){
					Node item = node.getParentNode();
					Node channel = item.getParentNode();
					channel.removeChild(item);
				}
			} catch (Exception e) {
			}
			
		}
	}
	private void createChannel(){
		Element rssElement = document.createElement("rss");
		document.appendChild(rssElement);	
		rssElement.setAttribute("version", "2.0");
		Element channelElement = document.createElement("channel");
		rssElement.appendChild(channelElement);
		Element titleElement = document.createElement("title");
		titleElement.setTextContent(envName.toUpperCase() + " Layer7 Gateway changes");
		channelElement.appendChild(titleElement);			
	}
	
	/**
	 * Checks whether there are changes in Layer7
	 * 
	 * @return true if Layer7 changes
	 */
	public boolean isLayer7Changed() {
		return numberOfNewServices > 0 || numberOfChangedServices > 0 || numberOfDeletedServices > 0;
	}

	public void writeRss() {
			try {
		        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            DOMSource source = new DOMSource(document);
	            FileWriter writer = new FileWriter(rssFile);
	            StreamResult result =  new StreamResult(writer);

	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
	            transformer.transform(source, result);
	            writer.flush();
	            writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	
}
