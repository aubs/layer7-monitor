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
package nl.denhaag.twb.layer7.exception;

import java.util.HashMap;
import java.util.Map;

import nl.denhaag.twb.util.AbstractTask;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

public class EnvironmentsExceptionTest {
	private static Logger LOGGER = Logger.getLogger(AbstractTask.class);
	@After
	public void tearDown() throws Exception {
	}

	private Map<String, Exception> getExceptions(){
		Map<String, Exception> envExceptions = new HashMap<String, Exception>();
		envExceptions.put("test", new Exception("This is a YES"));
		envExceptions.put("dev", new Exception("This is a test"));
		return envExceptions;
	}
//	@Test
//	public void testExceptionOnlyNew() {
//		Map<String, Exception>  envExceptions = getExceptions();
//		EnvironmentsException newEnvsException = new EnvironmentsException(envExceptions);
//		logException(null, newEnvsException);
//
//	}
//	@Test
//	public void testExceptionAdded() {
//		EnvironmentsException oldEnvsException = new EnvironmentsException(getExceptions());
//		Map<String, Exception>  envExceptions = getExceptions();
//		envExceptions.put("acc", new Exception("Hello"));
//		EnvironmentsException newEnvsException = new EnvironmentsException(envExceptions);
//		logException(oldEnvsException, newEnvsException);
//	}
//	@Test
//	public void testExceptionNoChange() {
//		EnvironmentsException oldEnvsException = new EnvironmentsException(getExceptions());
//		EnvironmentsException newEnvsException = new EnvironmentsException(getExceptions());
//		logException(oldEnvsException, newEnvsException);
//	}
	@Test
	public void testExceptionRemoved() {
		EnvironmentsException oldEnvsException = new EnvironmentsException(getExceptions());
		Map<String, Exception> envExceptions = getExceptions();
		envExceptions.remove(0);
		EnvironmentsException newEnvsException = new EnvironmentsException(envExceptions);
		logException(oldEnvsException, newEnvsException);
	}

	public void logException(EnvironmentsException oldEnvsException, EnvironmentsException newEnvsException){
		if (oldEnvsException == null && newEnvsException != null){
			/*
			 * all exceptions are new
			 */
			for (Map.Entry<String,Exception> entry: newEnvsException.getEnvExceptions().entrySet()){
				LOGGER.error(entry.getKey() + ": " + entry.getValue().getMessage(), entry.getValue());
			}
		}else if (oldEnvsException != null && newEnvsException != null){
			for (Map.Entry<String,Exception> entry: newEnvsException.getEnvExceptions().entrySet()){
				Exception oldException = oldEnvsException.getEnvExceptions().get(entry.getKey());
				Exception newException = entry.getValue();
				/*
				 * check if exception already exist and is the same
				 */
				if (oldException != null && newException.getClass().equals(oldException.getClass())) {
					LOGGER.error(entry.getKey() + ": " + newException.getMessage());
				} else {
					LOGGER.error(entry.getKey() + ": " + newException.getMessage(), newException);
				}
			}			
		}
	}

}
