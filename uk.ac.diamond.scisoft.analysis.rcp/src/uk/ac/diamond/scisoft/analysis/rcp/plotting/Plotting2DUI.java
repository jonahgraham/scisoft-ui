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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawb.common.ui.plot.region.RegionService;
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
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIUtils;

/**
 * Class to create the a 2D/image plotting
 */
public class Plotting2DUI extends AbstractPlotUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting2DUI";

	private AbstractPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private ROIManager manager;

	private static final Logger logger = LoggerFactory.getLogger(Plotting2DUI.class);

	/**
	 * Constructor for the ROI manager
	 * @param roiManager
	 * @param plotter
	 */
	public Plotting2DUI(ROIManager roiManager, final AbstractPlottingSystem plotter) {
		manager = roiManager;
		plottingSystem = plotter;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				Collection<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData != null) {
					Iterator<DataSetWithAxisInformation> iter = plotData.iterator();
					final List<AbstractDataset> yDatasets = Collections
							.synchronizedList(new LinkedList<AbstractDataset>());

					final AbstractDataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
					final AbstractDataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
					final List<IDataset> axes = Collections.synchronizedList(new LinkedList<IDataset>());
					
					String xAxisName = "", yAxisName = "";
					if(xAxisValues!=null){
						axes.add(0, xAxisValues);
						xAxisName = xAxisValues.getName();
					}
					if(yAxisValues!=null){
						axes.add(1, yAxisValues);
						yAxisName = yAxisValues.getName();
					}

					while (iter.hasNext()) {
						DataSetWithAxisInformation dataSetAxis = iter.next();
						AbstractDataset data = dataSetAxis.getData();
						yDatasets.add(data);
					}

					AbstractDataset data = yDatasets.get(0);
					if (data != null) {
					
						final Collection<ITrace> traces = plottingSystem.getTraces();
						final List<ITrace> traceList =new ArrayList<ITrace>(traces);
						if (traces != null && traces.size() > 0 
								&& traceList.size()>0
								&& traceList.get(0) instanceof IImageTrace) {
							final IImageTrace image = (IImageTrace) traces.iterator().next();
							final int[] shape = image.getData() != null ? image.getData().getShape() : null;
							
							List<IDataset> currentAxes = image.getAxes(); 
							String lastXAxisName = "", lastYAxisName = "";
							if(currentAxes!=null && currentAxes.size()>0)
								lastXAxisName = currentAxes.get(0).getName();
							if(currentAxes!=null && currentAxes.size()>1)
								lastYAxisName = currentAxes.get(1).getName();
							
							if (shape != null && Arrays.equals(shape, data.getShape())
									&& lastXAxisName.equals(xAxisName)
									&& lastYAxisName.equals(yAxisName)) {
								if(axes.size()>0)
									plottingSystem.updatePlot2D(data, axes, null);
								else
									plottingSystem.updatePlot2D(data, null, null);
								logger.debug("Plot 2D updated");
							} else {
								plottingSystem.createPlot2D(data, axes, null);
								logger.debug("Plot 2D created");
							}
						}else{
							if(axes.size()>0)
								plottingSystem.createPlot2D(data, axes, null);
							else
								plottingSystem.createPlot2D(data, null, null);
							logger.debug("Plot 2D created");
						}
						plottingSystem.repaint();

					} else
						logger.debug("No data to plot");
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
		observers.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void processGUIUpdate(GuiBean guiBean) {

		final ROIBase roi = (ROIBase)guiBean.get(GuiParameters.ROIDATA);

		logger.debug("There is a guiBean update:"+ guiBean.toString());

		final String cname = manager.getName();
		final ROIBase croi = manager.getROI();
		final Collection<IRegion> regions = plottingSystem.getRegions();
		@SuppressWarnings("rawtypes")
		ROIList list = (ROIList) guiBean.get(GuiParameters.ROIDATALIST);


		// Same as in SidePlotProfile with onSwitch = false
		// logic is for each GUI parameter
		//     if null and parameter exists
		//         if not onSwitch
		//             delete parameter
		//         signal updating of parameter
		//     else if same class
		//         replace parameter
		//         signal updating of parameter
		//     else if onSwitch
		//         signal updating of parameter

		if (roi == null) {
			if (cname != null && croi != null) {
				IRegion found = null;
				for (IRegion r : regions) {
					if (cname.equals(r.getName())) {
						found = r;
						break;
					}
				}
				if (found != null) { // delete old region
					regions.remove(found);
					final IRegion r = found;
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							plottingSystem.removeRegion(r);
						}
					});
					
				}
			}
		} else {
			boolean found = false; // found existing?
			if (cname != null && croi != null) {
				if (roi.getClass().equals(croi.getClass())) { // replace current ROI
					for (final IRegion r : regions) {
						if (cname.equals(r.getName())) {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									r.setROI(roi);
								}
							});
							found = true;
							break;
						}
					}
				}
			}
			if (!found) { // create new region
				if (list == null) {
					list = ROIUtils.createNewROIList(roi);
				}
				list.add(roi);
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						regions.add(createRegion(roi));
					}
				});
			}
		}

		final ROIBase roib = roi != null ? roi : (croi != null ? croi : null);
		if (roib != null) {
			if (list == null || list.size() == 0) {
				// prune regions of given type
				final List<IRegion> rList = createRegionsList(regions, roib.getClass());
				if (rList.size() > 0) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							for (IRegion r : rList) {
								plottingSystem.removeRegion(r);
							}
						}
					});
				}
			} else {
				ROIBase froi = (ROIBase) list.get(0);
				if (roib.getClass().equals(froi.getClass())) {
					final List<IRegion> rList = createRegionsList(regions, roib.getClass());
					final int nr = rList.size();
					if (nr > 0) {
						final ROIList<? extends ROIBase> flist = list;
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								int nl = flist.size();
								int n = nr;
								if (nl < nr) {
									for (int i = nr - 1; i >= nl; i--) {
										plottingSystem.removeRegion(rList.remove(i));
									}
									n = nl;
								}
								for (int i = 0; i < n; i++) {
									rList.get(i).setROI(flist.get(i));
								}
								if (nl > nr) {
									// create regions
									for (int i = nr; i < nl; i++) {
										createRegion(flist.get(i));
									}
								}
							}
						});
					}
				}
			}
		}
	}

	private IRegion createRegion(ROIBase roib) {
		try {
			RegionType type = RegionService.getRegion(roib.getClass());
			IRegion region = plottingSystem.createRegion(RegionUtils.getUniqueName(type.getName(), plottingSystem), type);
			region.setROI(roib);
			plottingSystem.addRegion(region);
			return region;
		} catch (Exception e) {
			logger.error("Problem creating new region from ROI", e);
		}
		return null;
	}

	private static List<IRegion> createRegionsList(Collection<IRegion> regions, Class<? extends ROIBase> roiClass) {
		List<IRegion> list = new ArrayList<IRegion>();
		for (IRegion r : regions) {
			IROI rr = r.getROI();
			if (rr.getClass().equals(roiClass)) {
				list.add(r);
			}
		}
		return list;
	}
}
