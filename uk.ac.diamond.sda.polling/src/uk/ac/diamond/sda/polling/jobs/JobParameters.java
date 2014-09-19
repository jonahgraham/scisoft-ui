/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.jobs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class JobParameters extends HashMap<String, String> {

	private static final long serialVersionUID = -7318893897906814738L;

	private File parameterFile;

	private long lastModified;
	
	public JobParameters(String fileName) throws IOException {
		parameterFile = new File(fileName);
		loadParameterFile();

		// initialise the last modified flag
		lastModified = parameterFile.lastModified();
	}

	public void refresh() throws IOException {
		long newLastModified = parameterFile.lastModified();
		if (lastModified != newLastModified) {
			loadParameterFile();
			lastModified = newLastModified;
		}
	}
	
	private void loadParameterFile() throws IOException {
		FileInputStream fin = new FileInputStream(parameterFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		BufferedReader br = new BufferedReader(new InputStreamReader(bis));
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				loadParameterString(line);
			}
		} finally {
			br.close();
		}
	}

	private void loadParameterString(String parameterString) {

		if (parameterString.contains("=")) {
			String[] parts = parameterString.split("=");
			if (parts.length > 1)
				put(parts[0].trim(), parts[1].trim());
		}

	}

	public File getParameterFile() {
		return parameterFile;
	}	
	
}
