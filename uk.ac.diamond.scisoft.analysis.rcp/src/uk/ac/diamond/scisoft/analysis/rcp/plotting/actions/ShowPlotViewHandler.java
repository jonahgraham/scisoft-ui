/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotclient.PlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

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
		 * <li>All of the Plot Views that have been activated and registered themselves PlotWindowManager</li>
		 * <li>All views in the view registry whose ID starts with {@link IPlotWindowManager#ID}</li>
		 * <li>All the view references whose primary ID is {@link IPlotWindowManager#PLOT_VIEW_MULTIPLE_ID}</li>
		 * <li>All Gui Names from {@link PlotService#getGuiNames()}. These are suffixed with {@link ShowPlotViewHandler#IN_PLOT_SERVER_SUFFIX}
		 * <li>A <code>null</code> value for a option to open a new unique view name.
		 * </ul>
		 * 
		 */
		@Override
		public Map<String, String> getParameterValues() {
			PlotWindowManager manager = PlotWindowManager.getPrivateManager();

			String[] views = manager.getAllPossibleViews(null, false);
			Set<String> guiNamesWithData = Collections.emptySet();
			try {
				String[] names = PlotServerProvider.getPlotServer().getGuiNames();
				if (names != null)
					guiNamesWithData = new HashSet<String>(Arrays.asList(names));
			} catch (Exception e) {
				// non-fatal, just means no IN_PLOT_SERVER_SUFFIX next to view name, still shouldn't happen
				logger.debug("Failed to get list of Gui Names from Plot Server", e);
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
