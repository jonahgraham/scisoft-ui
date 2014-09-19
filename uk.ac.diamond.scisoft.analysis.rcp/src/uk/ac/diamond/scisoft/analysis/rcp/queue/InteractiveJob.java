/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.queue;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An interface for an interactive job
 */
public interface InteractiveJob {

	/**
	 * Run job with given monitor
	 * @param monitor
	 */
	public void run(IProgressMonitor monitor);

	/**
	 * @return true if job is null
	 */
	public boolean isNull();
}
