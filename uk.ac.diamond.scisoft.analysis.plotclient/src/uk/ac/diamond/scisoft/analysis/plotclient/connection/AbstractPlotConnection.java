/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.plotclient.connection;

import uk.ac.diamond.scisoft.analysis.plotclient.ROIManager;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

public class AbstractPlotConnection implements IPlotConnection {

	protected ROIManager manager;
	
	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void disposeOverlays() {
	}

	@Override
	public void processGUIUpdate(GuiBean guiBean) {
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
	}

	public ROIManager getManager() {
		return manager;
	}

	public void setManager(ROIManager manager) {
		this.manager = manager;
	}

}
