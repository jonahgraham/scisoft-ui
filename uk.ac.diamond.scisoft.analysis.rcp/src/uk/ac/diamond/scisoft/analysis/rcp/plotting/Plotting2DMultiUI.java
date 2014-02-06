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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.IMulti2DTrace;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;

/**
 *
 */
public class Plotting2DMultiUI extends AbstractPlottingUI {

	private IPlottingSystem plottingSystem;

	/**
	 * @param plottingSystem
	 */
	public Plotting2DMultiUI(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, final boolean isUpdate) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData != null) {
					Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
//					final List<AbstractDataset> datasets = Collections.synchronizedList(new LinkedList<AbstractDataset>());
					AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
					AbstractDataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
					
					plottingSystem.getSelectedYAxis().setTitle(yAxisValues.getName());
					plottingSystem.getSelectedXAxis().setTitle(xAxisValues.getName());
					
					IDataset[] datasets = new IDataset[plotData.size()];
					int i = 0;
					while (iter.hasNext()) {
						DataSetWithAxisInformation dataSetAxis = iter.next();
						AbstractDataset data = dataSetAxis.getData();
						datasets[i] = data;
						i++;
					}

					List<IDataset> axes = new ArrayList<IDataset>(2);
					axes.add(xAxisValues);
					axes.add(yAxisValues);
					IMulti2DTrace multi2DTrace = plottingSystem.createMulti2DTrace(yAxisValues.getName());
					multi2DTrace.setName(xAxisValues.getName());
					multi2DTrace.setData(axes, datasets);
					plottingSystem.addTrace(multi2DTrace);
					plottingSystem.setTitle("Plot of " + yAxisValues.getName() + " against "+ xAxisValues.getName());
					plottingSystem.autoscaleAxes();

				}
			}
		});
	}	

	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		
	}

	private List<IObserver> observers = 
			Collections.synchronizedList(new LinkedList<IObserver>());

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
		observers.clear();
	}
}
