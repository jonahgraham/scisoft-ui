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

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.ROIProfilePlotWindow;

/**
 * PlotView equivalent to implement a ROIProfilePlotWindow
 */
public class ROIProfilePlotView extends AbstractPlotView {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(ROIProfilePlotView.class);

	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.roiProfilePlotView";
	/**
	 * The specific point ID for the plot view that can be opened multiple times
	 */
	public static final String PLOT_VIEW_MULTIPLE_ID = "uk.ac.diamond.scisoft.analysis.rcp.roiProfilePlotViewMultiple";

	private ROIProfilePlotWindow plotWindow;

	/**
	 * Default Constructor of the plot view
	 */

	public ROIProfilePlotView() {
		super();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public ROIProfilePlotView(String id) {
		super(id);
	}

	@Override
	public void createPartControl(Composite parent) {

		if (id != null) {
			// process extension configuration
			logger.info("ID: {}", id);
			final PlotViewConfig config = new PlotViewConfig(id);
			plotViewName = config.getName();
			setPartName(config.getName());
		} else {
			// default to the view name
			plotViewName = getViewSite().getRegisteredName();
			String secondaryId = getViewSite().getSecondaryId();
			if (secondaryId != null) {
				plotViewName = secondaryId;
				setPartName(plotViewName);
			}
		}
		logger.info("View name is {}", plotViewName);

		// plotConsumer = new PlotConsumer(plotServer, plotViewName, this);
		parent.setLayout(new FillLayout());

		final GuiBean bean = getGUIInfo();
		plotWindow = new ROIProfilePlotWindow(parent, (GuiPlotMode) bean.get(GuiParameters.PLOTMODE), this, this, getViewSite()
				.getActionBars(), getSite().getPage(), plotViewName);
		plotWindow.updatePlotMode(bean, false);

		setDataBeanAvailable(plotViewName);
		updateBeans();
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plotWindow.getAdapter(clazz);
		}
		return super.getAdapter(clazz);
	}

	@Override
	public void clearPlot() {
		plotWindow.clearPlot();
	}

	@Override
	public void runUpdate() {

		while (getDataBeanAvailable() != null || getStashedGuiBean() != null) {

			// if there is a stashedGUIBean to update then do that update first
			if (getStashedGuiBean() != null) {
				GuiBean guiBean = getStashedGuiBean();
				setStashedGuiBean(null);
				plotWindow.processGUIUpdate(guiBean);
			}

			// once the guiBean has been sorted out, see if there is any need to update the dataBean
			if (getDataBeanAvailable() != null) {
				String beanLocation = getDataBeanAvailable();
				setDataBeanAvailable(null);
				try {
					final DataBean dataBean;
					dataBean = getPlotServer().getData(beanLocation);

					if (dataBean == null)
						return;

					// update the GUI if needed
					updateGuiBean(dataBean);
					plotWindow.processPlotUpdate(dataBean);
					notifyDataObservers(dataBean);
				} catch (Exception e) {
					logger.error("There has been an issue retrieving the databean from the plotserver", e);
				}
			}
		}
	}

	@Override
	public void dispose() {
		if (plotWindow != null)
			plotWindow.dispose();
		// plotConsumer.stop();
		getExecSvc().shutdown();
		deleteIObservers();
		deleteDataObservers();
		System.gc();
	}

	@Override
	public void updatePlotMode(GuiPlotMode mode) {
		plotWindow.updatePlotMode(mode, true);
	}

	@Override
	public void processPlotUpdate(DataBean dBean) {
		plotWindow.processPlotUpdate(dBean);
		notifyDataObservers(dBean);
	}

	@Override
	public void processGUIUpdate(GuiBean bean) {
		plotWindow.processGUIUpdate(bean);
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plotWindow.getPlottingSystem();
	}

	public ROIProfilePlotWindow getPlotWindow() {
		return this.plotWindow;
	}

	//Not used
	@Override
	public DataSetPlotter getMainPlotter() {
		// TODO Auto-generated method stub
		return null;
	}
}
