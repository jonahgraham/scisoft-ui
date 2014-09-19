/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.util;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

public enum PlotMode {

	/**
	 * 1D
	 */
	PM1D(GuiPlotMode.ONED), 
	/**
	 * 3D but the axis in z is the index of Y
	 */
	PMSTACKED(GuiPlotMode.ONED_THREED), 
	/**
	 * 3D, z is a data set not the indices of Y
	 */
	PM3D(GuiPlotMode.ONED_THREED);

	private GuiPlotMode plotMode;

	private PlotMode(GuiPlotMode plotMode) {
		this.plotMode = plotMode;
	}

	public GuiPlotMode getGuiPlotMode() {
		return plotMode;
	}

}
