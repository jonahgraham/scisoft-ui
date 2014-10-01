/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;

/**
 *
 */
class PlottingScatter3DUI extends AbstractPlottingUI {

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
				List<DatasetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData == null)
					return;

				final List<IDataset> datasets = Collections.synchronizedList(new LinkedList<IDataset>());

				Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
				Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
				Dataset zAxisValues = dbPlot.getAxis(AxisMapBean.ZAXIS);

				if (xAxisValues != null && yAxisValues != null && zAxisValues != null) {
					datasets.add(xAxisValues);
					datasets.add(yAxisValues);
					datasets.add(zAxisValues);
					Map<String, Dataset> axisData = dbPlot.getAxisData();
					ArrayList<IDataset> yl = new ArrayList<IDataset>();
					String[] axesIDs = plotData.get(0).getAxisMap().getAxisID();
					String id = axesIDs == null ? AxisMapBean.XAXIS :
						plotData.get(0).getAxisMap().getAxisID()[0];
					Dataset nx = axisData.get(id);
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					xDatasets.add(nx);
					for (DatasetWithAxisInformation d : plotData) {
						Dataset ny = d.getData();
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
