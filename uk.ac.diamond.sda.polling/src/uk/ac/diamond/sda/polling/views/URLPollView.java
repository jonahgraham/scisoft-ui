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

package uk.ac.diamond.sda.polling.views;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class URLPollView extends ViewPart {

	private static HashMap<String, Browser> browsers = new HashMap<String, Browser>();
	private Browser browser = null;
	
	public URLPollView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		this.getTitle();
		browser = new Browser(parent, SWT.NONE);
		browsers.put(this.getTitle(), browser);
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	public static String setURL(final String URL, final String browserName) {
		UIJob uiJob = new UIJob("Updating URL") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (browsers.get(browserName) != null) {
					try {
						browsers.get(browserName).setUrl(URL);
						return Status.OK_STATUS;
					} catch (Exception e) {
						
					}
				}
				return Status.CANCEL_STATUS;
			}
		};
		
		uiJob.schedule();
		
		try {
			uiJob.join();
		} catch (InterruptedException e) {
			return ("Failed to update URL : " + URL);
		}
		
		if (!uiJob.getResult().isOK()) {
			return ("Failed to update URL : " + URL);
		}
		
		return "OK";
		
	}
	
	
}
