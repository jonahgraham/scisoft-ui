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
