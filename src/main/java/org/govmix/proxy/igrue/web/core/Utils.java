/*
 * ProxyIgrue - Reimplementazione free del Sender IGRUE del MEF 
 * http://igrue.gov4j.it
 * 
 * Copyright (c) 2009-2015 Link.it srl (http://link.it). 
 * Copyright (c) 2009 Provincia Autonoma di Bolzano (http://www.provincia.bz.it/). 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.govmix.proxy.igrue.web.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class Utils {








	private static Logger log = Logger.getLogger(Utils.class);
	
	protected static ClassLoader getCurrentClassLoader(Object defaultObject){
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		if(loader == null){
			loader = defaultObject.getClass().getClassLoader();
		}
		
		return loader;
	}

	public static String getMessageFromResourceBundle(
							String bundleName, 
							String key, 
							Object params[], 
							Locale locale){
		
		String text = null;
		
		ResourceBundle bundle = 
				ResourceBundle.getBundle(bundleName, locale, 
										getCurrentClassLoader(params));
		
		try{
			text = bundle.getString(key);
		} catch(MissingResourceException e){
			text = "?? key " + key + " not found ??";
		}
		
		if(params != null){
			MessageFormat mf = new MessageFormat(text, locale);
			text = mf.format(params, new StringBuffer(), null).toString();
		}
		
		return text;
	}
	
    public static String getMessageFromJSFBundle(String bundleName,String key) {
    	ResourceBundle rb = ResourceBundle.getBundle(bundleName);
		return rb.getString(key);
    	//return (String)resolveExpression("#{"+bundleName+"['" + key + "']}");
    }

    // from JSFUtils in Oracle ADF 11g Storefront Demo
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        return valueExp.getValue(elContext);
    }
	
    

    public static String prettifyXml(String xml) {
    	if(xml==null || "".equals(xml)) return "";
    	String res = "";
    	try{
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	Document document = builder.parse(new ByteArrayInputStream(xml.trim().getBytes()));
        	
            res = toXML(document);
    	}catch (Exception e) {
			log.error(e,e);
		}
    	return res;
    }
    
    public static String toXML(Node source) {

        String subscrXML=null;
        StringWriter stringWriter=new StringWriter();
         try {
            //Get the implementations

            DOMImplementationRegistry registry =  DOMImplementationRegistry.newInstance();

            DOMImplementationLS impls =  (DOMImplementationLS)registry.getDOMImplementation("LS");


            //Prepare the output
            LSOutput domOutput = impls.createLSOutput();
            domOutput.setEncoding(java.nio.charset.Charset.defaultCharset().name());            
            domOutput.setCharacterStream(stringWriter);
            domOutput.setEncoding("UTF-8");
            //Prepare the serializer
            LSSerializer domWriter = impls.createLSSerializer();            
            DOMConfiguration domConfig = domWriter.getDomConfig();
            domConfig.setParameter("format-pretty-print", true);
            domConfig.setParameter("element-content-whitespace", true);
            domWriter.setNewLine("\r\n");     
            domConfig.setParameter("cdata-sections", Boolean.TRUE);
            //And finaly, write
            domWriter.write(source, domOutput);
            subscrXML = domOutput.getCharacterStream().toString();
         } catch (Exception e) {
             e.printStackTrace();
         }
        return subscrXML;
     }
    
    
    public static byte[] readBytesFromFile(File f) throws Exception{
		FileInputStream fis =new FileInputStream(f);
		ByteArrayOutputStream byteInputBuffer = new ByteArrayOutputStream();
		byte [] readB = new byte[8192];
		int readByte = 0;
		while((readByte = fis.read(readB))!= -1){
			byteInputBuffer.write(readB,0,readByte);
		}
		fis.close();
		byteInputBuffer.flush();
		byteInputBuffer.close();
		
		return  byteInputBuffer.toByteArray();
	}
}
