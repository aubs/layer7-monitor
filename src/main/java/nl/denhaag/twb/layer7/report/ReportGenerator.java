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

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.denhaag.twb.layer7.util.WsdlSchemaFileInfo;
import nl.denhaag.twb.util.Util;

import org.w3c.dom.Element;

import com.l7tech.gateway.api.ServiceDetail;
import com.l7tech.gateway.api.ServiceDetail.HttpMapping;
import com.l7tech.gateway.api.ServiceDetail.ServiceMapping;
import com.l7tech.gateway.api.ServiceMO;

public class ReportGenerator {

	private Date timestamp;


	public ReportGenerator(Date timestamp) {
		this.timestamp = timestamp;

	}



	public void parse(ServiceMO service, String policyManagerPath, String protectedServiceUrl, List<WsdlSchemaFileInfo> wsdlSchemaInfos, File indexXmlFile) throws Exception {
		ServiceDetail serviceDetail = service.getServiceDetail();
		XMLBuilder xmlBuilder = new XMLBuilder();
        Element rootElement = xmlBuilder.addRootElement("service");
        xmlBuilder.addAttributeToElement(rootElement, "generatorVersion", Util.getImplementationVersion(this.getClass()));
        xmlBuilder.addAttributeToElement(rootElement, "generationDate", timestamp);
        xmlBuilder.addTextElement(rootElement, "id", service.getId());
        xmlBuilder.addTextElement(rootElement, "name", service.getServiceDetail().getName());
        Long policyVersion = (Long) serviceDetail.getProperties().get("policyRevision");
        xmlBuilder.addTextElement(rootElement, "policyVersion", policyVersion + "");
        xmlBuilder.addTextElement(rootElement, "version", service.getVersion() + "");        
        xmlBuilder.addTextElement(rootElement, "enabled", serviceDetail.getEnabled() +"");    
        xmlBuilder.addTextElement(rootElement, "policyManagerPath", policyManagerPath);
		HttpMapping httpMapping = null;
		for (ServiceMapping serviceMapping : serviceDetail.getServiceMappings()) {
			if (serviceMapping instanceof HttpMapping) {
				httpMapping = (HttpMapping) serviceMapping;

			}
		}
        xmlBuilder.addTextElement(rootElement, "resolutionPath", httpMapping.getUrlPattern());              
        xmlBuilder.addTextElement(rootElement, "protectedEndpoint", protectedServiceUrl); 
        xmlBuilder.addTextElement(rootElement, "wsSecurity", serviceDetail.getProperties().get("wssProcessingEnabled") + ""); 
        Boolean isSoap = (Boolean) serviceDetail.getProperties().get("soap");
        xmlBuilder.addTextElement(rootElement, "soap", isSoap.toString()); 
        if (isSoap){
        	 xmlBuilder.addTextElement(rootElement, "soapVersion", serviceDetail.getProperties().get("soapVersion") + ""); 
        }
        xmlBuilder.addTextElement(rootElement, "internal", serviceDetail.getProperties().get("internal") + "");
        if (httpMapping.getVerbs().size() > 0){
        	Element httpMethodsElement = xmlBuilder.addElement(rootElement, "httpMethods");
    		for (int i = 0; i < httpMapping.getVerbs().size(); i++) {
    			xmlBuilder.addTextElement(httpMethodsElement, "httpMethod", httpMapping.getVerbs().get(i));
    		}        	
        }
        if (wsdlSchemaInfos.size() > 0){
        	Element filesElement = xmlBuilder.addElement(rootElement, "files");
			for (WsdlSchemaFileInfo info : wsdlSchemaInfos){
				Element fileElement = xmlBuilder.addElement(filesElement, "file");
				fileElement.setTextContent(info.getFilename());
				xmlBuilder.addAttributeToElement(fileElement, "type", info.getType());
				if (info.isRoot()){
					xmlBuilder.addAttributeToElement(fileElement, "root", info.isRoot() + "");
				}
			}
        }

        FileWriter writer = new FileWriter(indexXmlFile);
        StreamResult result =  new StreamResult(writer);        
        Source source = new DOMSource(xmlBuilder.getDocument());
        // Write the DOM document to the file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(source, result);
        writer.flush();
        writer.close();
        XMLTransformer.transformClassloader(indexXmlFile, new File(indexXmlFile.getParentFile(), "index.html"), "xsl/html.xsl");
	}



}
