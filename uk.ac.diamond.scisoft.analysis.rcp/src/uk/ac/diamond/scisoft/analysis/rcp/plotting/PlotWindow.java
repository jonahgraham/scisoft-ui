/*-
 * Copyright 2014 Diamond Light Source Ltd.
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

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

	public PlotWindow(Composite parent, GuiPlotMode plotMode, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, plotMode, null, null, bars, page, name);
	}

	public PlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, IWorkbenchPage page, String name) {
		super(parent, plotMode, manager, notifyListener, bars, page, name);
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
	public void createPlottingSystem(Composite composite) {
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.NONE);
			plottingSystem.createPlotPart(composite, getName(), bars, PlotType.XY, (IViewPart) getGuiManager());
			plottingSystem.repaint();
			plottingSystem.addRegionListener(getRoiManager());
		} catch (Exception e) {
			logger.error("Cannot locate any plotting System!", e);
		}
	}

	@Override
	public GuiPlotMode getPlotMode() {
		return GuiPlotMode.ONED;
	}

}
