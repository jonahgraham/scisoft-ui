/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.plotclient.IPlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotclient.PlotWindowManager;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.views.ExamplePlotView;

/**
 * Plot Window example<br>
 * This plot window enables to create a custom Plot View with custom controls side by side with an IPlottingSystem
 * linked to the plot server.<br>
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

	public ExamplePlotWindow(Composite parent, IActionBars bars, IWorkbenchPart part, String name) {
		this(parent, null, bars, part, name);
	}

	public ExamplePlotWindow(final Composite parent, IBeanScriptingManager manager,
			                 IActionBars bars, IWorkbenchPart part, String name) {
		super(parent, manager, bars, part, name);
		PlotWindowManager.getPrivateManager().registerPlotWindow(this);
	}

	@Override
	public void createPlotControl(Composite composite) {
		SashForm sashForm = new SashForm(composite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setBackground(new Color(parentComp.getDisplay(), 192, 192, 192));

		Composite controlComp = new Composite(sashForm, SWT.NONE);
		controlComp.setLayout(new FillLayout());

		Label exampleLabel = new Label(controlComp, SWT.WRAP);
		exampleLabel.setText("Example of a composite side by side with an IPlottingSystem linked to a plot server");

		// Creates the PlottingSystem
		super.createPlotControl(sashForm);
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