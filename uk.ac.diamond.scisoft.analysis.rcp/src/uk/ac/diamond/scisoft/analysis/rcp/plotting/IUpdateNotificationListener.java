/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

/**
 * This listener is a simple interface just to be notified when a Data update
 * has been fully processed
 */
public interface IUpdateNotificationListener {

	public void updateProcessed();
	
}
