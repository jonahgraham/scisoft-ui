/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.eclipse.ui.IWorkbenchPart;

public interface IPlotWindow {

	/**
	 * Return current page.
	 * @return current page
	 */
	public IWorkbenchPart getPart();

	/**
	 * Return the name of the Window
	 * @return name
	 */
	public String getName();

}
