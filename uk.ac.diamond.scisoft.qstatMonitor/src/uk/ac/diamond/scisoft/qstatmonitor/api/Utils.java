/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.qstatmonitor.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {

	private static Process process = null;

	public static void main(String[] args) {
		// System.out.println(getAbsoluteScriptPath());
		// getTableLists("qstat", "*");
	}

	/**
	 * gets the list of arrays of table items
	 * 
	 * @param argument
	 *            the qstat query
	 * @param userString
	 *            the user name pattern to filter the users by in the query
	 * @return
	 */
	public static ArrayList<String>[] getTableLists(String argument,
			String userString, ArrayList<String>[] lists) {
		String scriptDir = getAbsoluteScriptPath() + "getQStatXML.sh";
		return convertXMLToStringArrays(lists,
				runScriptAndGetOutput(scriptDir, argument, userString));
	}

	/**
	 * Runs given command with arguments and returns the output as a String
	 * 
	 * @param cmd
	 *            command to run, this will usually be the script that is ran
	 * @param argment
	 *            the first argument, this will be the main qstat query
	 * @param userString
	 *            the user name pattern to filter the users by in the query
	 * @return
	 */
	public static String runScriptAndGetOutput(String cmd, String argment,
			String userString) {
		String result = "";

		ProcessBuilder processBuilder;
		if (userString != null && !userString.equals("")) {
			processBuilder = new ProcessBuilder(cmd, argment, userString);
		} else {
			processBuilder = new ProcessBuilder(cmd, argment);
		}

		processBuilder.redirectErrorStream(true);
		try {
			process = processBuilder.start();
		} catch (IOException ioe) {
			System.out.println(ioe);
			process = null;
		}

		if (process != null) {
			BufferedReader brOut = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			try {
				while ((line = brOut.readLine()) != null) {
					result += "\n" + line;
				}
				brOut.close();
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
		}

		int startOfXmlIndex = result.indexOf("<?xml version='1.0'?");
		if (startOfXmlIndex == -1) {
			System.out.println("Can not find XML header.");
		} else {
			result = result.substring(startOfXmlIndex);
		}

		return result;
	}

	/**
	 * Gets the contents of the XML as arrays, using a different array for each
	 * XML tag
	 * 
	 * @param xmlString
	 *            the string of XML returned by running a qstat command
	 * @return an array of length 9, containing ArrayLists for items of each XML
	 *         tag
	 */
	public static ArrayList<String>[] convertXMLToStringArrays(
			ArrayList<String>[] lists, String xmlString) {
		// ArrayList<String>[] lists = (ArrayList<String>[]) new ArrayList[9];
		for (int i = 0; i < lists.length; i++) {
			lists[i] = new ArrayList<String>();
		}
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("job_list");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					lists[0].add(getTagValue("JB_job_number", eElement));
					lists[1].add(getTagValue("JAT_prio", eElement));
					lists[2].add(getTagValue("JB_name", eElement));
					lists[3].add(getTagValue("JB_owner", eElement));
					lists[4].add(getTagValue("state", eElement));
					lists[5].add(getTagValue("JB_submission_time", eElement));
					lists[6].add(getTagValue("queue_name", eElement));
					lists[7].add(getTagValue("slots", eElement));
					lists[8].add(getTagValue("tasks", eElement));
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing XML");
			return null;
		}

		return lists;
	}

	/**
	 * @param sTag
	 * @param eElement
	 * @return
	 */
	private static String getTagValue(String sTag, Element eElement) {
		try {
			NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
					.getChildNodes();
			Node nValue = (Node) nlList.item(0);
			return nValue.getNodeValue();
		} catch (NullPointerException e) {
			return "";
		}
	}

	/**
	 * Gets the absolute path of the script where it is been run from, this
	 * script folder is in the same directory as the scr folder
	 * 
	 * @return
	 */
	public static String getAbsoluteScriptPath() {

		java.security.ProtectionDomain pd = Utils.class.getProtectionDomain();
		if (pd == null) {
			System.out.print("No path found returning null");
			return null;
		}
		java.security.CodeSource cs = pd.getCodeSource();
		if (cs == null) {
			System.out.print("No path found returning null");
			return null;
		}
		java.net.URL url = cs.getLocation();
		if (url == null) {
			System.out.print("No path found returning null");
			return null;
		}
		java.io.File f = new File(url.getFile());
		if (f == null) {
			System.out.print("No path found returning null");
			return null;
		}

		String resultTest = f.getAbsolutePath();

		if (resultTest == null) {
			System.out.print("No path found, returning null");
			return null;
		}

		if (isFolder(resultTest + "/scripts")) { // not in script folder
			return resultTest + "/scripts/";
		} else {
			File file = new File(resultTest);
			return file.getParent() + "/scripts/";
		}

	}

	private static boolean isFolder(String input) {
		File file = new File(input);
		return file.isDirectory();
	}

}
