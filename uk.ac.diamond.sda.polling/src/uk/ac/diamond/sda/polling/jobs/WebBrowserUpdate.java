/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.jobs;

import java.util.ArrayList;

import uk.ac.diamond.sda.polling.jobs.FilenameReaderUpdateOnlyJob;
import uk.ac.diamond.sda.polling.views.URLPollView;

public class WebBrowserUpdate extends FilenameReaderUpdateOnlyJob {
	
	public static final String URL_VIEW_NAME = "URLViewName";
	
	public WebBrowserUpdate() {
		super();
	}
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		try {			
			// only process the first file
			setStatus(URLPollView.setURL(filenames.get(0),getJobParameters().get(URL_VIEW_NAME)));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
