/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;

/**
 *
 */
public class PlottingScatter3DUI extends AbstractPlotUI {

	private Logger logger = LoggerFactory.getLogger(PlottingScatter3DUI.class);

	private IPlottingSystem plottingSystem;

	/**
	 * Constructor of a plotting 3D scatter plot
	 * @param plottingSystem plotting system
	 */
	public PlottingScatter3DUI(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, final boolean isUpdate) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData == null)
					return;

				final List<IDataset> datasets = Collections.synchronizedList(new LinkedList<IDataset>());

				AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
				AbstractDataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
				AbstractDataset zAxisValues = dbPlot.getAxis(AxisMapBean.ZAXIS);

				if (xAxisValues != null && yAxisValues != null && zAxisValues != null) {
					datasets.add(xAxisValues);
					datasets.add(yAxisValues);
					datasets.add(zAxisValues);
					Map<String, AbstractDataset> axisData = dbPlot.getAxisData();
					ArrayList<IDataset> yl = new ArrayList<IDataset>();
					String[] axesIDs = plotData.get(0).getAxisMap().getAxisID();
					String id = "";
					if (axesIDs == null) {
						id = "X-Axis";
					} else {
						id = plotData.get(0).getAxisMap().getAxisID()[0];
					}
					AbstractDataset nx = axisData.get(id);
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					xDatasets.add(nx);
					for (DataSetWithAxisInformation d : plotData) {
						AbstractDataset ny = d.getData();
						yl.add(ny);
					}
					plottingSystem.createPlot1D(nx, datasets, null, null);
					logger.debug("Plot Scatter 3D created");
				} else {
					logger.error("Cannot plot data with NULL axes");
				}
			}
		});
	}
}
