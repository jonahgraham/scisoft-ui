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

package uk.ac.diamond.sda.polling.server;

import uk.ac.diamond.sda.polling.jobs.AbstractPollJob;

public class PollScheduler implements Runnable {

	private static final int DEFAULT_MAXIMUM_POLL_TIME = 10000;
	public static boolean SCHEDULER_RUNNING = false;
	PollServer pollServer = null;

	public PollScheduler(PollServer pollServer) {
		this.pollServer = pollServer;
	}

	@Override
	/**
	 * check all jobs, if the time till the next poll is negative 
	 * run the job, if its positive then log it
	 * Finally wait until the next step
	 */
	public void run() {

		SCHEDULER_RUNNING = true;

		// TODO should make this easily killable
		while (SCHEDULER_RUNNING) {

			pollServer.pollMonitor.pollLoopStart();
			long timeTillNextJob = processAllJobs();

			try {
				Thread.sleep(timeTillNextJob);
			} catch (InterruptedException e) {
				// Kill the loop and carry on
				continue;
			}

		}
	}

	private long processAllJobs() {

		pollServer.pollMonitor.processingJobs();

		long timeTillNextJob = DEFAULT_MAXIMUM_POLL_TIME;

		for (AbstractPollJob pollJob : this.pollServer.getPollJobs()) {
			pollServer.pollMonitor.schedulingJob(pollJob);

			long timeTillNextEvent = pollJob.timeToSchedule();

			if (timeTillNextEvent > 0 && timeTillNextEvent < timeTillNextJob) {
				timeTillNextJob = timeTillNextEvent;
			}
		}
		pollServer.pollMonitor.processingJobsComplete(timeTillNextJob);
		return timeTillNextJob;
	}

}
