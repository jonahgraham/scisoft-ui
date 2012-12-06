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

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IUpdateNotificationListener;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

/**
 * Plot View is the main Analysis panel that can display any n-D scalar data it is the replacement of the Data Vector
 * panel inside the new RCP framework
 */
public class PlotView extends ViewPart implements IObserver, IObservable, IGuiInfoManager, IUpdateNotificationListener,
		ISidePlotPart {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(PlotView.class);

	/**
	 * The extension point ID for 3rd party contribution
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.plotView";
	/**
	 * The specific point ID for the plot view that can be opened multiple times
	 */
	public static final String PLOT_VIEW_MULTIPLE_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotViewMultiple";

	/**
	 * the ID of this view
	 */
	private String id;

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	private PlotWindow plotWindow;
	private PlotServer plotServer;
	private ExecutorService execSvc = null;
	protected String plotViewName = "Plot View";
	private IPlotUI plotUI = null;
	private UUID plotID = null;
	private GuiBean guiBean = null;

	private Set<IObserver> dataObservers = Collections.synchronizedSet(new LinkedHashSet<IObserver>());

	/**
	 * @return plot UI
	 */
	public IPlotUI getPlotUI() {
		return plotUI;
	}

	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Default Constructor of the plot view
	 */

	public PlotView() {
		super();
		init();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public PlotView(String id) {
		super();
		this.id = id;
		init();
	}

	private void init() {
		plotID = UUID.randomUUID();
		logger.info("Plot view uuid: {}", plotID);
		plotServer = PlotServerProvider.getPlotServer();
		plotServer.addIObserver(this);
		execSvc = Executors.newFixedThreadPool(2);
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
		plotWindow = new PlotWindow(parent, (GuiPlotMode) bean.get(GuiParameters.PLOTMODE), this, this, getViewSite()
				.getActionBars(), getSite().getPage(), plotViewName);
		plotWindow.updatePlotMode(bean, false);

		// plotConsumer.addIObserver(this);
		dataBeanAvailable = plotViewName;
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
	public void setFocus() {
	}

	public void clearPlot() {
		plotWindow.clearPlot();
	}

	private GuiBean stashedGuiBean;
	private String dataBeanAvailable;

	private void runUpdate() {

		while (dataBeanAvailable != null || stashedGuiBean != null) {

			// if there is a stashedGUIBean to update then do that update first
			if (stashedGuiBean != null) {
				GuiBean guiBean = stashedGuiBean;
				stashedGuiBean = null;
				if(plotWindow != null)
					plotWindow.processGUIUpdate(guiBean);
			}

			// once the guiBean has been sorted out, see if there is any need to update the dataBean
			if (dataBeanAvailable != null) {
				String beanLocation = dataBeanAvailable;
				dataBeanAvailable = null;
				try {
					final DataBean dataBean;
					dataBean = plotServer.getData(beanLocation);

					if (dataBean == null)
						return;

					// update the GUI if needed
					updateGuiBean(dataBean);
					if(plotWindow != null)
						plotWindow.processPlotUpdate(dataBean);
					notifyDataObservers(dataBean);
				} catch (Exception e) {
					logger.error("There has been an issue retrieving the databean from the plotserver", e);
				}
			}
		}
	}

	private void updateGuiBean(DataBean dataBean) {
		if (guiBean == null) {
			guiBean = new GuiBean();
		}
		if (dataBean.getGuiPlotMode() != null) {
			guiBean.put(GuiParameters.PLOTMODE, dataBean.getGuiPlotMode());
		}
		if (dataBean.getGuiParameters() != null) {
			guiBean.merge(dataBean.getGuiParameters());
		}
	}

	private Thread updateThread = null;

	private void updateBeans() {

		if (updateThread == null || updateThread.getState() == Thread.State.TERMINATED) {

			updateThread = new Thread(new Runnable() {

				@Override
				public void run() {
					runUpdate();
				}
			}, "PlotViewUpdateThread");

			updateThread.start();
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof String && changeCode.equals(plotViewName)) {
			logger.debug("Getting a plot data update from " + plotViewName);
			dataBeanAvailable = plotViewName;
			updateBeans();
		}
		if (changeCode instanceof GuiUpdate) {
			GuiUpdate gu = (GuiUpdate) changeCode;
			if (gu.getGuiName().contains(plotViewName)) {
				GuiBean bean = gu.getGuiData();
				logger.debug("Getting a plot gui update for \""+plotViewName+"\" plot: " + bean.toString());
				UUID id = (UUID) bean.get(GuiParameters.PLOTID);
				if (id == null || plotID.compareTo(id) != 0) { // filter out own beans
					if (guiBean == null)
						guiBean = bean.copy(); // cache a local copy
					else
						guiBean.merge(bean); // or merge it
					stashedGuiBean = bean;
					updateBeans();
				}
			}
		}

	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.clear();

	}

	/**
	 * Allow another observer to see plot data.
	 * <p>
	 * A data observer gets an update with a data bean.
	 * 
	 * @param observer
	 */
	public void addDataObserver(IObserver observer) {
		dataObservers.add(observer);
	}

	/**
	 * Remove a data observer
	 * 
	 * @param observer
	 */
	public void deleteDataObserver(IObserver observer) {
		dataObservers.remove(observer);
	}

	/**
	 * Remove all data observers
	 */
	public void deleteDataObservers() {
		dataObservers.clear();
	}

	private void notifyDataObservers(DataBean bean) {
		Iterator<IObserver> iter = dataObservers.iterator();
		while (iter.hasNext()) {
			IObserver ob = iter.next();
			ob.update(this, bean);
		}
	}

	/**
	 * Get gui information from plot server
	 */
	@Override
	public GuiBean getGUIInfo() {
		getGUIState();
		return guiBean;
	}

	private void getGUIState() {
		if (guiBean == null) {
			try {
				guiBean = plotServer.getGuiState(plotViewName);
			} catch (Exception e) {
				logger.warn("Problem with getting GUI data from plot server");
			}
			if (guiBean == null)
				guiBean = new GuiBean();
		}
	}

	private void pushGUIState() {
		try {
			plotServer.updateGui(plotViewName, guiBean);
		} catch (Exception e) {
			logger.warn("Problem with updating plot server with GUI data");
			e.printStackTrace();
		}
	}

	/**
	 * Push GUI information back to plot server
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void putGUIInfo(GuiParameters key, Serializable value) {
		getGUIState();

		guiBean.put(GuiParameters.PLOTID, plotID); // put plotID in bean

		guiBean.put(key, value);

		pushGUIState();
	}

	/**
	 * Remove GUI information from plot server
	 * 
	 * @param key
	 */
	@Override
	public void removeGUIInfo(GuiParameters key) {
		getGUIState();

		guiBean.put(GuiParameters.PLOTID, plotID); // put plotID in bean

		guiBean.remove(key);

		pushGUIState();
	}

	@Override
	public void dispose() {
		if (plotWindow != null)
			plotWindow.dispose();
		// plotConsumer.stop();
		execSvc.shutdown();
		deleteIObservers();
		deleteDataObservers();
		System.gc();
	}

	public String getPlotViewName() {
		return plotViewName;
	}

	public void updatePlotMode(GuiPlotMode mode) {
		plotWindow.updatePlotMode(mode);

	}

	public void processPlotUpdate(DataBean dBean) {
		plotWindow.processPlotUpdate(dBean);
		notifyDataObservers(dBean);
	}

	public void processGUIUpdate(GuiBean bean) {
		plotWindow.processGUIUpdate(bean);
	}

	@Override
	public DataSetPlotter getMainPlotter() {
		return plotWindow.getMainPlotter();
	}

	public AbstractPlottingSystem getPlottingSystem() {
		return plotWindow.getPlottingSystem();
	}

	public PlotWindow getPlotWindow() {
		return this.plotWindow;
	}

	@Override
	public void updateProcessed() {
	}

	@Override
	public IMetaData getMetadata() throws Exception {
		if (getMainPlotter() == null)
			return null;

		IDataset currentData = getMainPlotter().getCurrentDataSet();
		if (currentData != null)
			return currentData.getMetadata();
		return null;
	}

	@Override
	public SidePlotPreference getSidePlotPreference() {
		// TODO Return the real side preference, always diffraction for now.
		return SidePlotPreference.DIFFRACTION_3D;
	}

}
