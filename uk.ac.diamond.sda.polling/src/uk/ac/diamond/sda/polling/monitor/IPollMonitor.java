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

public interface IPollMonitor {

	void pollLoopStart();

	void processingJobs();

	void schedulingJob(AbstractPollJob pollJob);

	void processingJobsComplete(long timeTillNextJob);

	void jobAdded(Job job);

}
