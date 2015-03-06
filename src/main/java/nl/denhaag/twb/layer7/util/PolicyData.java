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

import nl.denhaag.twb.layer7.db.RetrievedServicePropertiesDAO;
import nl.denhaag.twb.layer7.rss.RSSLogger;

public abstract class PolicyData {
	private DataManager dataManager;

	public PolicyData(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * @return the dataManager
	 */
	protected DataManager getDataManager() {
		return dataManager;
	}
	protected static String getPrefix(String id, String name){
		return id + "-" + name;
	}
	public abstract void process(RetrievedServicePropertiesDAO serviceDAO, RSSLogger rssLogger) throws Exception;
}
