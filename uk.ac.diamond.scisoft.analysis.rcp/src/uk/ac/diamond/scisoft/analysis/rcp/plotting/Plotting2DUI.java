/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dawb.common.ui.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

/**
 * Class to create the a 2D/image plotting
 */
class Plotting2DUI extends AbstractPlottingUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting2DUI";

	private IPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private ROIManager manager;
	private IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);

	private static final Logger logger = LoggerFactory.getLogger(Plotting2DUI.class);

	/**
	 * Constructor for the ROI manager
	 * @param roiManager
	 * @param plotter
	 */
	public Plotting2DUI(ROIManager roiManager, final IPlottingSystem plotter) {
		manager = roiManager;
		plottingSystem = plotter;
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
		
		if (image == null) return;
		IPreferenceStore store = AnalysisRCPActivator.getDefault().getPreferenceStore();

		// check colour scheme in if image trace is in a live plot
		String paletteName = image.getPaletteName();
		String livePlot = store.getString(PreferenceConstants.IMAGEEXPLORER_PLAYBACKVIEW);
		if (plottingSystem.getPlotName().equals(livePlot)) {
			String savedLivePlotPalette = store.getString(PreferenceConstants.IMAGEEXPLORER_COLOURMAP);
			if (paletteName != null && !paletteName.equals(savedLivePlotPalette)) {
				image.setPaletteData(pservice.getDirectPaletteData(savedLivePlotPalette));
				image.setPaletteName(store.getString(PreferenceConstants.IMAGEEXPLORER_COLOURMAP));
				store.setValue(PreferenceConstants.IMAGEEXPLORER_COLOURMAP, savedLivePlotPalette);
			}
		} else {
			if (paletteName != null && !paletteName.equals(store.getString(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP))) {
				String savedPlotViewPalette = store.getString(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP);
				try {
					image.setPaletteData(pservice.getDirectPaletteData(savedPlotViewPalette));
					image.setPaletteName(savedPlotViewPalette);
					store.setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP, savedPlotViewPalette);
				} catch (Throwable ne) {
					// Leave palette as is and set PLOT_VIEW_PLOT2D_COLOURMAP back to grey.
					store.setValue(PreferenceConstants.PLOT_VIEW_PLOT2D_COLOURMAP, "Grey Scale");
				}
			}
		}
	}

	@Override
	public void processGUIUpdate(final GuiBean guiBean) {
		
		logger.debug("There is a guiBean update: {}", guiBean);

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
//				TODO investigate why this is called when there is a really small time between 2 plot updates
//				System.err.println("Should not be called!!!");
				final Boolean clearAll = (Boolean) guiBean.remove(GuiParameters.ROICLEARALL);
				if (clearAll != null && clearAll) {
					plottingSystem.clearRegions();
				}

				final IROI roi = (IROI) guiBean.get(GuiParameters.ROIDATA);
				IROI croi = manager.getROI();
				ROIList<? extends IROI> list = (ROIList<?>) guiBean.get(GuiParameters.ROIDATALIST);

				if (roi != null)
					logger.trace("R: {}", roi.getName());
				if (list != null) {
					for (IROI r : list)
						logger.trace("L: {}", r.getName());
				}
				// Same as in SidePlotProfile with onSwitch = false, i.e.:
				// logic is for each GUI parameter
				//     if null and parameter exists
				//         delete parameter
				//         signal updating of parameter
				//     else if same class
				//         replace parameter
				//         signal updating of parameter

				String rName = null;
				if (roi == null) { // this indicates to remove the current ROI
					if (croi != null) {
						final IRegion r = plottingSystem.getRegion(croi.getName());
						if (r != null) {
							plottingSystem.removeRegion(r);
						}
						croi = null;
					}
				} else {
					rName = roi.getName(); // overwrite name if necessary
					if (rName != null && rName.trim().length() == 0) {
						rName = null;
					}
					boolean found = false; // found existing?
					if (croi != null) {
						if (roi.getClass().equals(croi.getClass())) { // replace current ROI
							String cn = croi.getName();
							final IRegion reg = plottingSystem.getRegion(cn);
							if (reg != null) {
								if (rName != null) {
									reg.setName(rName);
									croi.setName(rName);
								} else {
									roi.setName(cn);
									rName = cn;
								}
								
								reg.setFromServer(true);
								if (!reg.getCoordinateSystem().isDisposed())
									reg.setROI(roi);
								found = true;
							}
						} else {
							if (rName != null) {
								final IRegion reg = plottingSystem.getRegion(rName);
								if (reg != null) {
									reg.setFromServer(true);
									reg.setROI(roi);
									found = true;
								}
							}
						}
					}
					if (!found) { // create new region
						if (list == null) {
							list = ROIUtils.createNewROIList(roi);
							list.add(roi);
						} else {
							if (list.size() > 0) {
								if (list.get(0).getClass().equals(roi.getClass())) {
									if (!list.contains(roi)) {
										list.add(roi);
									}
								}
							} else {
								list.add(roi);
							}
						}
						createRegion(roi, guiBean);
						croi = roi;
					}
				}

				final Set<String> names = new HashSet<String>(); // names of ROIs
				if (rName != null) { // add existing ROI
					names.add(rName);
				}
				if (list != null) {
					for (IROI r : list) {
						String n = r.getName();
						if (n != null && n.trim().length() > 0) {
							names.add(n);
						}
					}
				}

				final Collection<IRegion> regions = plottingSystem.getRegions();
				Set<String> regNames = new HashSet<String>(); // regions not removed
				for (IRegion reg : regions) { // clear all regions not listed
					String regName = reg.getName();
					if (!names.contains(regName)) {
						plottingSystem.removeRegion(reg);
					} else {
						regNames.add(regName);
					}
				}
				if (list != null) {
					for (IROI r : list) {
						if (r == croi)
							continue; // no need to update region

						String n = r.getName();
						if (regNames.contains(n)) { // update ROI
							IRegion region = plottingSystem.getRegion(n);
							if (region != null)
								region.setROI(r);
						} else { // or add new region that has not been listed
							IRegion reg = plottingSystem.getRegion(n);
							if (reg == null) {
								createRegion(r, guiBean);
							} else {
								reg.setFromServer(true);
								reg.setROI(r);
							}
						}
					}
				}
			}
		});
	}

	private IRegion createRegion(IROI roib, GuiBean guiBean) {
		if (roib == null)
			return null;
		try {
			final IRegionService rservice = (IRegionService)Activator.getService(IRegionService.class);
			RegionType type = rservice.getRegion(roib.getClass());
			String name = roib.getName();
			if (name == null || name.trim().length() == 0) {
				name = RegionUtils.getUniqueName(type.getName(), plottingSystem);
				logger.warn("Blank or unset name in ROI now set to {}", name);
				roib.setName(name);
				guiBean.put(GuiParameters.QUIET_UPDATE, ""); // mark bean to be sent back 
			}
			IRegion region = plottingSystem.createRegion(name, type);
			region.setFromServer(true);
			region.setROI(roib);
			plottingSystem.addRegion(region);
			return region;
		} catch (Exception e) {
			logger.error("Problem creating new region from ROI", e);
		}
		return null;
	}
}
