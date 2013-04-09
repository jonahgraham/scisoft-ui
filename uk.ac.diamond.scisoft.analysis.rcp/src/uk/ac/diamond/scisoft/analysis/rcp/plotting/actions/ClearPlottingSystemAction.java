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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

public class ClearPlottingSystemAction extends AbstractHandler{

	private static Logger logger = LoggerFactory.getLogger(ClearPlottingSystemAction.class);

	/**
	 * Command ID (as defined in plugin.xml)
	 */
	public static String COMMAND_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.clearPlottingSystem";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			String plotName = page.getActivePart().getTitle();
			IPlottingSystem plottingSystem = PlottingFactory.getPlottingSystem(plotName);
			PlotWindow.getManager().clearPlottingSystem(plottingSystem, plotName);
		} catch (Exception e) {
			logger.error("Cannot clear plot", e);
		}
		return null;
	}

}
