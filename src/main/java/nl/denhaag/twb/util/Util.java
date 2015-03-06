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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Utility class for all TWB projects.
 * 
 * 
 */
public class Util {
	private static final String FORBIDDEN_CHARACTERS_REPLACEMENT = "_";
	private static final String FORBIDDEN_CHARACTERS_REGEX = "[\\\\/:*?\"<>\\|]";
	private static final String UNKNOWN_VERSION = "unknown version";
	private static final String UNKNOWN_VENDOR = "unknown vendor";
	private static final String UNKNOWN_TITLE = "unknown title";
	private static Logger LOGGER = Logger.getLogger(Util.class);
	public static final String SEPARATOR = "/";

	/**
	 * Load a property file and handles the exception
	 * 
	 * @param file
	 *            File location
	 * @return Properties object
	 */
	public static Properties loadProperties(File file) {
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(file);
			properties.load(fis);
			fis.close();
		} catch (IOException ioe) {
			LOGGER.info(ioe.getMessage());
		}
		return properties;
	}

	/**
	 * Store properties to a file
	 * 
	 * @param file
	 *            File to store
	 * @param properties
	 *            Properties object
	 * @param title
	 *            Title of the properties file
	 * @throws IOException
	 */
	public static void storeProperties(File file, Properties properties, String title) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		if (title == null) {
			properties.store(fos, "");
		} else {
			properties.store(fos, title);
		}

		fos.flush();
		fos.close();
	}


	/**
	 * Zip a list of files.
	 * 
	 * @param sourceFiles
	 *            Files to be zipped.
	 * @param zipfile
	 *            The zip file that should be created.
	 */
	public static void zip(File directory, File zipfile, FileFilter fileFilter) {
		try {

			URI base = directory.toURI();
			ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipfile));
			
			zip(base, directory.listFiles(fileFilter), zipOutputStream,fileFilter);
			zipOutputStream.close();
		} catch (IOException ioe) {
			LOGGER.error(ioe.getMessage(), ioe);
		}
	}
	private static void zip(URI base, File[] sourceFiles,ZipOutputStream zipOutputStream, FileFilter fileFilter) throws IOException {
		for (File file : sourceFiles) {
			String name = base.relativize(file.toURI()).getPath();
			if (file.isFile()) {

				byte[] buffer = new byte[1024]; // Create a buffer for
												// copying
				int bytesRead;
				ZipEntry entry = new ZipEntry(name);
				zipOutputStream.putNextEntry(entry);
				FileInputStream fileInputStream = new FileInputStream(file); // Stream
																				// to
																				// read
																				// file
				while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					zipOutputStream.write(buffer, 0, bytesRead);
				}
				zipOutputStream.closeEntry();
				fileInputStream.close();
			} else if (file.isDirectory()){
				name = name.endsWith(SEPARATOR) ? name : name + SEPARATOR;
				zipOutputStream.putNextEntry(new ZipEntry(name));
				zip(base, file.listFiles(fileFilter),zipOutputStream, fileFilter);
			}else {
				LOGGER.warn("Did not zip: " + file.getAbsolutePath()
						+ ". It should be a real file, not a link or directory.");
			}

		}		
	}

	/**
	 * Delete a file or directory and all its children
	 * 
	 * @param file
	 *            File or directory to be deleted.
	 */
	public static void delete(File file) {
		delete(new File[] { file });
	}

	/**
	 * Delete a files or directories and all its children
	 * 
	 * @param files
	 *            Files or directories to be deleted.
	 */
	public static void delete(File[] files) {
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					// delete childs
					delete(files[i].listFiles());
				}
				files[i].delete();
			}
		}
	}

	/**
	 * Copy source file to destination
	 * 
	 * @param sourceFile
	 *            Source file
	 * @param targetFile
	 *            Destination file
	 * @throws Exception
	 */
	public static void copyFile(File sourceFile, File targetFile) throws Exception {
		FileInputStream fis = new FileInputStream(sourceFile);
		FileOutputStream fos = new FileOutputStream(targetFile);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Retrieve the name of the library from the meta-inf file.
	 * 
	 * @param clazz Class of the library
	 * @return Name of the library
	 */
	public static String getImplementationTitle(Class<?> clazz) {
		return getImplementationTitle(clazz, UNKNOWN_TITLE);
	}

	/**
	 * Retrieve the vendor of the library from the meta-inf file.
	 * 
	 * @param clazz Class of the library
	 * @return Vendor of the library
	 */
	public static String getImplementationVendor(Class<?> clazz) {
		return getImplementationVendor(clazz, UNKNOWN_VENDOR);
	}
	
	/**
	 * Retrieve the version of the library from the meta-inf file.
	 * 
	 * @param clazz Class of the library
	 * @return Version of the library
	 */
	public static String getImplementationVersion(Class<?> clazz) {
		return getImplementationVersion(clazz, UNKNOWN_VERSION);
	}

	private static String getImplementationTitle(Class<?> clazz, String defaultString) {
		String title = clazz.getPackage().getImplementationTitle();
		if (StringUtils.isBlank(title)) {
			title = defaultString;
		}
		return title;
	}

	private static String getImplementationVendor(Class<?> clazz, String defaultString) {
		String vendor = clazz.getPackage().getImplementationVendor();
		if (StringUtils.isBlank(vendor)) {
			vendor = defaultString;
		}
		return vendor;
	}

	private static String getImplementationVersion(Class<?> clazz, String defaultString) {
		String version = clazz.getPackage().getImplementationVersion();
		if (StringUtils.isBlank(version)) {
			version = defaultString;
		}
		return version;
	}

	/**
	 * Remove forbidden characters from filename.
	 * 
	 * @param filename Filename
	 * @return Filename without forbidden characters
	 */
	public static String removeForbiddenFileCharacters(String filename) {
		return filename.replaceAll(FORBIDDEN_CHARACTERS_REGEX, FORBIDDEN_CHARACTERS_REPLACEMENT);
	}
}
