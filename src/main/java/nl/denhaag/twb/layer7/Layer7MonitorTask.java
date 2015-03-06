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
package nl.denhaag.twb.layer7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import nl.denhaag.twb.layer7.db.RetrievedService;
import nl.denhaag.twb.layer7.db.RetrievedServicePropertiesDAO;
import nl.denhaag.twb.layer7.exception.EnvironmentsException;
import nl.denhaag.twb.layer7.rss.RSSLogger;
import nl.denhaag.twb.layer7.util.DataManager;
import nl.denhaag.twb.layer7.util.DeletedPolicyData;
import nl.denhaag.twb.layer7.util.FolderCache;
import nl.denhaag.twb.layer7.util.NewChangedPolicyData;
import nl.denhaag.twb.util.AbstractTask;
import nl.denhaag.twb.util.Util;

import org.apache.log4j.Logger;

import com.l7tech.gateway.api.Accessor;
import com.l7tech.gateway.api.Client;
import com.l7tech.gateway.api.ClientFactory;
import com.l7tech.gateway.api.FolderMO;
import com.l7tech.gateway.api.ServiceMO;

/**
 * Layer7 Monitor Thread
 * 
 * 
 */
public class Layer7MonitorTask extends AbstractTask {
	private static final String TRUE = "true";
	private static final String LAYER7_GATEWAY_GMS_URL = "layer7.gateway.gms-url";
	private static final String LAYER7_GATEWAY_PASSWORD = "layer7.gateway.password";
	private static final String LAYER7_GATEWAY_USER = "layer7.gateway.user";
	private static final String LAYER7MONITOR_LAYER7_ENV_NAME = "layer7monitor.layer7.name";
	private static final String LAYER7MONITOR_LAYER7_ENABLED = "layer7monitor.layer7.enabled";

	private static final String PROPERTIES_EXTENSION = ".properties";
	private static Logger LOGGER = Logger.getLogger(Layer7MonitorTask.class);
	private Properties globalLayer7Properties;
	private Map<String, Properties> layer7Properties = new HashMap<String, Properties>();
	private File baseDir;

	/**
	 * Constructor
	 * 
	 * @param baseDirName
	 *            Base directory name of Layer7 Monitor
	 * @param logDirname
	 *            Log directory name of Layer7 Monitor
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Layer7MonitorTask(String baseDirName, String logDirname) throws FileNotFoundException, IOException {
		super(baseDirName, logDirname);
		baseDir = new File(baseDirName);
		globalLayer7Properties = Util.loadProperties(new File(baseDir, "conf/layer7.properties"));
		File confDir = new File(baseDir, "conf/environments");
		for (File confFile : confDir.listFiles()) {
			String fileName = confFile.getName();
			if (confFile.isFile() && fileName.endsWith(PROPERTIES_EXTENSION)) {
				int index = fileName.lastIndexOf(PROPERTIES_EXTENSION);
				fileName = fileName.substring(0, index);
				Properties temp = Util.loadProperties(confFile);
				temp.put(LAYER7MONITOR_LAYER7_ENV_NAME, fileName);
				layer7Properties.put(fileName, temp);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.denhaag.twb.util.AbstractTask#getStartTimeString()
	 */
	protected String getStartTimeString() {
		return globalLayer7Properties.getProperty("layer7monitor.layer7.polling.starttime");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.denhaag.twb.util.AbstractTask#getIntervalString()
	 */
	protected String getIntervalString() {
		return globalLayer7Properties.getProperty("layer7monitor.layer7.polling.interval");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.denhaag.twb.util.AbstractTask#runInternal()
	 */
	protected void runInternal() throws EnvironmentsException {
		File baseDataLocation = new File(baseDir, "data");
		File baseRssDataLocation = new File(globalLayer7Properties.getProperty("layer7monitor.rss.data.dir"));
		File rssLocation = new File(globalLayer7Properties.getProperty("layer7monitor.rssdir"));
		rssLocation.mkdirs();
		String dataUrl = globalLayer7Properties.getProperty("layer7monitor.rss.data.url");
		Map<String, Exception> envExceptions = new HashMap<String, Exception>();
		for (Properties layer7EnvProperties : layer7Properties.values()) {
			String envName = layer7EnvProperties.getProperty(LAYER7MONITOR_LAYER7_ENV_NAME);
			printHeader("Checking layer7 environment: " + envName + " ...");
			if (TRUE.equalsIgnoreCase(layer7EnvProperties.getProperty(LAYER7MONITOR_LAYER7_ENABLED))) {
				Date timestamp = new Date();
				RSSLogger rssLogger = new RSSLogger(timestamp, envName, dataUrl, new File(rssLocation,
						envName+".xml"));
				File dataLocation = new File(baseDataLocation, envName);
				File rssDataLocation = new File(baseRssDataLocation, envName);				
				DataManager dataManager = new DataManager(dataLocation, rssDataLocation);
				Exception exception = null;
				try {
					RetrievedServicePropertiesDAO serviceDAO = new RetrievedServicePropertiesDAO(timestamp, baseDataLocation, envName);
					ClientFactory factory = ClientFactory.newInstance();
					factory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false);
					factory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false);
					factory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME,
							layer7EnvProperties.getProperty(LAYER7_GATEWAY_USER));
					factory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD,
							layer7EnvProperties.getProperty(LAYER7_GATEWAY_PASSWORD));
					Client client = factory.createClient(layer7EnvProperties.getProperty(LAYER7_GATEWAY_GMS_URL));
					Accessor<ServiceMO> serviceMOAccessor = client.getAccessor(ServiceMO.class);
					Accessor<FolderMO> folderMOAccessor =  client.getAccessor(FolderMO.class);
					FolderCache folderCache = new FolderCache(folderMOAccessor);
					boolean failed = false;
					Iterator<ServiceMO> serviceMOIterator = serviceMOAccessor.enumerate();
					try {
						while (serviceMOIterator.hasNext()) {
								ServiceMO serviceMO = serviceMOIterator.next();					
								NewChangedPolicyData policyData = dataManager.getPolicyData(serviceMO,folderCache);
								policyData.process(serviceDAO, rssLogger);
						}
					}catch (Exception e){
						exception = e;
						failed = true;
					}
					if (failed){
						serviceDAO.undoPossibleDeletedServices();
					}else {
						for (RetrievedService oldService : serviceDAO.getDeletedServices()) {
							DeletedPolicyData policyData = dataManager.getPolicyData(oldService);
							policyData.process(serviceDAO, rssLogger);
						}
					}
					serviceDAO.save();
					if (exception != null){
						throw exception;
					}
					printHeader("Checking layer7 environment: " + envName + " ...FINISHED");
				} catch (Exception e) {
					envExceptions.put(envName, e);
					printHeaderError("Checking layer7 environment: " + envName + " ...FAILED");
				} finally {
					dataManager.clean();
					rssLogger.writeRss();
				}
			} else {
				printHeader("Checking layer7 environment: " + envName + " ...DISABLED");
			}
		}
		if (envExceptions.size() > 0) {
			throw new EnvironmentsException(envExceptions);
		}
	}


	private static void printHeader(String string) {
		LOGGER.info("=====================================================================================================");
		LOGGER.info(string);
		LOGGER.info("=====================================================================================================");
	}
	private static void printHeaderError(String string) {
		LOGGER.error("=====================================================================================================");
		LOGGER.error(string);
		LOGGER.error("=====================================================================================================");
	}

}
