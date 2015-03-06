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
package nl.denhaag.twb.layer7.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class PolicyXslt {

	private static final Logger LOGGER = Logger.getLogger(PolicyXslt.class);
	
	private static XsltExecutable getXsltExecutable(String xslUrl) throws SaxonApiException{
        ClassLoader classLoader = (ClassLoader) Thread.currentThread().getContextClassLoader();
        Source xsltSource = new StreamSource(classLoader.getResourceAsStream(xslUrl));	
        Processor processor = new Processor(false);
        Base64Extension resourcebundleExtension = new Base64Extension();
        processor.registerExtensionFunction(resourcebundleExtension);
        XsltCompiler compiler = processor.newXsltCompiler();
        return compiler.compile(xsltSource);
	}

    public static void generatePolicyReport(Writer writer, File xmlFile) throws SaxonApiException, IOException{
    	try {
	    	FileInputStream fileInputStream = new FileInputStream(xmlFile);
			Source xmlSource = new StreamSource(fileInputStream);
	    	XsltExecutable executable = getXsltExecutable("xsl/policy.xsl");
	        XsltTransformer transformer = executable.load();
	        transformer.setSource(xmlSource);
	        Serializer serializer = new Serializer();
	        serializer.setOutputWriter(writer);
	        transformer.setDestination(serializer);
	        transformer.transform();
	        fileInputStream.close();
    	}catch (Exception e){
    		LOGGER.error(e.getMessage());
    	}
    }
}
