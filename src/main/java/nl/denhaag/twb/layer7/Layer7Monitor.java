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

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.denhaag.twb.layer7.exception.EnvironmentsException;
import nl.denhaag.twb.util.Util;

import org.apache.log4j.Logger;


/**
 * Main class for Layer7 monitor
 * 
 *
 */
public class Layer7Monitor{
	private static volatile Thread thrd; // start and stop are called from
	private static Logger LOGGER = Logger.getLogger(Layer7Monitor.class);

	/**
	 * Main method to start the Layer7 monitor standalone (not as a windows services
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Layer7MonitorTask task = new Layer7MonitorTask(getArg(args, 0), getArg(args,1));
		try {
			task.runInternal();
		} catch (EnvironmentsException e) {
			Layer7MonitorTask.logException(null, e);
		}
	}

	/**
	 * Start the Layer7 Monitor service, and waits for it to complete.
	 * 
	 * @param args
	 *            arg[0] = base dir of the Layer7 monitor
	 *            arg[1] = log dir of the Layer7 monitor
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void start(String[] args) throws FileNotFoundException, IOException {
		thrd = new Thread(new Layer7MonitorTask(getArg(args, 0), getArg(args,1)));
		String title = Util.getImplementationTitle(Layer7MonitorTask.class);
		String version = Util.getImplementationVersion(Layer7MonitorTask.class);
		LOGGER.info("Starting " + title + " " + version);
		thrd.start();
		while (thrd.isAlive()) {
			try {
				thrd.join();
			} catch (InterruptedException ie) {
				// Ignored
			}
		}
	}


	/**
	 * Stops the Layer7 monitor service
	 * @param args
	 */
	public static void stop(String[] args) {
		String title = Util.getImplementationTitle(Layer7MonitorTask.class);
		String version = Util.getImplementationVersion(Layer7MonitorTask.class);
		if (thrd != null) {
			LOGGER.info("Stopping " + title + " " + version);
			thrd.interrupt();
		} else {
			LOGGER.info("Unable to stop " + title + " " + version);
		}
	}


	private static String getArg(String[] args, int argnum) {
		if (args.length > argnum) {
			return args[argnum];
		} else {
			return null;
		}
	}
}
