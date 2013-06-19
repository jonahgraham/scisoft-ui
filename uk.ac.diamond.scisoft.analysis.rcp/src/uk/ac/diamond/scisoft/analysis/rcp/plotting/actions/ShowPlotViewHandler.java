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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameterValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.PlotService;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindowManager;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

public class ShowPlotViewHandler extends AbstractHandler {

	/**
	 * Command ID (as defined in plugin.xml)
	 */
	public static String COMMAND_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.showPlotView";
	/**
	 * The parameter key for the ExecutionEvent that specifies the name of the view to show. Contains the argument to
	 * pass as the view name to {@link IPlotWindowManager#openView(org.eclipse.ui.IWorkbenchPage, String)}
	 * <p>
	 * Legal values for the parameter are defined by {@link ShowPlotViewParameterValues}
	 */
	public static String VIEW_NAME_PARAM = COMMAND_ID + ".viewName";
	/**
	 * The text to display for opening a new view, i.e. when VIEW_NAME_PARAM == null
	 */
	public static final String NEW_PLOT_VIEW = "New Plot View";

	/**
	 * Suffix string on Plot View Name in menu when there is corresponding Data and/or Gui bean in the plot server
	 */
	public static final String IN_PLOT_SERVER_SUFFIX = " *";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewName = event.getParameter(VIEW_NAME_PARAM);
		PlotWindow.getManager().openView(null, viewName);

		return null;
	}

	public static class ShowPlotViewParameterValues implements IParameterValues {
		static private Logger logger = LoggerFactory.getLogger(ShowPlotViewParameterValues.class);

		/**
		 * Returns a list of all legal parameter values for {@link ShowPlotViewHandler}. The key is the display name and
		 * the value is the parameter value to {@link ShowPlotViewHandler#VIEW_NAME_PARAM}.
		 * <p>
		 * The list of legal values is made up of the following sources:
		 * <ul>
		 * <li>All of the Plot Views that have been activated and registered themselves with {@link PlotWindowManager#registerPlotWindow(uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotWindow)}</li>
		 * <li>All views in the view registry whose ID starts with {@link PlotView#ID}</li>
		 * <li>All the view references whose primary ID is {@link PlotView#PLOT_VIEW_MULTIPLE_ID}</li>
		 * <li>All Gui Names from {@link PlotService#getGuiNames()}. These are suffixed with {@link ShowPlotViewHandler#IN_PLOT_SERVER_SUFFIX}
		 * <li>A <code>null</code> value for a option to open a new unique view name.
		 * </ul>
		 * 
		 */
		@Override
		public Map<String, String> getParameterValues() {
			PlotWindowManager manager = PlotWindowManager.getPrivateManager();

			String[] views = manager.getAllPossibleViews(null);
			Set<String> guiNamesWithData;
			try {
				guiNamesWithData = new HashSet<String>(Arrays.asList(PlotServerProvider.getPlotServer().getGuiNames()));
			} catch (Exception e) {
				// non-fatal, just means no IN_PLOT_SERVER_SUFFIX next to view name, still shouldn't happen
				logger.debug("Failed to get list of Gui Names from Plot Server", e);
				guiNamesWithData = Collections.emptySet();
			}
			Map<String, String> values = new HashMap<String, String>();
			for (String view : views) {
				String viewDisplay = view;
				
				DATA_BLOCK: if (guiNamesWithData.contains(view)) {
					try {
						DataBean db = PlotServerProvider.getPlotServer().getData(view);
						if (db==null || db.getData()==null || db.getData().isEmpty()) break DATA_BLOCK;
						viewDisplay = view + IN_PLOT_SERVER_SUFFIX;
					} catch (Exception ne) {
						break DATA_BLOCK;
					}
				}
				values.put(viewDisplay, view);
			}
			// null is a legal argument, means open a new view
			values.put(NEW_PLOT_VIEW, null);
			return values;
		}
	}

}
