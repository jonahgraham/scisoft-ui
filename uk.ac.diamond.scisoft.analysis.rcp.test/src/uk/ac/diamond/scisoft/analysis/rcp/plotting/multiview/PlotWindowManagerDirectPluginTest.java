/*
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
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
	public void clearPlottingSystem(AbstractPlottingSystem plottingSystem, String viewName) {
		plottingSystem.reset();
	}

}
