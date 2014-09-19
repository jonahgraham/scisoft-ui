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

public class NullPollMonitor implements IPollMonitor {

	@Override
	public void pollLoopStart() {
		
	}

	@Override
	public void processingJobs() {
		
	}

	@Override
	public void schedulingJob(AbstractPollJob pollJob) {
		
	}

	@Override
	public void processingJobsComplete(long timeTillNextJob) {
		
	}

	@Override
	public void jobAdded(Job job) {
		
	}

}
