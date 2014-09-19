/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
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
