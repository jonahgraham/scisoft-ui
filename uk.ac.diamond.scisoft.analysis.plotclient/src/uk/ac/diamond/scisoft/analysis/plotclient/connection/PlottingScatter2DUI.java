/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
/**
 *
 */
class PlottingScatter2DUI extends AbstractPlotConnection {
	private IPlottingSystem<?> plottingSystem;
	private Logger logger = LoggerFactory.getLogger(PlottingScatter2DUI.class);

	public PlottingScatter2DUI(IPlottingSystem<?> plotter) {
		this.plottingSystem = plotter;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, final boolean isUpdate) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData != null) {
					Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
					final List<Dataset> yDatasets = Collections.synchronizedList(new LinkedList<Dataset>());

					int counter = 0;

					while (iter.hasNext()) {
						DatasetWithAxisInformation dataSetAxis = iter.next();
						Dataset data = dataSetAxis.getData();
						yDatasets.add(data);

						Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS + Integer.toString(counter));
						Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS + Integer.toString(counter));

						plottingSystem.getSelectedYAxis().setTitle(yAxisValues.getName());
						plottingSystem.getSelectedXAxis().setTitle(xAxisValues.getName());
						// if we add points (an update) we do not clear the plot
						if (!isUpdate)
							plottingSystem.clear();
						ILineTrace scatterPlotPoints = plottingSystem.createLineTrace(yAxisValues.getName());
						scatterPlotPoints.setTraceType(TraceType.POINT);
						scatterPlotPoints.setTraceColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
						scatterPlotPoints.setPointStyle(PointStyle.FILLED_CIRCLE);
						scatterPlotPoints.setPointSize(6);
						scatterPlotPoints.setName(xAxisValues.getName());
						scatterPlotPoints.setData(xAxisValues, yAxisValues);
						plottingSystem.addTrace(scatterPlotPoints);
						plottingSystem.setTitle("Plot of " + yAxisValues.getName() + " against "+ xAxisValues.getName());
						plottingSystem.autoscaleAxes();
						if (!isUpdate)
							logger.debug("Scatter plot created");
						else
							logger.debug("Scatter plot updated");
						counter++;
					}
				}
			}
		});
	}
}
