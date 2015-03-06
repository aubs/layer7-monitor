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
package nl.denhaag.twb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import nl.denhaag.twb.layer7.exception.EnvironmentsException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Thread that runs
 * 
 * 
 */
public abstract class AbstractTask implements Runnable {
	private static final SimpleDateFormat LONG_STARTTIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat STARTTIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat INTERVAL_FORMATTER = new SimpleDateFormat("HH:mm:ss");
	private static final SimpleDateFormat LONG_INTERVAL_FORMATTER = new SimpleDateFormat("dd HH:mm:ss");
	private static final long MS_PER_SEC = 1000l; // Milliseconds in a second
	private static final long MS_PER_MINUTE = 60 * MS_PER_SEC;
	private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
	private static final long MS_PER_DAY = 24 * MS_PER_HOUR;

	private long interval = -1; // How long to pause in service loop
	private long startTime = -1;

	private static Logger LOGGER = Logger.getLogger(AbstractTask.class);
	private File baseDir;

	public AbstractTask(String baseDirName, String logDirname) throws FileNotFoundException, IOException {
		baseDir = new File(baseDirName);
		File logDir = new File(logDirname);
		logDir.mkdirs();
		System.setProperty("log.dir", logDir.getCanonicalPath());
		System.setProperty("base.dir", baseDir.getCanonicalPath());
		DOMConfigurator.configure(new File(baseDir, "conf/log4j.xml").getAbsolutePath());

	}

	private void init() {
		Calendar startTimeCalendar = null;
		Calendar intervalCalendar = null;
		this.interval = 0l;
		try {
			startTimeCalendar = parse(LONG_STARTTIME_FORMATTER, getStartTimeString());
		} catch (ParseException e) {
			try {
				startTimeCalendar = parse(STARTTIME_FORMATTER, getStartTimeString());
			} catch (ParseException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
		}
		this.startTime = startTimeCalendar.getTimeInMillis();
		try {
			intervalCalendar = parse(LONG_INTERVAL_FORMATTER, getIntervalString());
			this.interval += intervalCalendar.get(Calendar.DAY_OF_MONTH) * MS_PER_DAY;
		} catch (ParseException e) {
			try {
				intervalCalendar = parse(INTERVAL_FORMATTER, getIntervalString());
			} catch (ParseException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
		}
		this.interval += intervalCalendar.get(Calendar.SECOND) * MS_PER_SEC;
		this.interval += intervalCalendar.get(Calendar.MINUTE) * MS_PER_MINUTE;
		this.interval += intervalCalendar.get(Calendar.HOUR_OF_DAY) * MS_PER_HOUR;

	}

	private Calendar parse(SimpleDateFormat formatter, String dateTimeString) throws ParseException {
		Date time = formatter.parse(dateTimeString);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		return calendar;
	}

	/**
	 * The real functionality that is executed after an interval
	 * @throws Exception
	 */
	protected abstract void runInternal() throws EnvironmentsException;

	/**
	 * Get the start time string
	 * @return string with start time
	 */
	protected abstract String getStartTimeString();

	/**
	 * Get the interval string
	 * @return string with interval
	 */
	protected abstract String getIntervalString();

	/**
	 * This method runs, until the thread is interrupted. It execute the method
	 * runInternal depending on interval that is choosen.
	 */
	public final void run() {
		init();
		if (startTime > 0 && interval > 0) {
			while (startTime <= System.currentTimeMillis()) {
				startTime += interval;
			}
			// logSystemEnvironment();
			EnvironmentsException oldEnvsException = null;
			while (true) {

				try {
					long sleep = startTime - System.currentTimeMillis();
					if (sleep > 0) {
						LOGGER.info("Next run will be at: " + LONG_STARTTIME_FORMATTER.format(new Date(startTime)));
						Thread.sleep(sleep);
					}
					try {
						runInternal();
						oldEnvsException = null;
					} catch (EnvironmentsException e) {
						logException(oldEnvsException, e);
						oldEnvsException = e;
					}
					while (startTime <= System.currentTimeMillis()) {
						startTime += interval;
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		} else {
			LOGGER.error("Starttime or interval invalid");
		}
	}
	public static void logException(EnvironmentsException oldEnvsException, EnvironmentsException newEnvsException){
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
