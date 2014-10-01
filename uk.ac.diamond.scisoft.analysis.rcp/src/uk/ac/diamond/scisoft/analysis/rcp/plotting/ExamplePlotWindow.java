/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.ExamplePlotView;

/**
 * Plot Window example<br>
 * This plot window enables to create a custom Plot View with custom 
 * controls side by side with an IPlottingSystem linked to the plot server.<br>
 * The IPlottingSystem should be created the following way:<br>
 * {@code plottingSystem = PlottingFactory.createPlottingSystem();}<br>
 * {@code plottingSystem.setColorOption(ColorOption.NONE);}<br>
 * {@code plottingSystem.createPlotPart(parent, getName(), bars, PlotType.XY, (IViewPart) getGuiManager());}<br>
 * {@code plottingSystem.repaint();}<br>
 * {@code plottingSystem.addRegionListener(getRoiManager());}
 * {@code plottingSystem.addTraceListener(getRoiManager().getTraceListener());}<br>
 * (see {@link ExamplePlotView} for more info.)
 */
public class ExamplePlotWindow extends AbstractPlotWindow {

	static private Logger logger = LoggerFactory.getLogger(ExamplePlotWindow.class);

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

	public ExamplePlotWindow(Composite parent, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, null, bars, page, name);
	}

	public ExamplePlotWindow(final Composite parent, IGuiInfoManager manager,
			                 IActionBars bars, IWorkbenchPage page, String name) {
		super(parent, manager, bars, page, name);
		PlotWindowManager.getPrivateManager().registerPlotWindow(this);
	}

	@Override
	public void createPlottingSystem(Composite composite) {
		SashForm sashForm = new SashForm(composite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setBackground(new Color(parentComp.getDisplay(), 192, 192, 192));

		Composite controlComp = new Composite(sashForm, SWT.NONE);
		controlComp.setLayout(new FillLayout());

		Label exampleLabel = new Label(controlComp, SWT.WRAP);
		exampleLabel.setText("Example of a composite side by side with an IPlottingSystem linked to a plot server");

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.NONE);
			plottingSystem.createPlotPart(sashForm, getName(), bars, PlotType.XY, (IViewPart) getGuiManager());
			plottingSystem.repaint();
			plottingSystem.addRegionListener(getRoiManager());
			plottingSystem.addTraceListener(getRoiManager().getTraceListener());
		} catch (Exception e) {
			logger.error("Cannot locate any Abstract plotting System!", e);
		}
	}

	@Override
	public GuiPlotMode getPlotMode() {
		return GuiPlotMode.ONED;
	}

	@Override
	public void createRegion() {
		// TODO Auto-generated method stub
	}
}