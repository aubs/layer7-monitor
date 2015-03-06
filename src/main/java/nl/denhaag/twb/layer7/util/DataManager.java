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
import java.io.FilenameFilter;

import nl.denhaag.twb.layer7.db.RetrievedService;
import nl.denhaag.twb.util.Util;

import com.l7tech.gateway.api.ServiceMO;

/**
 * Manage the date of the Layer7 services
 *
 */
public class DataManager {



	
	private File webDumpsOldLocation;
	private File webDumpsRemovedLocation;
	private File zipsDumpsOldLocation;
	private File zipsDumpsRemovedLocation;	
	private File zipsLocation;
	private File webLocation;
	/**
	 * Constructor
	 * @param dataLocation Location for reading and storing data.
	 * @param rssDataLocation Backup location
	 */
	public DataManager(File dataLocation, File rssDataLocation){
		zipsLocation = new File(rssDataLocation, "zips");
		zipsLocation.mkdirs();	
		webLocation = new File(rssDataLocation, "web");
		webLocation.mkdirs();			
		webDumpsOldLocation = new File(webLocation, "OLD");
		webDumpsOldLocation.mkdirs();
		webDumpsRemovedLocation = new File(webLocation, "REMOVED");
		webDumpsRemovedLocation.mkdirs();
		zipsDumpsOldLocation = new File(zipsLocation, "OLD");
		zipsDumpsOldLocation.mkdirs();
		zipsDumpsRemovedLocation = new File(zipsLocation, "REMOVED");
		zipsDumpsRemovedLocation.mkdirs();
	}




	/**
	 * @return the zipsLocation
	 */
	protected File getZipsLocation() {
		return zipsLocation;
	}




	/**
	 * @return the webLocation
	 */
	protected File getWebLocation() {
		return webLocation;
	}




	public NewChangedPolicyData getPolicyData(ServiceMO serviceMO, FolderCache folderCache){
		return new NewChangedPolicyData(this, serviceMO,folderCache);
	}
	public DeletedPolicyData getPolicyData(RetrievedService oldService){
		return new DeletedPolicyData(this, oldService);
	}

	/**
	 * Clean up files
	 */
	public void clean(){
		//Util.delete(dumpsTempLocation);
	}
	protected void publishToRssDatadir(File sourceFile) throws Exception{
		File destFile = new File(webLocation, sourceFile.getName());
		Util.copyFile(sourceFile, destFile);
	
	}

	protected void moveDeprecatedFilesWithPrefix(String prefix){
		File[] filesToMove = webLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(webDumpsOldLocation, fileToMove.getName()));
		}
		filesToMove = zipsLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(zipsDumpsOldLocation, fileToMove.getName()));
		}
	}
	protected void deleteFilesWithPrefix(String prefix){
		File[] filesToMove = webLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(webDumpsRemovedLocation, fileToMove.getName()));
		}
		filesToMove = webDumpsOldLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(webDumpsRemovedLocation, fileToMove.getName()));
		}
		filesToMove = zipsLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(zipsDumpsRemovedLocation, fileToMove.getName()));
		}
		filesToMove = zipsDumpsOldLocation.listFiles(new PrefixFilenameFilter(prefix));
		for (File fileToMove: filesToMove){
			fileToMove.renameTo(new File(zipsDumpsRemovedLocation, fileToMove.getName()));
		}
	
	}	

	
	private static class PrefixFilenameFilter implements FilenameFilter {

		private String prefix;
		public PrefixFilenameFilter(String prefix){
			this.prefix = prefix;
		}

		public boolean accept(File dir, String name) {
			return name.startsWith(prefix);
		}
		
	}
}
