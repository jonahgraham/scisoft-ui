/*
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.icat;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

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
	ResourceBundle bundle = null;
	Properties properties = null;

	boolean dbusernameVerified = false;
	boolean dbpasswordVerified = false;
	boolean dburlVerified = false;


	try {

		//bundle = new PropertyResourceBundle(ICATDBClient.class.getResourceAsStream("/conf/icatdb.properties"));

		logger.debug("Properties file loaded!");
		
		properties = new Properties();
		properties.load(new FileInputStream("/dls_sw/apps/apache-tomcat-7.0.30/icatproperties/icatdb.properties"));

//		Enumeration<String> keys = bundle.getKeys();
//
//		while (keys.hasMoreElements()) {
//			String prop = keys.nextElement();
//			String val = bundle.getString(prop);
//
//			// check whether all required keys and (non null) values are present
//			if ((prop != null) && (prop.equals("dbusername"))) {
//				if ((val != null) && (val.length() > 0))
//					dbusernameVerified = true;
//			}
//			if ((prop != null) && (prop.equals("dbpassword"))) {
//				if ((val != null) && (val.length() > 0))
//					dbpasswordVerified = true;
//			}
//			if ((prop != null) && (prop.equals("dburl"))) {
//				if ((val != null) && (val.length() > 0))
//					dburlVerified = true;
//			}
//			properties.setProperty(prop, val);
//		}// end while
			
		
		// in case one of the keys/values is missing
//		if (!dbusernameVerified)
//			throw new Exception(
//					"Please check icatdb.properties file to ensure that dbusername key is supplied e.g. 'dbusername=icatuser123'");
//		if (!dbpasswordVerified)
//			throw new Exception(
//					"Please check icatdb.properties file to ensure that dbpassword key is supplied e.g. 'dbpassword=fjGH89f=0'");
//		if (!dburlVerified)
//			throw new Exception(
//					"Please check icatdb.properties file to ensure that dburl key is supplied e.g. 'dburl=dbhost:1254//db'");
	} catch (Exception io) {
//		io.printStackTrace();
	}// end try/catch

	return properties;
}
}

