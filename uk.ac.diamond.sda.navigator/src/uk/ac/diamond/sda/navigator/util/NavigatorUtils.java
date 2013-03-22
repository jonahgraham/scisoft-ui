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
import java.util.List;

import org.dawb.common.util.io.FileUtils;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.IExtendedMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class NavigatorUtils {

	/**
	 * Method that returns a Scan Command given a .nxs, .hdf5, .srs, .dat file
	 * @param file
	 * @return a Scan Command as a String
	 * @throws Exception
	 */
	public static String getScanCommand(File file) throws Exception{
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

	private static final String scanCmdName = "scan_command";
	private static final String titleName = "title";

	/**
	 * Method that returns a matrix of String with scan commands and titles given the full path to a Nexus file.<br>
	 * Make sure that the file path is the one of a Nexus file.
	 * @param fullpath
	 * @return a String array of Scan commands and Titles
	 * @throws Exception
	 */
	public static String[][] getHDF5TitlesAndScanCmds(String fullpath) throws Exception {
		
		List<ILazyDataset> list = new HDF5Loader(fullpath).findDatasets(new String[] {scanCmdName, titleName}, 1, null);

		List<String> scans = new ArrayList<String>();
		List<String> titles = new ArrayList<String>();
		for (ILazyDataset d : list) {
			if (d instanceof StringDataset) {
				StringDataset sd = (StringDataset) d;
				String n = d.getName();
				if (n == null) {
					continue;
				}
				if (n.contains(scanCmdName)) {
					scans.add(sd.getString(0));
					if (scans.size() > titles.size() + 1)
						titles.add(null); // bulk out list
				} else if (n.contains(titleName)) {
					titles.add(sd.getString(0));
					if (titles.size() > scans.size() + 1)
						scans.add(null);
				}
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

		String[][] results = new String[2][s];
		for (int i = 0; i < s; i++) {
			String str = scans.get(i);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			results[0][i] = str == null ? "" : "\nScanCmd" + (i+1) + ": " + str;
			str = titles.get(i);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			results[1][i] = str == null ? "" : "\nTitle" + (i+1) + ": " + str;
		}

		return results;
	}

	/**
	 * Method that returns of the Scan Command of the nxs file being looked at.<br>
	 * If there are more than one scan command, it returns the first one<br>
	 * @param fullpath
	 * @return the Scan command as a String 
	 * @throws Exception
	 */
	public static String getHDF5ScanCommand(String fullpath) throws Exception {
		return getHDF5ScanCommandOrTitle(fullpath, scanCmdName);
	}

	/**
	 * Method that returns the title of the nxs file being looked at.<br>
	 * If there are more than one title, it returns the first one<br>
	 * @param fullpath
	 * @return a String 
	 * @throws Exception
	 */
	public static String getHDF5Title(String fullpath) throws Exception {
		return getHDF5ScanCommandOrTitle(fullpath, titleName);
	}

	private static String getHDF5ScanCommandOrTitle(String fullpath, String type) throws Exception {
		// make it work just for nxs and hdf5 files
		File node = new File(fullpath);
		if(!FileUtils.getFileExtension(node).equals("hdf5") 
				&& !FileUtils.getFileExtension(node).equals("hdf")
				&& !FileUtils.getFileExtension(node).equals("h5")
				&& !FileUtils.getFileExtension(node).equals("nxs")) return "";

		List<ILazyDataset> list = new HDF5Loader(fullpath).findDatasets(new String[] {type}, 1, null);

		List<String> scans = new ArrayList<String>();
		for (ILazyDataset d : list) {
			if (d instanceof StringDataset) {
				String n = d.getName();
				if (n == null) {
					continue;
				}
				if (n.contains(type)) {
					scans.add(d.toString());
					if(scans.size()>1) break; // get out of the loop if more than 1 scan command
				}
			}
		}

		String result = "N/A";
		if(scans.size()>0){
			String str = scans.get(0);
			if (str != null && str.length() > 100) { // restrict to 100 characters
				str = str.substring(0,  100) + "...";
			}
			if(scans.size() > 1){
				result = str == null ? "" : "1/"+list.size()+ ": "+str;
			} else if(scans.size() == 1) {
				result = str == null ? "" : str;
			}
		}
		return result;
	}

	/**
	 * Method that returns a title if the parameter is a nexus file, a comment if Ascii
	 * @param file
	 * @return a String
	 * @throws Exception
	 */
	public static String getComment(File file) throws Exception {
		String extension = FileUtils.getFileExtension(file);
		if(extension.equals("hdf5") 
				|| extension.equals("hdf")
				|| extension.equals("h5")
				|| extension.equals("nxs")) 
			return getHDF5Title(file.getAbsolutePath());
		return "";
	}

}
