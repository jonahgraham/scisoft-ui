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

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIUtils;

/**
 * Class to deal with interactions of regions and ROIs
 */
public class ROIManager implements IROIListener, IRegionListener {
	private static Logger logger = LoggerFactory.getLogger(ROIManager.class);

	private IROI roi;
	private IGuiInfoManager server; // usually plot window
	private String plotName;
	private IPlottingSystem plottingSystem;
	private ReentrantLock lock;

	public ROIManager(IGuiInfoManager guiManager, String plotName) {
		server = guiManager;
		this.plotName = plotName;
		lock = new ReentrantLock();
	}

	/**
	 * Return the Trace Listener to be added to the PlottingSystem
	 * @return ITraceListener
	 */
	public ITraceListener getTraceListener() {
		return new ITraceListener.Stub() {
			@Override
			public void traceUpdated(TraceEvent evt) {
				addCurrentROI(true, true);
			}

			@Override
			public void traceAdded(TraceEvent evt) {
				addCurrentROI(true, true);
			}
		};
	}

	private IPlottingSystem getPlottingSystem() {
		if (plottingSystem == null) {
			plottingSystem = PlottingFactory.getPlottingSystem(plotName);
		}
		if (plottingSystem == null) {
			logger.error("Plotting system is null");
		} else if (plottingSystem.isDisposed()) {
			logger.error("Plotting system is disposed");
		}
		return plottingSystem;
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
		removeROI(region.getROI());
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		clearGUIBean();
		roi = null;
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		region.addROIListener(this);
		IROI eroi = region.getROI();
		if (eroi != null && !eroi.equals(roi)) {
			roi = eroi;
		}
		addCurrentROI(!region.fromServer(), false);
		region.setFromServer(false);
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		// do nothing
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void roiDragged(ROIEvent evt) {
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		IRegion reg = (IRegion) evt.getSource();
		if (reg == null)
			return;
		IROI eroi = evt.getROI();
		if (eroi == null)
			return;

		roi = eroi;

		addCurrentROI(!reg.fromServer(), false);
		reg.setFromServer(false);
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// do same as if ROI changed
		roiChanged(evt);
	}

	private static final long CLIENT_WAIT_PERIOD = 40;

	private void removeROI(IROI or) {
//		roiMap.remove(or.getName());
		if (roi != null) {
			if (!or.getClass().equals(roi.getClass())) {
				// switch current roi (and list), then delete
				updateGuiBean(or.getClass(), or, false);
				try {
					Thread.sleep(CLIENT_WAIT_PERIOD); // allow time for clients to update
				} catch (InterruptedException e) {
				}
				updateROIMap();
				updateGuiBean(or.getClass(), null, false);
				try {
					Thread.sleep(CLIENT_WAIT_PERIOD); // allow time for clients to update
				} catch (InterruptedException e) {
				}
			} else {
				updateROIMap();
			}

			if (or.equals(roi.getName())) { // replace current ROI
				roi = getFromROIMap(roi.getClass());
			}
			roi = updateGuiBean(roi.getClass(), roi, false);
		} else {
			updateROIMap();
			roi = updateGuiBean(or.getClass(), or, false);
		}
	}

	private void addCurrentROI(boolean updateServer, boolean quietUpdate) {
		updateROIMap();
		if (updateServer && roi != null)
			updateGuiBean(roi.getClass(), roi, quietUpdate);
	}

	private IROI updateGuiBean(Class<? extends IROI> clazz, IROI r, boolean quietUpdate) {
		// if locked then do not update server
		if (lock.isLocked()) {
			logger.trace("Silent update: {}", r);
			return r;
		}
		logger.trace("Broadcasting update: {}", r);

		GuiBean bean = server.getGUIInfo();
		bean.remove(GuiParameters.ROICLEARALL);
		bean.remove(GuiParameters.ROIDATALIST);
		bean.put(GuiParameters.ROIDATA, null);
		if (quietUpdate)
			bean.put(GuiParameters.QUIET_UPDATE, "");
		ROIList<?> list = createNewROIList(clazz);
		if (list != null && list.size() > 0) {
			bean.put(GuiParameters.ROIDATALIST, list);
			if (!list.contains(r))
				r = list.get(0);
			bean.put(GuiParameters.ROIDATA, r);
		} else {
			r = null;
		}
		server.sendGUIInfo(bean);
		return r;
	}

	private void clearGUIBean() {
		// if locked then do not update server
		if (lock.isLocked()) {
			logger.trace("Silent clear");
			return;
		}
		logger.trace("Broadcasting clear");

		GuiBean bean = server.getGUIInfo();
		bean.put(GuiParameters.ROIDATA, null);
		bean.remove(GuiParameters.ROIDATALIST);
		bean.put(GuiParameters.ROICLEARALL, true);
		server.sendGUIInfo(bean);
	}

	public ROIList<?> createNewROIList(Class<? extends IROI> clazz) {
		ROIList<? extends IROI> list = ROIUtils.createNewROIList(clazz);
		if (list == null) {
			return null;
		}
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions != null) {
			for (IRegion iRegion : regions) {
				IROI r = iRegion.getROI();
				if (r != null && r.getClass().equals(clazz)) {
					list.add(r);
				}
			}
		}
		return list;
	}

	private IROI getFromROIMap(Class<? extends IROI> clazz) {
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions != null) {
			for (IRegion iRegion : regions) {
				IROI r = iRegion.getROI();
				if (r != null && r.getClass().equals(clazz)) {
					return r;
				}
			}
		}
		return null;
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
		// this no longer does anything!!! but leave in place
		// TODO update bean with a  map
//		if (plottingSystem == null)
//			plottingSystem = PlottingFactory.getPlottingSystem(plotName);
//		if (plottingSystem == null)
//			return;
//		Collection<IRegion> regions = plottingSystem.getRegions();
//		if (regions == null)
//			return;
//		Set<String> regNames = new HashSet<String>();
//		for (IRegion iRegion : regions) {
//			regNames.add(iRegion.getName());
//		}
	}
}
