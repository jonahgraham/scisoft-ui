/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObservable;

import org.dawnsci.plotting.jreality.tool.AreaSelectEventListener;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

/**
 * Generic interface for Plotting UI attached to different Plotters
 */
public interface IPlottingUI extends IObservable, PlotActionEventListener, AreaSelectEventListener {

	/**
	 * Process a plot update
	 * 
	 * @param dbPlot DataBean containing the new plot
	 * @param isUpdate is this an update of an existing plot?
	 */
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate);

	/**
	 * Unregister and dispose all overlays associated to the current plot,
	 * since PlotUI is currently the master on them
	 */
	public void disposeOverlays();

	/**
	 * Deactivate the UI this can be used to do some additional actions before
	 * the UI gets removed
	 */
	public void deactivate(boolean leaveSidePlotOpen);

	/**
	 * Process a GUI update. Implement this synchronously
	 * @param guiBean
	 */
	public void processGUIUpdate(GuiBean guiBean);

	/**
	 * Called when ui is no longer needed
	 */
	public void dispose();

}
