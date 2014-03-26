/*-
 * Copyright 2014 Diamond Light Source Ltd.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Class to create a 3D Stack plotting
 * TODO: correctly set axes name
 */
public class Plotting1DStackUI extends AbstractPlottingUI {

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

				final int n = plotData.size();
				if (n == 0)
					return;

				// single stack trace with multiple plots

				// work out if more than one x axis supplied
				String xName = null;
				Set<String> xNames = new HashSet<String>();
				for (int i = 0; i < n; i++) {
					DataSetWithAxisInformation d = plotData.get(i);
					xNames.add(d.getAxisMap().getAxisID()[0]);
				}
				if (xNames.size() == 1)
					xName = xNames.iterator().next();

				GuiBean gb = dbPlot.getGuiParameters();
				String plotOperation = gb == null ? null : (String) gb.get(GuiParameters.PLOTOPERATION);

				// check for number of stack traces
				ILineStackTrace oldTrace = null;
				Collection<ITrace> oldTraces = plottingSystem.getTraces();
				for (ITrace t : oldTraces) {
					if (t instanceof ILineStackTrace) {
						 ILineStackTrace s = (ILineStackTrace) t;
						 if (oldTrace == null) {
							 oldTrace = s;
						 } else if (!GuiParameters.PLOTOP_UPDATE.equals(plotOperation)) {
							 plottingSystem.removeTrace(s);
						 }
					} else {
						logger.warn("Trace is not a line stack trace: {}", t);
					}
				}

				ILineStackTrace trace;
				if (oldTrace != null) {
					trace = oldTrace;
				} else {
					plottingSystem.reset();
					trace = plottingSystem.createLineStackTrace("Plots", n);
				}

				IDataset[] ys = new IDataset[n];
				List<IDataset> axes = new ArrayList<IDataset>();
				Map<String, AbstractDataset> axisData = dbPlot.getAxisData();
				for (int i = 0; i < n; i++) {
					DataSetWithAxisInformation d = plotData.get(i);
					ys[i] = d.getData();
					if (xName == null)
						axes.add(axisData.get(d.getAxisMap().getAxisID()[0]));
				}
				if (xName != null) {
					axes.add(axisData.get(xName));
				}
				axes.add(null);
				axes.add(axisData.get(AxisMapBean.ZAXIS));
				trace.setData(axes, ys);

				if (trace == oldTrace) {
					logger.debug("Plot 1D 3D updated");
				} else {
					plottingSystem.addTrace(oldTrace);
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
