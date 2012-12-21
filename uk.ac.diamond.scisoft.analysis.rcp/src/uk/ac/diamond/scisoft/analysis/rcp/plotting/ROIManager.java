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
import java.util.LinkedHashMap;
import java.util.Map;

import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIList;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROIList;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROIList;

/**
 * Class to deal with interactions of regions and ROIs
 */
public class ROIManager implements IROIListener, IRegionListener {
	private Map<String, ROIBase> roiMap; // region name to ROI map
	private String name;
	private ROIBase roi;
	private IGuiInfoManager server; // usually plot window

	public ROIManager(IGuiInfoManager guiManager) {
		server = guiManager;
		roiMap = new LinkedHashMap<String, ROIBase>();
	}

	/**
	 * @return name of current region (can be null)
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return name of ROI (can be null)
	 */
	public ROIBase getROI() {
		return roi;
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		region.removeROIListener(this);
		String rName = region.getName();
		if (rName.equals(name)) { // delete current ROI
			roi = null;
			name = null;
		}
		roiMap.remove(rName);
		updateGuiBean(roi);
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		ROIBase eroi = region.getROI();
		if (eroi != null && !eroi.equals(roi)) {
			roi = eroi;
			name = region.getName();
			roiMap.put(name, roi);
		}
		updateGuiBean(roi);
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		IRegion region = evt.getRegion();
		if (region == null)
			return;

		region.addROIListener(this);
		ROIBase eroi = region.getROI();
		if (eroi != null) {
			updateGuiBean(eroi);
		}
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		roi = null;
		name = null;
		roiMap.clear();
		updateGuiBean(null);
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		// do nothing
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		ROIBase eroi = evt.getROI();
		if (eroi == null)
			return;

		String id = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getId();
		if (id.startsWith(PlotView.ID)) {
			name = evt.getSource().toString();
			roi = eroi;

			roiMap.put(name, eroi);
			updateGuiBean(roi);
		}
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// do same as if ROI changed
		roiChanged(evt);
	}

	private void updateGuiBean(ROIBase roib) {
		server.removeGUIInfo(GuiParameters.ROIDATA);
		server.putGUIInfo(GuiParameters.ROIDATA, roib);

		if (roib == null) { // get first element if possible
			@SuppressWarnings("unchecked")
			ROIList<? extends ROIBase> list = (ROIList<? extends ROIBase>) server.getGUIInfo().get(GuiParameters.ROIDATALIST);

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
	}

	public ROIList<? extends ROIBase> createNewROIList(ROIBase roib) {
		if (roib instanceof LinearROI)
			return createNewLROIList();
		else if (roib instanceof RectangularROI)
			return createNewRROIList();
		else if (roib instanceof SectorROI)
			return createNewSROIList();
		return null;
	}

	public LinearROIList createNewLROIList() {
		LinearROIList list = new LinearROIList();
		for (ROIBase r : roiMap.values()) {
			if (r instanceof LinearROI) {
				list.add((LinearROI) r);
			}
		}
		if (list.size() == 0)
			return null;
		return list;
	}

	public RectangularROIList createNewRROIList() {
		RectangularROIList list = new RectangularROIList();
		for (ROIBase r : roiMap.values()) {
			if (r instanceof RectangularROI) {
				list.add((RectangularROI) r);
			}
		}
		if (list.size() == 0)
			return null;
		return list;
	}

	public SectorROIList createNewSROIList() {
		SectorROIList list = new SectorROIList();
		for (ROIBase r : roiMap.values()) {
			if (r instanceof SectorROI) {
				list.add((SectorROI) r);
			}
		}
		if (list.size() == 0)
			return null;
		return list;
	}
}
