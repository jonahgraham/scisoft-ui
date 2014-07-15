/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.scisoft.mappingexplorer.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 * @author rsr31645
 * 
 */
public abstract class BaseViewPageComposite extends Composite {

	private List<ViewPageCompositeListener> compositeSelectionListener = new ArrayList<BaseViewPageComposite.ViewPageCompositeListener>();

	public BaseViewPageComposite(Composite parent, int style) {
		super(parent, style);
	}

	public void addCompositeSelectionListener(ViewPageCompositeListener listener) {
		compositeSelectionListener.add(listener);
	}

	public void removeCompositeSelectionListener(
			ViewPageCompositeListener listener) {
		compositeSelectionListener.remove(listener);
	}

	public interface ViewPageCompositeListener {

		void selectionChanged(ISelection selection);

	}

	public abstract ISelection getSelection();

	public abstract void selectionChanged(IWorkbenchPart part,
			ISelection selection);

	public abstract void updatePlot() throws Exception;

	public abstract void initialPlot() throws Exception;

	protected void notifyListeners(ISelection selection) {

		for (ViewPageCompositeListener l : compositeSelectionListener) {
			l.selectionChanged(selection);
		}
	}

	public abstract IMappingViewData getMappingViewData();

	/**
	 * To be overriden by those classes who'd like to interact with the
	 * {@link HistogramView}
	 */
	public HistogramDataUpdate getHistogramDataUpdate() {
		return null;
	}

	/**
	 * Should be over-ridden by subclasses to use the histogram update that is
	 * provided by the {@link HistogramView}
	 * 
	 * @param histogramUpdate
	 */
	public void applyHistogramUpdate(HistogramUpdate histogramUpdate) {
		// do nothing by default
	}

	public void selectAllForHistogram() {
		
	}

	protected void disablePlottingSystemActions(IPlottingSystem plottingSystem) {
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.REMOVE_REGION);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.CONFIG_SETTINGS);
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.CONFIG.getId() + BasePlottingConstants.XY_SHOWLEGEND);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.HISTO);
		plottingSystem.getPlotActionSystem().remove(ToolbarConfigurationConstants.CONFIG.getId());
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.ADD_REGION);
		
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.RESCALE);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_INDEX);
		plottingSystem.getPlotActionSystem().remove(BasePlottingConstants.PLOT_X_AXIS);
	}

}
