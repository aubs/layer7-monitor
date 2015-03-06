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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.denhaag.twb.layer7.db.RetrievedService;
import nl.denhaag.twb.layer7.db.RetrievedServicePropertiesDAO;
import nl.denhaag.twb.layer7.db.ServiceStatus;
import nl.denhaag.twb.layer7.report.ReportGenerator;
import nl.denhaag.twb.layer7.rss.RSSLogger;
import nl.denhaag.twb.util.NoPolicyFileFilter;
import nl.denhaag.twb.util.Util;

import org.apache.log4j.Logger;

import com.l7tech.gateway.api.Resource;
import com.l7tech.gateway.api.ResourceSet;
import com.l7tech.gateway.api.ServiceDetail;
import com.l7tech.gateway.api.ServiceMO;

public class NewChangedPolicyData extends PolicyData {
	public static final String SEPARATOR = "/";
	public final static String STANDARD_DATE_FORMAT = "dd MMMM yyyy HH:mm";
	private static Logger LOGGER = Logger.getLogger(NewChangedPolicyData.class);
	private static final String POLICY_XML = "policy.xml";
	private static final String ZIP_SUFFIX = ".zip";

	private ServiceMO serviceMO;
	private File folder;
	private int numberOfXsdWsdls = 0;
	private String folderName;
	private PolicyAnalyzer policyAnalyzer;
	private RetrievedService oldService;	
	private FolderCache folderCache;
	public NewChangedPolicyData (DataManager dataManager, ServiceMO serviceMO, FolderCache folderCache){
		super(dataManager);
		this.serviceMO = serviceMO;
		this.folderCache = folderCache;
		folder = new File(dataManager.getWebLocation(), getName());
	}
	private String getName(){
		ServiceDetail serviceDetail = serviceMO.getServiceDetail();
		Long policyVersion = (Long) serviceDetail.getProperties().get("policyRevision");
		String name = getPrefix(serviceMO.getId(),WsdlSchemaFileInfo.convertToValidFilename(serviceMO.getServiceDetail().getName())) + "-v" + serviceMO.getVersion() + "-pv"
		+ policyVersion;
		return name;
	}

	/**
	 * Write temporary file.
	 * @param filename Name of the file to write
	 * @param content Content of the file
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException 
	 */
	public void writeTempFile(String filename, String content) throws FileNotFoundException, UnsupportedEncodingException{
		if (!folder.exists()){
			folder.mkdirs();
		}
		File outputFile = new File (folder, filename);
		outputFile.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		writer.print(content);
		writer.flush();
		writer.close();
	}
	
	public File getTempFile(String filename) throws FileNotFoundException{
		if (!folder.exists()){
			folder.mkdirs();
		}
		File outputFile = new File (folder, filename);
		outputFile.getParentFile().mkdirs();
		return outputFile;
	}
	/**
	 * Get temporary file
	 * @param filename Name of the file
	 * @return Temporary file
	 */
	private File getFile(String filename){
		return new File (folder, filename);
	}

	public String getZipName(){
		return "zips/" + folder.getName() + ZIP_SUFFIX;
	}
	public String getWebName(){
		return "web/" + folder.getName();
	}
	

	/**
	 * @return the numberOfXsdWsdls
	 */
	public int getNumberOfXsdWsdls() {
		return numberOfXsdWsdls;
	}
	public void process(RetrievedServicePropertiesDAO serviceDAO, RSSLogger rssLogger) throws Exception{
		ServiceStatus serviceStatus = serviceDAO.update(serviceMO);
		if (ServiceStatus.NEW.equals(serviceStatus) || ServiceStatus.UPDATED.equals(serviceStatus)) {
			if (ServiceStatus.NEW.equals(serviceStatus)) {
				LOGGER.info("Service ("+ serviceMO.getId() +"): " + serviceMO.getServiceDetail().getName() + " has a new version");
			}else {
				LOGGER.info("Service ("+ serviceMO.getId() +"): " + serviceMO.getServiceDetail().getName() + " has an updated version");
				oldService = serviceDAO.getRetrievedService(serviceMO.getId());
				String prefix = getPrefix(serviceMO.getId(),"");
				getDataManager().moveDeprecatedFilesWithPrefix(prefix);
			}

			folderName = folderCache.getFolderName(serviceMO.getServiceDetail().getFolderId());
			List<WsdlSchemaFileInfo> wsdlSchemaInfos = new ArrayList<WsdlSchemaFileInfo>();
			for (ResourceSet resourceSet : serviceMO.getResourceSets()) {
				String rootUrl = resourceSet.getRootUrl();
				for (Resource resource : resourceSet.getResources()) {
					String filename = null;
					if (resource.getSourceUrl() != null) {
						boolean isRoot = resource.getSourceUrl().equals(rootUrl);
						WsdlSchemaFileInfo wsdlSchemaFileInfo = new WsdlSchemaFileInfo(resource, isRoot);
						wsdlSchemaInfos.add(wsdlSchemaFileInfo);			
						numberOfXsdWsdls++;
					} else {
						filename = POLICY_XML;
						writeTempFile(filename, resource.getContent());

					}
					
				}
			}
			processFiles(wsdlSchemaInfos);

			File zipFile = new File(getDataManager().getZipsLocation(), folder.getName() + ZIP_SUFFIX);

			policyAnalyzer = new PolicyAnalyzer(getFile(POLICY_XML));
			ReportGenerator reportGenerator = new ReportGenerator( new Date());
			reportGenerator.parse(serviceMO, getFolderName(), getPolicyAnalyzer().getProtectedServiceUrl(), wsdlSchemaInfos, getTempFile("index.xml"));
			
			Util.zip(folder, zipFile, new NoPolicyFileFilter());
			rssLogger.addNewOrChangedService(this);
//			getDataManager().publishToRssDatadir(zipFile);
		}

		
	}

	/**
	 * @return the serviceMO
	 */
	public ServiceMO getNewChangedService() {
		return serviceMO;
	}
	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}
	
	
	/**
	 * @return the policyAnalyzer
	 */
	public PolicyAnalyzer getPolicyAnalyzer() {
		return policyAnalyzer;
	}
	/**
	 * @return the oldService
	 */
	public RetrievedService getOldService() {
		return oldService;
	}

	private void processFiles(List<WsdlSchemaFileInfo> wsdlSchemaInfos) throws FileNotFoundException, UnsupportedEncodingException{
		//List<String> filenames = new ArrayList<String>();
		int lowestPosition = -1;
		for (WsdlSchemaFileInfo temp: wsdlSchemaInfos){
			int position = temp.calculatePosition();
			if (lowestPosition == -1 || position < lowestPosition){
				lowestPosition = position;
			}
		}
		for (WsdlSchemaFileInfo temp: wsdlSchemaInfos){
			String url = temp.getUrl(lowestPosition);
			writeTempFile(url, temp.getResource().getContent());
			temp.setFilename(url);
		}

	}

}
