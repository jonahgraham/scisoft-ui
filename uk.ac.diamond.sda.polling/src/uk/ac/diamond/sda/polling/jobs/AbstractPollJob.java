/*-
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
