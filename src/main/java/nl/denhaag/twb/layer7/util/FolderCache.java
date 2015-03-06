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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.l7tech.gateway.api.Accessor;
import com.l7tech.gateway.api.Accessor.AccessorException;
import com.l7tech.gateway.api.FolderMO;

/**
 * Cache all folder request. So the number of request are minimized
 *
 */
public class FolderCache {
	private Accessor<FolderMO> folderMOAccessor;
	private Map<String, String> folderCache = new HashMap<String, String>();

	public FolderCache(Accessor<FolderMO> folderMOAccessor){
		this.folderMOAccessor = folderMOAccessor;
	}
	public String getFolderName(String id) throws AccessorException{
		String folderName = folderCache.get(id);
		if (folderName == null){
			FolderMO folderMO = folderMOAccessor.get(id);
			if (StringUtils.isBlank(folderMO.getFolderId())){
				folderName =  "/";			
			}else {
				folderName = getFolderName(folderMO.getFolderId())  +  folderMO.getName()+ "/";
			}
			folderCache.put(id, folderName);
		}

		return folderName;

	}
}
