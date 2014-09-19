/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
