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

import java.io.File;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
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
public class PlotSaveGraphAction extends AbstractHandler {

	private String filename;
	Logger logger = LoggerFactory.getLogger(PlotSaveGraphAction.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final PlotView pv = (PlotView)HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();

		try{
			String plotName = EclipseUtils.getActivePage().getActivePart().getTitle();
			DataBean dbPlot = SDAPlotter.getDataBean(plotName);
			GuiPlotMode plotMode = dbPlot.getGuiPlotMode();

			// With DatasetPlotter
			if(getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
				return saveDatasetPlotter(pv);
			} 
			// with Plotting System
			else if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
				// plot modes with new plotting system
				if (plotMode.equals(GuiPlotMode.ONED) 
						||(plotMode.equals(GuiPlotMode.TWOD))
						||(plotMode.equals(GuiPlotMode.SCATTER2D))) {
					AbstractPlottingSystem plottingSystem = pv.getPlottingSystem();
					plottingSystem.savePlotting(filename);
				} 
				// plot modes with DatasetPlotter
				else {
					return saveDatasetPlotter(pv);
				}
			}
		}catch (Exception e) {
			logger.error("Error while processing save", e);
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}

	private Boolean saveDatasetPlotter(PlotView pv){
		DataSetPlotter plotter = pv.getMainPlotter();
		if (plotter != null) {
			FileDialog dialog = new FileDialog(pv.getSite().getShell(), SWT.SAVE);
			String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
			if (filename != null) {
				dialog.setFilterPath((new File(filename)).getParent());
			} else {
				String filterPath = "/";
				String platform = SWT.getPlatform();
				if (platform.equals("win32") || platform.equals("wpf")) {
					filterPath = "c:\\";
				}
				dialog.setFilterPath(filterPath);
			}
			dialog.setFilterNames(PlotExportUtil.FILE_TYPES);
			dialog.setFilterExtensions(filterExtensions);
			filename = dialog.open();
			if (filename == null)
				return Boolean.FALSE;
			plotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
		} else
			return Boolean.FALSE;
		return Boolean.TRUE;
	}

	private int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? 
				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)
				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}
}
