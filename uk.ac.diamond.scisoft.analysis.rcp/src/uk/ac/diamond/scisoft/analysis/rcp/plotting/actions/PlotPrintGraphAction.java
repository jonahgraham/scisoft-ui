/*-
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

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

/**
 *
 */
public class PlotPrintGraphAction extends AbstractHandler {

	Logger logger = LoggerFactory.getLogger(PlotPrintGraphAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final PlotView pv = (PlotView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();

		try{
			String plotName = EclipseUtils.getActivePage().getActivePart().getTitle();
			DataBean dbPlot = SDAPlotter.getDataBean(plotName);
			GuiPlotMode plotMode = dbPlot.getGuiPlotMode();
		
			// With DatasetPlotter
			if(getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
				DataSetPlotter plotter = pv.getMainPlotter();
				if (plotter != null) {
					plotter.printGraph();
				} else
					return Boolean.FALSE;
			} 
			// with Plotting System
			else {
				
				if (plotMode.equals(GuiPlotMode.ONED) 
						||(plotMode.equals(GuiPlotMode.TWOD))
						||(plotMode.equals(GuiPlotMode.SCATTER2D))) {
					IPlottingSystem plottingSystem = pv.getPlottingSystem();
					plottingSystem.printPlotting();
				} else {
					DataSetPlotter plotter = pv.getMainPlotter();
					if (plotter != null) {
						plotter.printGraph();
					}
				}
			}
		}catch (Exception e) {
			logger.error("Error while processing print", e);
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}

	private int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? 
				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)
				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}
}
