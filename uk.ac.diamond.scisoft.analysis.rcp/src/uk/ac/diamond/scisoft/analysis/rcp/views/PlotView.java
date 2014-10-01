/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

/**
 * Plot View is the main Analysis panel that can display any n-D scalar data it is the replacement of the Data Vector
 * panel inside the new RCP framework
 */
public class PlotView extends AbstractPlotView {

	public static final String ID = IPlotWindowManager.ID;

	/**
	 * Default Constructor of the plot view
	 */

	public PlotView() {
		super();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public PlotView(String id) {
		super(id);
	}

	@Override
	public AbstractPlotWindow createPlotWindow(Composite parent, 
												IBeanScriptingManager manager,
												IActionBars bars, 
												IWorkbenchPart part, 
												String name) {
		return new PlotWindow(parent, manager, bars, part, name);
	}
}
