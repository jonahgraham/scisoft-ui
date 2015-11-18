/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotclient.PlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;

/**
 * Actual PlotWindow that can be used inside a View- or EditorPart
 */
public class PlotWindow extends AbstractPlotWindow {

	static private Logger logger = LoggerFactory.getLogger(PlotWindow.class);

	/**
	 * Obtain the IPlotWindowManager for the running Eclipse.
	 * 
	 * @return singleton instance of IPlotWindowManager
	 */
	public static IPlotWindowManager getManager() {
		// get the private manager for use only within the framework and
		// "upcast" it to IPlotWindowManager
		return PlotWindowManager.getPrivateManager();
	}

	public PlotWindow(Composite parent, IActionBars bars, IWorkbenchPart part, String name) {
		this(parent, null, bars, part, name);
	}

	public PlotWindow(final Composite parent, IBeanScriptingManager manager, IActionBars bars, IWorkbenchPart part, String name) {
		super(parent, manager, bars, part, name);
		parentAddControlListener();
		PlotWindowManager.getPrivateManager().registerPlotWindow(this);
	}

	private void parentAddControlListener() {
		// for some reason, this window does not get repainted
		// when a perspective is switched and the view is resized
		parentComp.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
			}
			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
	}

	@Override
	public void createPlotControl(Composite composite) {
		try {
			IPlottingSystem<Composite> system = PlottingFactory.createPlottingSystem();
			system.setColorOption(ColorOption.NONE);
			system.createPlotPart(composite, getName(), bars, PlotType.XY, getPart());
			system.repaint();
			setPlottingSystem(system);
		} catch (Exception e) {
			logger.error("Cannot locate any plotting System!", e);
		}
	}

}
