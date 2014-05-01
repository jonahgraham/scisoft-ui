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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.trace.ColorOption;
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
 * {@code plottingSystem.addRegionListener(getRoiManager());}<br>
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

	public ExamplePlotWindow(Composite parent, GuiPlotMode plotMode, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, plotMode, null, null, bars, page, name);
	}

	public ExamplePlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, IWorkbenchPage page, String name) {
		super(parent, plotMode, manager, notifyListener, bars, page, name);
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