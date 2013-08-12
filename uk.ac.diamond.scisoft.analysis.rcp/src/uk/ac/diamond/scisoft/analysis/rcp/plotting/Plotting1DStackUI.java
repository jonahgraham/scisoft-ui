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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Class to create a 3D Stack plotting
 * TODO: correctly set axes name
 */
public class Plotting1DStackUI extends AbstractPlotUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting1DStackUI";
	private static final Logger logger = LoggerFactory.getLogger(Plotting1DStackUI.class);

	private IPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Constructor of a plotting 1D 3D stack
	 * @param plottingSystem plotting system
	 */
	public Plotting1DStackUI(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData == null)
					return;

				GuiBean gb = dbPlot.getGuiParameters();

				String plotOperation = gb == null ? null : (String) gb.get(GuiParameters.PLOTOPERATION);
				Collection<ITrace> oldTraces = plottingSystem.getTraces();
				ILineStackTrace trace = oldTraces.size() != 0 ? (ILineStackTrace)oldTraces.iterator().next() : null;
				IDataset[] stackTraces = trace != null ? trace.getStack() : null;
				int traces = stackTraces != null ? stackTraces.length : 0;
				boolean useOldTraces = false;
				final int plots = plotData.size();
				if (GuiParameters.PLOTOP_NONE.equals(plotOperation) || GuiParameters.PLOTOP_UPDATE.equals(plotOperation)) {

					// check if same lines are being plotted
					if (plots <= traces) {
						useOldTraces = plots == traces;
					}

					if (!useOldTraces) {
						traces = 0;
					}
				}

				if (useOldTraces && stackTraces != null && trace != null) {
					List<IDataset> unused = new ArrayList<IDataset>();
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					IDataset[] yDatasets = new IDataset[plotData.size()];
					for (IDataset data : stackTraces) {
						boolean used = false;
						int i = 0;
						for (DataSetWithAxisInformation d : plotData) {
							AbstractDataset ny = d.getData();
							yDatasets[i] = ny;
							AbstractDataset nx = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]);
							xDatasets.add(nx);
							i++;
						}
						if (!used)
							unused.add(data);
					}
					trace.setData(xDatasets, yDatasets);
					logger.debug("Plot 1D 3D updated");
				} else {

					Map<String, AbstractDataset> axisData = dbPlot.getAxisData();
					ArrayList<IDataset> yl = new ArrayList<IDataset>();
					String id = plotData.get(0).getAxisMap().getAxisID()[0];
					AbstractDataset nx = axisData.get(id);
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					xDatasets.add(nx);
					for (DataSetWithAxisInformation d : plotData) {
						AbstractDataset ny = d.getData();
						yl.add(ny);
					}
					plottingSystem.reset();
					plottingSystem.createPlot1D(nx, yl, null, null);

					logger.debug("Plot 1D 3D created");
				}
			}
		});
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.removeAll(observers);
	}
}
