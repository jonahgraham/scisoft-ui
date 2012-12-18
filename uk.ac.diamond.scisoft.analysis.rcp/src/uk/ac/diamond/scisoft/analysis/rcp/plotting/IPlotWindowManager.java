/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.plotserver.IPlotWindowManagerRMI;

/**
 * Public interface for PlotWindowManager. A handle to the manager can be obtained from {@link PlotWindow#getManager()}
 * <p>
 * For use from jython or python of the PlotWindowManager, use the scisoftpy wrapper:
 * <p>
 * Jython:
 * <pre>
 * import scisoftpy as dnp
 * dnp.plot.window_manager.openDuplicateView(viewName)
 * dnp.plot.window_manager.openView(viewName)
 * dnp.plot.window_manager.getOpenViews()
 * </pre>
 * Python:
 * <pre>
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
