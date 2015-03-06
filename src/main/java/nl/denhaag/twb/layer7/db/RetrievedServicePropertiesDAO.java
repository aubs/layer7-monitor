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
package nl.denhaag.twb.layer7.db;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import nl.denhaag.twb.layer7.Layer7MonitorTask;
import nl.denhaag.twb.util.Util;

import com.l7tech.gateway.api.ServiceDetail;
import com.l7tech.gateway.api.ServiceMO;

/**
 * Stores and reads information of retrieved services.
 * 
 *
 */
public class RetrievedServicePropertiesDAO {
	private static final SimpleDateFormat TIMESTAMP_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
	private static final String CHECKED_PROPERTIES = "-checked.properties";
	private static final String SERVICE_PREFIX = "service.";
	private static final String SERVICE_NAME = ".name";
	private static final String SERVICE_GLOBAL_VERSION = ".version.global";
	private static final String SERVICE_POLICY_VERSION = ".version.policy";
	private static final String SERVICE_TIMESTAMP = ".timestamp";	
	private File dataFile;
	private Properties newStatusProperties;
	private Properties deletedServicesProperties;
	private Properties currentStatusProperties;
	private Date timestamp;
	
	/**
	 * Default constructor
	 * 
	 * @param baseDir Layer7 monitor base dir
	 * @throws IOException
	 */
	public RetrievedServicePropertiesDAO(Date timestamp, File baseDir, String envName) throws IOException {
		dataFile = new File(baseDir, envName +CHECKED_PROPERTIES);
		currentStatusProperties = Util.loadProperties(dataFile);
		deletedServicesProperties = Util.loadProperties(dataFile);
		newStatusProperties = new Properties();
		this.timestamp = timestamp;
	}

	/**
	 * Update an Layer7 service object
	 * 
	 * @param serviceMO Layer7 service object
	 * @return Status of this object, f.i. new, updated, no change
	 */
	public ServiceStatus update(ServiceMO serviceMO) {
		ServiceDetail serviceDetail = serviceMO.getServiceDetail();
		String nameKey = SERVICE_PREFIX + serviceMO.getId() + SERVICE_NAME;
		String globalVersionKey = SERVICE_PREFIX + serviceMO.getId() + SERVICE_GLOBAL_VERSION;
		String policyVersionKey = SERVICE_PREFIX + serviceMO.getId() + SERVICE_POLICY_VERSION;
		String timestampKey = SERVICE_PREFIX + serviceMO.getId() + SERVICE_TIMESTAMP;
		ServiceStatus serviceStatus;
		Long newPolicyVersion = (Long) serviceDetail.getProperties().get("policyRevision");
		Integer newGlobalVersion = serviceMO.getVersion();
		if (currentStatusProperties.containsKey(nameKey)) {
			Integer previousGlobalVersion = new Integer(currentStatusProperties.getProperty(globalVersionKey));
			Integer previousPolicyVersion = new Integer(currentStatusProperties.getProperty(policyVersionKey));

			if (newGlobalVersion.intValue() > previousGlobalVersion.intValue()
					|| newPolicyVersion.intValue() > previousPolicyVersion.intValue()) {
				serviceStatus = ServiceStatus.UPDATED;
			} else {
				serviceStatus = ServiceStatus.NOCHANGE;
			}
			deletedServicesProperties.remove(nameKey);
			deletedServicesProperties.remove(globalVersionKey);
			deletedServicesProperties.remove(policyVersionKey);
			deletedServicesProperties.remove(timestampKey);
		} else {
			serviceStatus = ServiceStatus.NEW;

		}
		String timestamp = null;
		if (ServiceStatus.NOCHANGE.equals(serviceStatus)){
			timestamp = currentStatusProperties.getProperty(timestampKey);
		}
		if (timestamp == null){
			timestamp = TIMESTAMP_DATE.format(this.timestamp);
		}
		newStatusProperties.setProperty(nameKey, serviceDetail.getName());
		newStatusProperties.setProperty(globalVersionKey, newGlobalVersion.toString());
		newStatusProperties.setProperty(policyVersionKey, newPolicyVersion.toString());
		newStatusProperties.setProperty(timestampKey, timestamp);
		return serviceStatus;

	}

	/**
	 * Undo all possible deleted services
	 * @return List of deleted services
	 */
	public void undoPossibleDeletedServices() {
		Set<String> ids = new TreeSet<String>();
		for (Object keyObject: deletedServicesProperties.keySet()){
			String key = (String) keyObject;
			String id = key.substring(SERVICE_PREFIX.length());
			id = id.substring(0, id.indexOf("."));
			ids.add(id);
		}
		for (String id: ids){
			String nameKey = SERVICE_PREFIX + id + SERVICE_NAME;
			String globalVersionKey = SERVICE_PREFIX + id + SERVICE_GLOBAL_VERSION;
			String policyVersionKey = SERVICE_PREFIX + id + SERVICE_POLICY_VERSION;
			newStatusProperties.setProperty(nameKey, deletedServicesProperties.getProperty(nameKey));
			newStatusProperties.setProperty(globalVersionKey, deletedServicesProperties.getProperty(globalVersionKey));
			newStatusProperties.setProperty(policyVersionKey,deletedServicesProperties.getProperty(policyVersionKey));
		}
		deletedServicesProperties.clear();


	}
	/**
	 * Retrieves all services that are deleted in Layer7
	 * @return List of deleted services
	 */
	public List<RetrievedService> getDeletedServices() {
		Set<String> ids = new TreeSet<String>();
		List<RetrievedService> deletedServices = new ArrayList<RetrievedService>();
		for (Object keyObject: deletedServicesProperties.keySet()){
			String key = (String) keyObject;
			String id = key.substring(SERVICE_PREFIX.length());
			id = id.substring(0, id.indexOf("."));
			ids.add(id);
		}
		for (String id: ids){
			deletedServices.add(getRetrievedService(id));
		}
		return deletedServices;
	}


	/**
	 * Retrieve already retrieved services by id.
	 * 
	 * @param id Layer7 id
	 * @return Retrieved service
	 */
	public RetrievedService getRetrievedService(String id){
		RetrievedService service = new RetrievedService();
		String nameKey = SERVICE_PREFIX + id + SERVICE_NAME;
		String globalVersionKey = SERVICE_PREFIX + id + SERVICE_GLOBAL_VERSION;
		String policyVersionKey = SERVICE_PREFIX + id + SERVICE_POLICY_VERSION;
		String timestampKey = SERVICE_PREFIX + id + SERVICE_TIMESTAMP;
		Integer previousGlobalVersion = new Integer(currentStatusProperties.getProperty(globalVersionKey));
		Integer previousPolicyVersion = new Integer(currentStatusProperties.getProperty(policyVersionKey));
		service.setPolicyVersion(previousPolicyVersion);
		service.setVersion(previousGlobalVersion);
		service.setName(currentStatusProperties.getProperty(nameKey));
		service.setTimestamp(currentStatusProperties.getProperty(timestampKey));
		service.setId(new Integer(id));
		return service;
		
	}
	
	/**
	 * Save changes to the properties file.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		dataFile.getParentFile().mkdirs();
		Util.storeProperties(dataFile, newStatusProperties, Util.getImplementationTitle(Layer7MonitorTask.class) + " "
				+ Util.getImplementationVersion(Layer7MonitorTask.class));
	}
}
