/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

/**
 * Concrete class that tests direct connection (ie in same JVM, no RMI, RPC)
 */
public class PlotWindowManagerDirectPluginTest extends PlotWindowManagerPluginTestAbstract {

	@Override
	public String openDuplicateView(IWorkbenchPage page, String viewName) {
		return PlotWindow.getManager().openDuplicateView(page, viewName);
	}

	@Override
	public String openView(IWorkbenchPage page, String viewName) {
		return PlotWindow.getManager().openView(page, viewName);
	}

	@Override
	public String[] getOpenViews() {
		return PlotWindow.getManager().getOpenViews();
	}

	@Override
	public void clearPlottingSystem(IPlottingSystem<?> plottingSystem, String viewName) {
		plottingSystem.reset();
	}

}
