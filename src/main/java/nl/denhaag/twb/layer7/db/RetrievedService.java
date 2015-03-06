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

/**
 * Contains information about a retrieved service
 * 
 *
 */
public class RetrievedService {

	private Integer id;
	private String name;
	private Integer version;
	private Integer policyVersion;
	private String timestamp;
	/**
	 * Retrieves Layer7 id of the service
	 * @return Layer7 id of the service
	 */
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * Retrieves Layer7 name of the service 
	 * @return Layer7 name of the service 
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Retrieves Layer7 global version of the service
	 * @return version Layer7 global version of the service
	 */
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
	 * Retrieves Layer7 policy version of the service
	 * 
	 * @return Layer7 policy version of the service
	 */
	public Integer getPolicyVersion() {
		return policyVersion;
	}
	public void setPolicyVersion(Integer policyVersion) {
		this.policyVersion = policyVersion;
	}
	/**
	 * @return the timestamp
	 */
	protected String getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	protected void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	

}
