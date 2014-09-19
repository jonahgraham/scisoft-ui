/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
