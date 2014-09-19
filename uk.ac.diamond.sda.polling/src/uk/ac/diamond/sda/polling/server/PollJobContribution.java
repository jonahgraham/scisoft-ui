/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.server;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import uk.ac.diamond.sda.polling.jobs.AbstractPollJob;
import uk.ac.diamond.sda.polling.jobs.JobParameters;

public class PollJobContribution {
	
	private static final String ATT_EXAMPLE_CONFIG_TEXT = "example_config_text";
	private static final String ATT_ID = "id";
	private static final String ATT_NAME = "name";
	private static final String ATT_CLASS = "class";
	
	IConfigurationElement configElement;
	
	public static PollJobContribution getPollJobContribution(IConfigurationElement config) throws IllegalArgumentException{
		
		PollJobContribution pollJobContribution = new PollJobContribution();
		// try to get things out of the config which are required
		try {
			
			config.getAttribute(ATT_NAME);
			config.getAttribute(ATT_ID);
			config.getAttribute(ATT_CLASS);
			@SuppressWarnings("unused")
			AbstractPollJob pollJob = (AbstractPollJob) config.createExecutableExtension(ATT_CLASS);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot create PollJob contribution due to the following error",e);
		}
		
		pollJobContribution.configElement = config;
		
		return pollJobContribution; 
		
	}

	public String getName() {
		return configElement.getAttribute(ATT_NAME);
	}
	
	public String getID() {
		return configElement.getAttribute(ATT_ID);
	}
	
	public String getClassName() {
		return configElement.getAttribute(ATT_CLASS);
	}
	
	public String getExampleConfigText() {
		String value = configElement.getAttribute(ATT_EXAMPLE_CONFIG_TEXT);
		return value;
	}
	
	public AbstractPollJob getJob(String fileName) throws CoreException, IOException {
		AbstractPollJob pollJob = (AbstractPollJob) configElement.createExecutableExtension(ATT_CLASS);
		pollJob.setJobParametersFilename(fileName);
		return pollJob;
	}
	
	public AbstractPollJob getJob(JobParameters jobParameters) throws CoreException {
		AbstractPollJob pollJob = (AbstractPollJob) configElement.createExecutableExtension(ATT_CLASS);
		pollJob.setJobParameters(jobParameters);
		return pollJob;
	}


	
	
}
