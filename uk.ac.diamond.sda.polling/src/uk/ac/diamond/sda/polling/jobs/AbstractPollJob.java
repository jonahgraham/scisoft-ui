/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.jobs;

import java.io.IOException;

import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractPollJob extends Job {

	private static final String POLL_TIME = "PollTime";
	private JobParameters jobParameters = null;
	private long lastRun;
	private String status = "Starting";

	private void runJob() {
		try {
			jobParameters.refresh();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastRun = System.currentTimeMillis();
		this.schedule();
	}

	public long timeToSchedule() {
		long pollTime = (long) (Double.parseDouble(jobParameters.get(POLL_TIME)) * 1000.0);
		long time = (lastRun + pollTime) - System.currentTimeMillis();
		if (time < 0) {
			runJob();
			time = pollTime;
		}
		return time;
	}

	public AbstractPollJob(String name) {
		super(name);
		lastRun = System.currentTimeMillis();
	}

	public JobParameters getJobParameters() {
		return jobParameters;
	}

	public void setJobParameters(JobParameters jobParameters) {
		this.jobParameters = jobParameters;
	}

	public String getPollTime() {
		return jobParameters.get(POLL_TIME);
	}

	public void setJobParametersFilename(String fileName) throws IOException {
		jobParameters = new JobParameters(fileName);
	}

	public String getJobParametersFilename() {
		return jobParameters.getParameterFile().getAbsolutePath();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
