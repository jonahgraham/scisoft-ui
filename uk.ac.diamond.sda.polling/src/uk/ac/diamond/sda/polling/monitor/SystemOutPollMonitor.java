/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.polling.monitor;

import org.eclipse.core.runtime.jobs.Job;

import uk.ac.diamond.sda.polling.jobs.AbstractPollJob;

public class SystemOutPollMonitor implements IPollMonitor {

	long loopCount = 0;
	
	@Override
	public void pollLoopStart() {
		System.out.println(String.format("Starting Poling Loop %d",loopCount));
		loopCount += 1;
	}

	@Override
	public void processingJobs() {
		System.out.println("Processing Jobs");

	}

	@Override
	public void schedulingJob(AbstractPollJob pollJob) {
		System.out.println("Scheduling Job : "+ pollJob.toString());

	}

	@Override
	public void processingJobsComplete(long timeTillNextJob) {
		System.out.println(String.format("Processing Jobs Complete Time to Wait %d",timeTillNextJob));

	}

	@Override
	public void jobAdded(Job job) {
		System.out.println("Scheduling Job : "+ job.toString());

	}

}
