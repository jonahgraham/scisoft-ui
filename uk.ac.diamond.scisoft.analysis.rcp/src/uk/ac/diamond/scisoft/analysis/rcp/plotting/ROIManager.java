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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIUtils;

/**
 * Class to deal with interactions of regions and ROIs
 */
public class ROIManager implements IROIListener, IRegionListener {
	private static Logger logger = LoggerFactory.getLogger(ROIManager.class);

	private Map<String, IROI> roiMap; // region name to ROI map
	private IROI roi;
	private IGuiInfoManager server; // usually plot window
	private String plotName;
	private IPlottingSystem plottingSystem;
	private ReentrantLock lock;

	public ROIManager(IGuiInfoManager guiManager, String plotName) {
		server = guiManager;
		roiMap = new LinkedHashMap<String, IROI>();

		this.plotName = plotName;
		plottingSystem = PlottingFactory.getPlottingSystem(plotName);
		lock = new ReentrantLock();
	}

	/**
	 * @return name of ROI (can be null)
	 */
	public IROI getROI() {
		return roi;
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		region.removeROIListener(this);
		String rName = region.getName();
		if (roi != null && rName.equals(roi.getName())) { // delete current ROI
			roi = null;
		}
		roiMap.remove(rName);
		updateGuiBean(roi);
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		IROI eroi = region.getROI();
		if (eroi != null && !eroi.equals(roi)) {
			roi = eroi;
			roiMap.put(roi.getName(), roi);
		}
		updateGuiBean(roi);
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		region.addROIListener(this);
		IROI eroi = region.getROI();
		if (eroi != null) {
			updateGuiBean(eroi);
		}
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		roi = null;
		roiMap.clear();
		updateGuiBean(null);
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		
		//THIS WAS CAUSING SCI-1244 BY NULLIFYING THE ROI
		
		// disable the ROI in the region of the plotting system when a drag event
		// so that the region does not have its position reset
//		if (plottingSystem == null)
//			plottingSystem = PlottingFactory.getPlottingSystem(plotName);
//		if (plottingSystem == null)
//			return;
//		IRegion region = plottingSystem.getRegion(((IRegion) evt.getSource()).getName());
//		if (plottingSystem.getRegions().contains(region)) {
//			System.err.println("Hello: reg " + region.getROI() + "; drag " + evt.getROI());
//		}
//		if (region == null) return;
//		if (region.getROI() != null)
//			region.setROI(null);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		IROI eroi = evt.getROI();
		if (eroi == null)
			return;

		roi = eroi;

		roiMap.put(eroi.getName(), eroi);
		updateGuiBean(roi);
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// do same as if ROI changed
		roiChanged(evt);
	}

	private void updateGuiBean(IROI roib) {
		IROI tmpRoi = roib;
		updateROIMap();

		// if locked then do not update server
		if (lock.isLocked()) {
			logger.trace("Silent");
			return;
		}
		logger.trace("Broadcasting");

		server.removeGUIInfo(GuiParameters.ROIDATA);
		server.putGUIInfo(GuiParameters.ROIDATA, roib);

		if (roib == null) { // get first element if possible
			@SuppressWarnings("unchecked")
			ROIList<? extends IROI> list = (ROIList<? extends IROI>) server.getGUIInfo().get(GuiParameters.ROIDATALIST);

			if (list != null && list.size() > 0) {
				roib = list.get(0);
			}
			if (roib == null) {
				server.removeGUIInfo(GuiParameters.ROIDATALIST);
				return;
			}
		}

		server.removeGUIInfo(GuiParameters.ROIDATALIST);
		Serializable list = createNewROIList(roib);
		if (list != null)
			server.putGUIInfo(GuiParameters.ROIDATALIST, list);

		//if the region has been removed, roib will be null
		//we get the first roi from the roilist and put it in roidata
		if(tmpRoi == null){
			@SuppressWarnings("unchecked")
			ROIList<? extends IROI> roilist = (ROIList<? extends IROI>) server.getGUIInfo().get(GuiParameters.ROIDATALIST);

			if (list != null && roilist.size() > 0) {
				tmpRoi = roilist.get(0);
			}
			if(tmpRoi != null){
				server.putGUIInfo(GuiParameters.ROIDATA, roib);
			} else {
				server.removeGUIInfo(GuiParameters.ROIDATA);
			}
		}
	}

	public ROIList<?> createNewROIList(IROI roib) {
		ROIList<? extends IROI> list = ROIUtils.createNewROIList(roib);
		if (list == null)
			return null;

		Class<? extends IROI> clazz = roib.getClass();
		for (IROI r : roiMap.values()) {
			if (r.getClass().equals(clazz)) {
				list.add(r);
			}
		}
		return list;
	}

	/**
	 * This stops ROI manager from broadcasting ROIs back to the plot server
	 */
	public void acquireLock() {
		lock.lock();
	}

	/**
	 * This allows ROI manager to broadcast ROIs back to the plot server
	 */
	public void releaseLock() {
		lock.unlock();
	}

	/**
	 * Method to update roiMap when region name is changed through the UI
	 * This is done by comparing the list of regions in the plottingSystem
	 * and the regions in roiMap
	 */
	private void updateROIMap() {
		if(plottingSystem == null) plottingSystem = PlottingFactory.getPlottingSystem(plotName);
		if(plottingSystem == null) return;
		Collection<IRegion> regions = plottingSystem.getRegions();
		if (regions == null) return;
		Set<String> regNames = new HashSet<String>();
		for (IRegion iRegion : regions) {
			regNames.add(iRegion.getName());
		}
		for (String n : new LinkedHashSet<String>(roiMap.keySet())) {
			if (!regNames.contains(n))
				roiMap.remove(n);
		}
	}
}
