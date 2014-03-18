/*
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
	
	private void loadParameterFile() throws IOException{
		FileInputStream fin = new FileInputStream(parameterFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(bis));

			String line = null;
			while ((line = br.readLine()) != null) {
				loadParameterString(line);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}
	
	private void loadParameterString(String parameterString) {
		
		if (parameterString.contains("=")) {
			String[] parts = parameterString.split("=");
			put(parts[0].trim(), parts[1].trim());
		}

	}

	public File getParameterFile() {
		return parameterFile;
	}	
	
}
