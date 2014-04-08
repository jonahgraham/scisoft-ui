/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.sda.navigator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import ncsa.hdf.object.Dataset;

import org.dawb.common.util.io.FileUtils;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IExtendedMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class NavigatorUtils {

	private static final String scanCmdName = "scan_command";
	private static final String titleName = "title";

	private static final Logger logger = LoggerFactory.getLogger(NavigatorUtils.class);

	/**
	 * Method that returns a Scan Command given a .nxs, .hdf5, .srs, .dat file
	 * @param file
	 * @return a Scan Command as a String
	 */
	public static String getScanCommand(File file) {
		String extension = FileUtils.getFileExtension(file);
		if(extension.equals("dat") 
				|| extension.equals("srs")) 
			return getASCIIScanCommand(file);
		else if(extension.equals("hdf5") 
				|| extension.equals("hdf")
				|| extension.equals("h5")
				|| extension.equals("nxs")) 
			return getHDF5ScanCommand(file.getAbsolutePath());
		else return "";
	}

	/**
	 * Method that returns a Scan Command given a particular file<br>
	 * Files with .dat and .srs extensions are supported. So are .nxs files.<br>
	 * @param file
	 * @return a String value of the scan command
	 */
	public static String getASCIIScanCommand(File file) {
		// make it work just for srs files
		if(!FileUtils.getFileExtension(file).equals("dat") && !FileUtils.getFileExtension(file).equals("srs")) return "";
		String result = "N/A";
		IExtendedMetadata metaData = null;
		try {
			IMetaData metaDataTest=LoaderFactory.getMetaData(file.getAbsolutePath(), null);
			if(metaDataTest instanceof IExtendedMetadata){
				metaData = (IExtendedMetadata)LoaderFactory.getMetaData(file.getAbsolutePath(), null);
				if(metaData == null) return result;
				result = metaData.getScanCommand();
			}
		} catch (Exception ne) {
			ne.printStackTrace();
			return result;
		}
		return result;
	}

	/**
	 * Method that returns a matrix of String with scan commands and titles given the full path to a Nexus file.<br>
	 * Make sure that the file path is the one of a Nexus file.
	 * @param fullpath
	 * @return a String array of Scan commands and Titles
	 */
	public static String[][] getHDF5TitlesAndScanCmds(String fullpath) {

		String[][] results = {{""}, {""}};
		List<String> scans = new ArrayList<String>();
		List<String> titles = new ArrayList<String>();

		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(fullpath);
		} catch (Exception e) {
			logger.debug("HDF5 file Reading Exception:", e);
			return results;
		}
		if (file == null) return null;
		Enumeration<?> rootEntries = file.getNode().children();
		while (rootEntries.hasMoreElements()) {
			Object elem = rootEntries.nextElement();
			TreeNode node = (TreeNode) elem;
			String entryName = node.toString();

			//read scan command
			Dataset scanCmdData;
			try {
				scanCmdData = (Dataset) file.getData(entryName + "/" + scanCmdName);

				if (scanCmdData != null) {
					Object val = scanCmdData.read();
					String[] scan = (String[]) val;
					scans.add(scan[0]);
					if (scans.size() > titles.size() + 1)
						titles.add(null); // bulk out list
				}

				// read title
				Dataset titleData = (Dataset) file.getData(entryName + "/" + titleName);
				if (titleData != null) {
					Object val = titleData.read();
					String[] title = (String[]) val;
					titles.add(title[0]);
					if (titles.size() > scans.size() + 1)
						scans.add(null);
				}
			} catch (Exception e) {
				logger.debug("Error getting the data from the HDF5 tree:", e);
				return results;
			}
		}

		int s = scans.size();
		int t = titles.size();
		if (s != t) {
			// correct size of lists
//			logger.warn("Scans and titles not in sync!");
			while (s < t) {
				scans.add(null);
				s++;
			}
			while (t < s) {
				titles.add(null);
				t++;
			}
		}

		results = new String[2][s];
		for (int i = 0; i < s; i++) {
			String str = titles.get(i);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			if(i==0)
				results[0][i] = str == null ? "" : " * Title" + (i+1) + ": " + str;
			else
				results[0][i] = str == null ? "" : System.getProperty("line.separator")+"Title" + (i+1) + ": " + str;
			str = scans.get(i);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			results[1][i] = str == null ? "" : System.getProperty("line.separator")+"ScanCmd" + (i+1) + ": " + str;
		}
		try {
			file.close();
		} catch (Exception e) {
			logger.debug("Error closing HDF5 file:", e);
		}
		return results;
	}

	/**
	 * Method that returns of the Scan Command of the nxs file being looked at.<br>
	 * If there are more than one scan command, it returns the first one<br>
	 * @param fullpath
	 * @return the Scan command as a String 
	 */
	public static String getHDF5ScanCommand(String fullpath) {
		return getHDF5ScanCommandOrTitle(fullpath, scanCmdName, null);
	}

	/**
	 * Method that returns of the Scan Command of the nxs file being looked at.<br>
	 * If there are more than one scan command, it returns the first one<br>
	 * @param fullpath
	 * @param h5File 
	 * @return the Scan command as a String 
	 */
	public static String getHDF5ScanCommand(String fullpath, IHierarchicalDataFile h5File) {
		return getHDF5ScanCommandOrTitle(fullpath, scanCmdName, h5File);
	}

	/**
	 * Method that returns the title of the nxs file being looked at.<br>
	 * If there are more than one title, it returns the first one<br>
	 * @param fullpath
	 * @param h5File 
	 * @return a String 
	 */
	public static String getHDF5Title(String fullpath, IHierarchicalDataFile h5File) {
		return getHDF5ScanCommandOrTitle(fullpath, titleName, h5File);
	}

	private static String getHDF5ScanCommandOrTitle(String fullpath, String type, IHierarchicalDataFile h5File) {
		// make it work just for nxs and hdf5 files
		if(!fullpath.endsWith(".hdf5") 
				&& !fullpath.endsWith(".hdf")
				&& !fullpath.endsWith(".h5")
				&& !fullpath.endsWith(".nxs")) return "";

		String result = "N/A";
		List<String> comments = new ArrayList<String>();

		if (h5File == null)
			return result;
		Enumeration<?> rootEntries = h5File.getNode().children();
		if (!rootEntries.hasMoreElements())
			return result;
		Object elem = rootEntries.nextElement();
		TreeNode node = (TreeNode) elem;
		String entryName = node.toString();

		// read scan command /title
		Dataset commentData;
		try {
			commentData = (Dataset) h5File.getData(entryName + "/" + type);
			if (commentData != null) {
				Object val = commentData.read();
				String[] comment = (String[]) val;
				comments.add(comment[0]);
			}
		} catch (Exception e) {
			logger.error("Error getting data from HDF5 tree:", e);
			return "";
		}


		int s = comments.size();
		if (s > 0) {
			String str = comments.get(0);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			result = str == null ? result : str;
		}
		return result;
	}

	/**
	 * Method that returns a title if the parameter is a nexus file, a comment if Ascii
	 * @param file
	 * @return a String
	 */
	public static String getComment(File file) {
		return getHDF5Title(file.getAbsolutePath(), null);
	}

	/**
	 * Method that returns a title if the parameter is a nexus file, a comment if Ascii
	 * @param file
	 * @param h5File
	 * @return a String
	 */
	public static String getComment(File file, IHierarchicalDataFile h5File) {
		String extension = FileUtils.getFileExtension(file);
		if(extension.equals("hdf5") 
				|| extension.equals("hdf")
				|| extension.equals("h5")
				|| extension.equals("nxs")) 
			return getHDF5Title(file.getAbsolutePath(), h5File);
		return "";
	}

}
