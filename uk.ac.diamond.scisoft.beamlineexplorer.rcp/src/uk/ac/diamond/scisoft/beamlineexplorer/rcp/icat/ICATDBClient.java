/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.icat;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ICATDBClient {
	
	private static Logger logger = LoggerFactory.getLogger(ICATDBClient.class);
	
	public static Connection getConnection() {

		Connection conn = null;
		
		// Check whether properties file is present and contains all required
		// entries
		Properties properties = readConfigFile();

		// Get properties
		String dbusername = properties.getProperty("dbusername");
		String dbpassword = properties.getProperty("dbpassword");
		String dburl      = properties.getProperty("dburl");
	
		//TODO move to ICAT 4.2 when read only db instance available
		try {

			Class.forName("oracle.jdbc.driver.OracleDriver");

			// Creating the connection to the ICAT database
			conn = DriverManager.getConnection(dburl, dbusername, dbpassword);

			logger.info("Connection to ICAT 3.3 Database started up!");
		} catch (Exception e) {
			logger.error("Connection to ICAT 3.3 Database FAILED! ");
			e.printStackTrace();
		}
		
		return conn;
	}


/* *
 * Reads properties from configuration file.
 * @returns set of properties
 */
public static Properties readConfigFile() {
	Properties properties = null;

	boolean dbusernameVerified = false;
	boolean dbpasswordVerified = false;
	boolean dburlVerified = false;


	try {
	
		properties = new Properties();
		// this file resides in Diamond internal file system so the values are not visible in github
		properties.load(new FileInputStream("/dls_sw/apps/synchlink/apache-tomcat-7.0.30/icatproperties/icatdb.properties"));
		logger.debug("Properties file loaded!");	
		
		Enumeration<Object> keys = properties.keys();

		while (keys.hasMoreElements()) {
			String prop = (String)keys.nextElement();
			String val = properties.getProperty(prop);

			// check whether all required keys and (non null) values are present
			if ((prop != null) && (prop.equals("dbusername"))) {
				if ((val != null) && (val.length() > 0))
					dbusernameVerified = true;
			}
			if ((prop != null) && (prop.equals("dbpassword"))) {
				if ((val != null) && (val.length() > 0))
					dbpasswordVerified = true;
			}
			if ((prop != null) && (prop.equals("dburl"))) {
				if ((val != null) && (val.length() > 0))
					dburlVerified = true;
			}
			properties.setProperty(prop, val);
		}// end while
		
		
		// in case one of the keys/values is missing
		if (!dbusernameVerified)
			throw new Exception(
					"Please check icatdb.properties file to ensure that dbusername key is supplied e.g. 'dbusername=icatuser123'");
		if (!dbpasswordVerified)
			throw new Exception(
					"Please check icatdb.properties file to ensure that dbpassword key is supplied e.g. 'dbpassword=fjGH89f=0'");
		if (!dburlVerified)
			throw new Exception(
					"Please check icatdb.properties file to ensure that dburl key is supplied e.g. 'dburl=dbhost:1254//db'");
	} catch (Exception io) {
		logger.error("Error loading properties file: " + io.getMessage());
	}// end try/catch

	return properties;
}

}

