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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.denhaag.twb.util.Util;

import com.l7tech.gateway.api.Resource;

public class WsdlSchemaFileInfo {
	private static final String UNSUPPORTED = "UNSUPPORTED";
	private static final String XSD = "XSD";
	private static final String XMLSCHEMA = "xmlschema";
	private static final String WSDL = "WSDL";
	private static final String XSD_PARAMETER = "?xsd=";
	private static final String WSDL_PARAMETER = "?wsdl";
	public static final String SEPARATOR = "/";
	private String[] splittedSourceUrl;
	private Resource resource;
	int position = 0;
	private List<String> importsAndIncludes = new ArrayList<String>();
	private boolean root;
	private String filename;
	private String type;
	
	protected WsdlSchemaFileInfo(Resource resource, boolean root) throws Exception{
		WsdlSchemaAnalyzer wsdlSchemaAnalyzer = new WsdlSchemaAnalyzer(resource.getContent());
		this.importsAndIncludes = wsdlSchemaAnalyzer.getImportOrIncludeLocations();
		splittedSourceUrl = resource.getSourceUrl().split(SEPARATOR);
		position = splittedSourceUrl.length - 1;
		this.resource = resource;
		this.root = root;
	}


	/**
	 * @return the resource
	 */
	protected Resource getResource() {
		return resource;
	}


	protected int calculatePosition(){
		int numberOfParentFolders = getNumberOfParentFolders(importsAndIncludes);
		position -=numberOfParentFolders;
		return position;
	}
	protected String getUrl(int newPosition){
		int i = newPosition;
		if ("http:".equalsIgnoreCase(splittedSourceUrl[0]) || "https:".equalsIgnoreCase(splittedSourceUrl[0])){
			i = 2;
		}
		String result = null;
		while (i < splittedSourceUrl.length){
			if (result == null){
				result = convertToValidFilename(splittedSourceUrl[i]);
			}else {
				result +=SEPARATOR + convertToValidFilename(splittedSourceUrl[i]);
			}
			i++;
		}
		return result;

	}
	private static int getNumberOfParentFolders(List<String> importsAndIncludes){
		int maxNumber = 0;
		Pattern pattern = Pattern.compile("\\.\\./");
			for (String value: importsAndIncludes){
				Matcher matcher = pattern.matcher(value);  
			    int count;  
			    for (count = 0; matcher.find(); count++)  
			        ;  
			    if (count > maxNumber){
			    	maxNumber = count;
			    }
			}
			
		return maxNumber;
	}
	/**
	 * Convert url to a valid file name
	 * 
	 * @param filename
	 *            filename from Layer7
	 * @return Valid xsd or wsdl file name
	 */
	public static String convertToValidFilename(String filename) {
		if (filename.endsWith(WSDL_PARAMETER)) {
			filename = filename.substring(0, (filename.length() - WSDL_PARAMETER.length())) + ".wsdl";
		}
		int xsdIndex = filename.lastIndexOf(XSD_PARAMETER);
		if (xsdIndex > -1) {
			filename = filename.substring(xsdIndex + XSD_PARAMETER.length());
		} else if (filename.startsWith(XSD_PARAMETER)) {
			filename = filename.substring(XSD_PARAMETER.length());
		}
		return  Util.removeForbiddenFileCharacters(filename);
	}


	/**
	 * @return the root
	 */
	public boolean isRoot() {
		return root;
	}


	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}


	/**
	 * @param filename the filename to set
	 */
	protected void setFilename(String filename) {
		this.filename = filename;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		if (WSDL.equalsIgnoreCase(resource.getType())){
			return WSDL;
		}else if (XMLSCHEMA.equalsIgnoreCase(resource.getType())){
			return XSD;
		}else {
			return UNSUPPORTED;
		}
	}
}
