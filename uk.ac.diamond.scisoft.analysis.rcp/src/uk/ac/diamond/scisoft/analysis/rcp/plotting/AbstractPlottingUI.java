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

import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

public abstract class AbstractPlottingUI implements IPlottingUI {

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

	@Override
	public void areaSelected(AreaSelectEvent event) {
	}

	@Override
	public void plotActionPerformed(PlotActionEvent event) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public void addIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObserver(IObserver observer) {
	}
}
