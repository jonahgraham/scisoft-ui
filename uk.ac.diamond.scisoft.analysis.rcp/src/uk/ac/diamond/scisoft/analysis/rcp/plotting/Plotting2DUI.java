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

import org.dawb.common.ui.plot.region.RegionService;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
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
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIUtils;

/**
 * Class to create the a 2D/image plotting
 */
public class Plotting2DUI extends AbstractPlotUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting2DUI";

	private IPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private ROIManager manager;

	private static final Logger logger = LoggerFactory.getLogger(Plotting2DUI.class);

	/**
	 * Constructor for the ROI manager
	 * @param roiManager
	 * @param plotter
	 */
	public Plotting2DUI(ROIManager roiManager, final IPlottingSystem plotter) {
		manager = roiManager;
		plottingSystem = plotter;
		if (plottingSystem.getPlotType().equals(PlotType.SURFACE)){
			try {
				((IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class))
					.setToolVisible("org.dawb.workbench.plotting.tools.windowTool", 
									ToolPageRole.ROLE_3D, 
									"org.dawb.workbench.plotting.views.toolPageView.3D");
			} catch (Exception e1) {
				logger.error("Cannot open window tool!", e1);
			}
		}
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
								&& traceList.size()>0) {
							List<IDataset> currentAxes = null;
							int[] shape = null; 
							if(traceList.get(0) instanceof IImageTrace){
								final IImageTrace image = (IImageTrace) traces.iterator().next();
								shape = image.getData() != null ? image.getData().getShape() : null;
								currentAxes = image.getAxes();
							} else if(traceList.get(0) instanceof ISurfaceTrace){
								final ISurfaceTrace surface = (ISurfaceTrace) traces.iterator().next();
								shape = surface.getData() != null ? surface.getData().getShape() : null;
								currentAxes = surface.getAxes();
							}
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
	
	@Override
	public void processGUIUpdate(GuiBean guiBean) {

		final IROI roi = (IROI)guiBean.get(GuiParameters.ROIDATA);

		logger.debug("There is a guiBean update:"+ guiBean.toString());

		final IROI croi = manager.getROI();
		ROIList<? extends IROI> list = (ROIList<?>) guiBean.get(GuiParameters.ROIDATALIST);


		// Same as in SidePlotProfile with onSwitch = false, i.e.:
		// logic is for each GUI parameter
		//     if null and parameter exists
		//         delete parameter
		//         signal updating of parameter
		//     else if same class
		//         replace parameter
		//         signal updating of parameter

		if (roi == null) {
			if (croi != null) {
				final IRegion r = plottingSystem.getRegion(croi.getName());
				if (r != null) {
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
			if (croi != null) {
				if (roi.getClass().equals(croi.getClass())) { // replace current ROI
					final IRegion r = plottingSystem.getRegion(croi.getName());
					if (r != null) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								r.setROI(roi);
							}
						});
						found = true;
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
						createRegion(roi);
					}
				});
			}
		}

		final IROI roib = roi != null ? roi : croi;
		Class<? extends IROI> clazz = roib == null ? null : roib.getClass();
		if (clazz == null && list != null && list.size() > 0) {
			clazz = list.get(0).getClass();
		}
		final List<String> names = new ArrayList<String>(); // names of ROIs of same class
		if (list != null) {
			for (IROI r : list) {
				if (r.getClass().equals(clazz))
					names.add(r.getName());
			}
		}

		final Collection<IRegion> regions = plottingSystem.getRegions();
		final List<IRegion> rList = createRegionsList(regions, clazz); // regions of given type
		final ROIList<? extends IROI> flist = list;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<String> rNames = new ArrayList<String>(); // regions not removed
				for (IRegion r : rList) { // clear all regions of given type not listed
					String rName = r.getName();
					if (!names.contains(rName))
						plottingSystem.removeRegion(r);
					else
						rNames.add(rName);
				}

				if (flist != null) {
					for (IROI r : flist) {
						String n = r.getName();
						if (rNames.contains(n)) { // update ROI
							plottingSystem.getRegion(n).setROI(r);
						} else { // or add new region that has not been listed
							createRegion(r);
						}
					}
				}
			}
		});
	}

	private IRegion createRegion(IROI roib) {
		try {
			RegionType type = RegionService.getRegion(roib.getClass());
			String name = roib.getName();
			if (name == null || name.trim().length() == 0) {
				name = RegionUtils.getUniqueName(type.getName(), plottingSystem);
				roib.setName(name);
			}
			IRegion region = plottingSystem.createRegion(name, type);
			region.setROI(roib);
			plottingSystem.addRegion(region);
			return region;
		} catch (Exception e) {
			logger.error("Problem creating new region from ROI", e);
		}
		return null;
	}

	private static List<IRegion> createRegionsList(Collection<IRegion> regions, Class<? extends IROI> clazz) {
		List<IRegion> list = new ArrayList<IRegion>();
		if (clazz == null)
			return list;
		for (IRegion r : regions) {
			if (r.getROI().getClass().equals(clazz)) {
				list.add(r);
			}
		}
		return list;
	}
}
