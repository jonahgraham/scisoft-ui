/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotclient.Activator;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * AbstractPlotConnection class with gui update for regions
 *
 */
public class PlottingGUIUpdate extends AbstractPlotConnection {

	private static final Logger logger = LoggerFactory.getLogger(PlottingGUIUpdate.class);

	protected IPlottingSystem plottingSystem;

	public PlottingGUIUpdate(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
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
									plottingSystem.renameRegion(reg, rName);
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
						String n = r.getName();
						if (r == croi || n.equals(rName)) {
							continue; // no need to update current region
						}

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
