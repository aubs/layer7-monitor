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
package nl.denhaag.twb.layer7.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class XMLTransformer {

    public static void transformClassloader(File inputFile, File outputFile, String xsltLocation)
            throws TransformerException {
        transformClassloader(inputFile, outputFile, xsltLocation, null);

    }

    public static void transformClassloader(File inputFile, File outputFile, String xsltLocation, String parameter, Object value)
            throws TransformerException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(parameter, value);
        transformClassloader(inputFile, outputFile, xsltLocation, parameters);

    }

    public static void transformClassloader(File inputFile, File outputFile, String xsltLocation, Map<String, Object> parameters)
            throws TransformerException {
        //parameters.put("voorschriftenVersion", VOORSCHRIFTEN_VERSION);
        //parameters.put("toolVersion", TOOL_VERSION);
        //System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        ClassLoader classLoader = (ClassLoader) Thread.currentThread().getContextClassLoader();
        Source xsltSource = new StreamSource(classLoader.getResourceAsStream(xsltLocation));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xsltSource);
        Source inputSource = new StreamSource(inputFile);
        Result outputSource = new StreamResult(outputFile);
        XsltErrorListener errorListener = new XsltErrorListener();
        transformer.setErrorListener(errorListener);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
        }
        transformer.transform(inputSource, outputSource);

    }

    public static void transformFile(File inputFile, File outputFile, String xsltLocation, Map<String, Object> parameters)
            throws TransformerException, IOException {
        //System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        Source xsltSource = new StreamSource(new File(xsltLocation));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xsltSource);
        FileInputStream inputStream = new FileInputStream(inputFile);
        Source inputSource = new StreamSource(inputStream);
        Result outputSource = new StreamResult(outputFile);
        XsltErrorListener errorListener = new XsltErrorListener();
        transformer.setErrorListener(errorListener);
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
        }
        transformer.transform(inputSource, outputSource);
        inputStream.close();
    }

    private static class XsltErrorListener implements ErrorListener {


        public void warning(TransformerException exception) throws TransformerException {
            throw exception;

        }

        public void error(TransformerException exception) throws TransformerException {
            throw exception;
        }

        public void fatalError(TransformerException exception) throws TransformerException {
            throw exception;
        }
    }
}
