/*
 * Copyright (c) 2012, 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;

/**
 * Class to create the a 2D/image plotting
 */
class Plotting2DUI extends PlottingGUIUpdate {
	
	private static final Logger logger = LoggerFactory.getLogger(Plotting2DUI.class);

	public Plotting2DUI(IPlottingSystem plotter) {
		super(plotter);
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> yDatasets = Collections
					.synchronizedList(new LinkedList<Dataset>());

			final Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			final Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
			List<IDataset> axes = Collections.synchronizedList(new LinkedList<IDataset>());

			String xAxisName = "";
			if (xAxisValues != null) {
				axes.add(xAxisValues);
				xAxisName = xAxisValues.getName();
			} else {
				axes.add(null);
			}
			String yAxisName = "";
			if (yAxisValues != null) {
				axes.add(yAxisValues);
				yAxisName = yAxisValues.getName();
			} else {
				axes.add(null);
			}
			if (axes.get(0) == null && axes.get(1) == null) {
				axes = null;
			}

			while (iter.hasNext()) {
				DatasetWithAxisInformation dataSetAxis = iter.next();
				Dataset data = dataSetAxis.getData();
				yDatasets.add(data);
			}

			Dataset data = yDatasets.get(0);
			if (data != null) {

				final Collection<ITrace> traces = plottingSystem.getTraces();
				if (traces != null && traces.size() > 0) {
					ITrace trace = traces.iterator().next();
					List<IDataset> currentAxes = null;
					int[] shape = null;
					if (trace instanceof IImageTrace) {
						final IImageTrace image = (IImageTrace) trace;
						shape = image.getData() != null ? image.getData().getShape() : null;
						currentAxes = image.getAxes();
					} else if (trace instanceof ISurfaceTrace) {
						final ISurfaceTrace surface = (ISurfaceTrace) trace;
						shape = surface.getData() != null ? surface.getData().getShape() : null;
						currentAxes = surface.getAxes();
					}
					boolean newAxes = true;
					String lastXAxisName = "", lastYAxisName = "";
					if (currentAxes != null && currentAxes.size() > 0) {
						lastXAxisName = currentAxes.get(0).getName();
						lastYAxisName = currentAxes.get(1).getName();
						newAxes = !currentAxes.equals(axes);
					}

					if (shape != null && Arrays.equals(shape, data.getShape()) &&
							lastXAxisName.equals(xAxisName) && lastYAxisName.equals(yAxisName)) {
						IPaletteTrace image = null;
						image = (IPaletteTrace) plottingSystem.updatePlot2D(data, axes, null);
						setPlotViewPalette(image);
						logger.debug("Plot 2D updated");
					} else {
						IPaletteTrace image = null;
						image = (IPaletteTrace) plottingSystem.createPlot2D(data, axes, null);
						setPlotViewPalette(image);
						logger.debug("Plot 2D created");
					}
					if (newAxes) {
						plottingSystem.repaint();
					}
				} else {
					IPaletteTrace image = null;
					image = (IPaletteTrace) plottingSystem.createPlot2D(data, axes, null);

					setPlotViewPalette(image);
					logger.debug("Plot 2D created");
				}
				// COMMENTED TO FIX SCI-808: no need for a repaint
				// plottingSystem.repaint();
			} else {
				logger.debug("No data to plot");
			}
		}
	}

	/**
	 * Thread safe palette update
	 * @param image
	 */
	private void setPlotViewPalette(final IPaletteTrace image) {
		
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			setPaletteUnsafe(image);
		} else {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					setPaletteUnsafe(image);
				}
			});
		}
	}
	
	private void setPaletteUnsafe(IPaletteTrace image) {
		
		
		// TODO This is probably no longer needed as the preference is being stored in a way
		// likely not to work with the plotting system. 
		
		if (image == null) return;
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");

		// check colour scheme in if image trace is in a live plot
		String paletteName = image.getPaletteName();
		String livePlot = store.getString("imageExplorer.playbackView");
		if (plottingSystem.getPlotName().equals(livePlot)) {
			String savedLivePlotPalette = store.getString("imageExplorer.colourMap");
			if (paletteName != null && !paletteName.equals(savedLivePlotPalette)) {
				image.setPalette(store.getString("imageExplorer.colourMap"));
				store.setValue("imageExplorer.colourMap", savedLivePlotPalette);
			}
		} else {
			if (paletteName != null && !paletteName.equals(store.getString("plotView.plot2DcolourMap"))) {
				String savedPlotViewPalette = store.getString("plotView.plot2DcolourMap");
				try {
					image.setPalette(savedPlotViewPalette);
					store.setValue("plotView.plot2DcolourMap", savedPlotViewPalette);
				} catch (Throwable ne) {
					// Leave palette as is and set PLOT_VIEW_PLOT2D_COLOURMAP back to grey.
					store.setValue("plotView.plot2DcolourMap", "Grey Scale");
				}
			}
		}
	}
}
