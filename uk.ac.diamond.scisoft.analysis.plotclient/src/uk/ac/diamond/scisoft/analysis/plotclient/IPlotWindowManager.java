/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.plotclient;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.plotserver.IPlotWindowManagerRMI;

/**
 * Public interface for PlotWindowManager. A handle to the manager can be obtained from {@link PlotWindow#getManager()}
 * <p>
 * For use from jython or python of the PlotWindowManager, use the scisoftpy wrapper:
 * <pre>
 * import scisoftpy as dnp
 * import scisoftpy as dnp
 * dnp.plot.window_manager.open_duplicate_view(viewName)
 * dnp.plot.window_manager.open_view(viewName)
 * dnp.plot.window_manager.get_open_views()
 * </pre>
 * <p>
 * For RMI access to PlotWindowManager, use {@link IPlotWindowManagerRMI} obtained from
 * {@link RMIPlotWindowManger#getManager()}
 * <p>
 * @see IPlotWindowManagerRMI
 */
public interface IPlotWindowManager {

	/**
	 * The full path of the PlotView class
	 */
	public static final String PLOTVIEW_PATH = "uk.ac.diamond.scisoft.analysis.rcp.views.PlotView";

	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.plotView";
	/**
	 * The specific point ID for the plot view that can be opened multiple times
	 */
	public static final String PLOT_VIEW_MULTIPLE_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotViewMultiple";

	
	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	/**
	 * Create and open a view with a new unique name and fill the view's GuiBean and DataBean with a copy of viewName's
	 * beans
	 * 
	 * @param page
	 *            to apply {@link IWorkbenchPage#showView(String, String, int)} to, or <code>null</code> for automatic
	 * @param viewName
	 *            to duplicate
	 * @return name of the newly duplicated and opened view
	 */
	public String openDuplicateView(IWorkbenchPage page, String viewName);

	/**
	 * Opens the plot view with the given name. If the view name is registered with Eclipse as a primary view, open
	 * that, otherwise open a new Plot window with the given name.
	 * 
	 * @param page
	 *            to apply {@link IWorkbenchPage#showView(String, String, int)} to, or <code>null</code> for automatic
	 * @param viewName
	 *            to open, or <code>null</code> to open a newly named plot window
	 * @return name of the opened view
	 */
	public String openView(IWorkbenchPage page, String viewName);

	/**
	 * Returns a list of all the plot window views currently open.
	 * 
	 * @return list of views
	 */
	public String[] getOpenViews();

	/**
	 * Clear the current plotting system
	 * 
	 * @param plottingSystem
	 *            to clear
	 * @param viewName
	 *            to duplicate
	 */
	public void clearPlottingSystem(IPlottingSystem plottingSystem, String viewName);

}
