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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.ExamplePlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IUpdateNotificationListener;

/**
 * Plot View Example<br>
 * To create a custom PlotView, a PlotWindow needs to be instantiated. <br>
 * This PlotView class instantiates {@code ExamplePlotWindow}<br>
 * The view needs to be added as an extension point as any other view.
 */
public class ExamplePlotView extends AbstractPlotView {

	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.plotViewExample";
	/**
	 * The specific point ID for the plot view that can be opened multiple times
	 */
	public static final String PLOT_VIEW_MULTIPLE_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotViewMultipleExample";

	/**
	 * Default Constructor of the plot view
	 */

	public ExamplePlotView() {
		super();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public ExamplePlotView(String id) {
		super(id);
	}

	@Override
	public AbstractPlotWindow createPlotWindow(Composite parent, 
												GuiPlotMode plotMode, 
												IGuiInfoManager manager,
												IUpdateNotificationListener notifyListener, 
												IActionBars bars, 
												IWorkbenchPage page, 
												String name) {
		return new ExamplePlotWindow(parent, plotMode, manager, notifyListener, bars, page, name);
	}

}