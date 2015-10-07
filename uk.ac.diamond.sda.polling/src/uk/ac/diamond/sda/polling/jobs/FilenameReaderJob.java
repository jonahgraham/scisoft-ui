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
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.diamond.sda.polling.Activator;

public abstract class FilenameReaderJob extends AbstractPollJob {
	
	public static final String FILE_NAME = "FileName";

	public FilenameReaderJob() {
		super("Filename Reader Job");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			File file = new File(getJobParameters().get(FILE_NAME));
			FileInputStream fin = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fin);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			
			ArrayList<String> filenames = new ArrayList<String>();
			
			String filename = br.readLine();
			// if there is nothing there, throw an exception here to let the user know
			if(filename == null) {
				br.close();
				throw new IOException("No File Specified in drop location");
			}
			
			// otherwise try to load in all the image filenames
			while(filename != null) {
				filenames.add(filename);
				filename = br.readLine();
			}
			br.close();
			processFile(filenames);
		
		} catch (Exception e) {
			setStatus(e.getLocalizedMessage());
			return new Status(IStatus.INFO, Activator.PLUGIN_ID, e.getLocalizedMessage());
		}
		
		setStatus("OK");
		return Status.OK_STATUS;
		
	}

	protected abstract void processFile(ArrayList<String> filenames);
}
