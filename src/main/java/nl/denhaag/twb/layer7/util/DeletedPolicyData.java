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

import org.apache.log4j.Logger;

import nl.denhaag.twb.layer7.db.RetrievedService;
import nl.denhaag.twb.layer7.db.RetrievedServicePropertiesDAO;
import nl.denhaag.twb.layer7.rss.RSSLogger;

public class DeletedPolicyData extends PolicyData{
	private static Logger LOGGER = Logger.getLogger(DeletedPolicyData.class);
	private RetrievedService oldService;
	public DeletedPolicyData (DataManager dataManager,RetrievedService oldService){
		super(dataManager);
		this.oldService = oldService;
	}
	
	@Override
	public void process(RetrievedServicePropertiesDAO serviceDAO, RSSLogger rssLogger) throws IOException {
		LOGGER.info("Service ("+ oldService.getId() +"): " + oldService.getName() + " deleted");
		String prefix = getPrefix(oldService.getId().toString(),"");
		rssLogger.addDeletedService(this);
		getDataManager().deleteFilesWithPrefix(prefix);
	}

	/**
	 * @return the oldService
	 */
	public RetrievedService getOldService() {
		return oldService;
	}
	
}
