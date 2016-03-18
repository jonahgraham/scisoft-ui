/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.feedback.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;

/**
 * 
 */
public class FeedbackUtils {

	/**
	 * size unit
	 * @param value
	 * @return the string value with unit
	 */
	public static String getValueWithUnit(long value){
		if (((value / 1000) > 1) && ((value / 1000) < 1000))
			return String.valueOf(value / 1000) + "KB";
		else if ((value / 1000) > 1000)
			return String.valueOf(value / 1000000) + "MB";
		else
			return String.valueOf(value) + "B";
	}

	/**
	 * Copy file using java nio, if file exists, it will overwrite it.
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (destFile.exists())
			destFile.delete();
		destFile.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;
		FileInputStream filesource = new FileInputStream(sourceFile);
		FileOutputStream filedestination = new FileOutputStream(destFile);
		try {
			source = filesource.getChannel();
			destination = filedestination.getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null)
				source.close();
			if (destination != null)
				destination.close();
			filesource.close();
			filedestination.close();
		}
	}

	/**
	 * 
	 * @return true if Windows
	 */
	public static boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}
}
