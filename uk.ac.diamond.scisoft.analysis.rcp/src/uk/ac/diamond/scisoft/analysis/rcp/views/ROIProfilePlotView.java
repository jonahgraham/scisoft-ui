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
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IUpdateNotificationListener;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.ROIProfilePlotWindow;

/**
 * PlotView equivalent to implement a ROIProfilePlotWindow
 */
public class ROIProfilePlotView extends AbstractPlotView {

	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.roiProfilePlotView";
	/**
	 * The specific point ID for the plot view that can be opened multiple times
	 */
	public static final String PLOT_VIEW_MULTIPLE_ID = "uk.ac.diamond.scisoft.analysis.rcp.roiProfilePlotViewMultiple";

	/**
	 * Default Constructor of the plot view
	 */

	public ROIProfilePlotView() {
		super();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public ROIProfilePlotView(String id) {
		super(id);
	}

	@Override
	public AbstractPlotWindow createPlotWindow(Composite parent, 
												GuiPlotMode plotMode, 
												IGuiInfoManager manager,
												IUpdateNotificationListener notifyListener, 
												IActionBars bars, 
												IWorkbenchPage page, 
												String name) {
		return new ROIProfilePlotWindow(parent, plotMode, manager, notifyListener, bars, page, name);
	}
}