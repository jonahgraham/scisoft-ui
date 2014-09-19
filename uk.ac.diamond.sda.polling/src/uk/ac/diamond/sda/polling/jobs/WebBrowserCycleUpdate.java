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

import uk.ac.diamond.sda.polling.views.URLPollView;

public class WebBrowserCycleUpdate extends FilenameReaderJob {
	
	public static final String MAX_PAGES_TO_CYCLE = "MaxPagesToCycle";
	public static final String URL_VIEW_NAME = "URLViewName";
	private int cycle = 0;
	
	public WebBrowserCycleUpdate() {
		super();
	}
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		try {	
			// get the end of the list
			int listEnd = filenames.size()-1;
			// check to make sure the cyclepoint is valid
			if((listEnd-cycle < 0) || cycle > Integer.parseInt(getJobParameters().get(MAX_PAGES_TO_CYCLE))) {
				// otherwise reset it to zero
				cycle = 0;
			}
			setStatus(URLPollView.setURL(filenames.get(listEnd-cycle),getJobParameters().get(URL_VIEW_NAME)));
			cycle++;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
