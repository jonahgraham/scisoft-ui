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
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IUpdateNotificationListener;

/**
 * Abstract Class from which PlotView and ROIProfilePlotView both extend 
 * Used to create the main Analysis panel that can display any n-D scalar data 
 * it is the replacement of the Data Vector panel inside the new RCP framework
 * (different from uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView)
 */
public abstract class AbstractPlotView extends ViewPart implements IObserver, IObservable, IGuiInfoManager, IUpdateNotificationListener,
		ISidePlotPart {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlotView.class);

	/**
	 * the ID of the view
	 */
	protected String id;

	private PlotServer plotServer;
	private ExecutorService execSvc = null;
	protected String plotViewName = "Plot View";
	private IPlotUI plotUI = null;
	private UUID plotID = null;
	private GuiBean guiBean = null;
	private GuiBean stashedGuiBean;
	private String dataBeanAvailable;

	private Set<IObserver> dataObservers = Collections.synchronizedSet(new LinkedHashSet<IObserver>());
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	private Thread updateThread = null;

	/**
	 * Default Constructor of the plot view
	 */

	public AbstractPlotView() {
		super();
		init();
	}

	/**
	 * Constructor which must be called by 3rd party extension to extension point
	 * "uk.ac.diamond.scisoft.analysis.rcp.plotView"
	 * 
	 * @param id
	 */
	public AbstractPlotView(String id) {
		super();
		this.id = id;
		init();
	}

	private void init() {
		plotID = UUID.randomUUID();
		logger.info("Plot view uuid: {}", plotID);
		setPlotServer(PlotServerProvider.getPlotServer());
		getPlotServer().addIObserver(this);
		setExecSvc(Executors.newFixedThreadPool(2));
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public  Object getAdapter(final Class clazz) {
		return super.getAdapter(clazz);
	}

	@Override
	public void setFocus() {
	}

	/**
	 * Clear the PlotWindow
	 */
	public abstract void clearPlot();

	/**
	 * Update the PlotWindow
	 */
	public abstract void runUpdate();

	public void updateGuiBean(DataBean dataBean) {
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

	/**
	 * Update the beans
	 */
	public void updateBeans() {

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
			setDataBeanAvailable(plotViewName);
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
					setStashedGuiBean(bean);
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

	public void notifyDataObservers(DataBean bean) {
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
				guiBean = getPlotServer().getGuiState(plotViewName);
			} catch (Exception e) {
				logger.warn("Problem with getting GUI data from plot server");
			}
			if (guiBean == null)
				guiBean = new GuiBean();
		}
	}

	private void pushGUIState() {
		try {
			getPlotServer().updateGui(plotViewName, guiBean);
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

	public String getPlotViewName() {
		return plotViewName;
	}

	/**
	 * Update the Plot Mode
	 * @param mode
	 */
	public abstract void updatePlotMode(GuiPlotMode mode) ;

	/**
	 * Update the DataBean
	 * @param dBean
	 */
	public abstract void processPlotUpdate(DataBean dBean);

	/**
	 * Update the GuiBean
	 * @param bean
	 */
	public abstract void processGUIUpdate(GuiBean bean);

	/**
	 * Get the Lightweight PlottingSystem
	 * @return plottingSystem
	 */
	public abstract AbstractPlottingSystem getPlottingSystem();

	@Override
	public void updateProcessed() {
		// do nothing
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

	/**
	 * @return plot UI
	 */
	public IPlotUI getPlotUI() {
		return plotUI;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDataBeanAvailable() {
		return dataBeanAvailable;
	}

	public void setDataBeanAvailable(String dataBeanAvailable) {
		this.dataBeanAvailable = dataBeanAvailable;
	}

	public GuiBean getStashedGuiBean() {
		return stashedGuiBean;
	}

	public void setStashedGuiBean(GuiBean stashedGuiBean) {
		this.stashedGuiBean = stashedGuiBean;
	}

	public PlotServer getPlotServer() {
		return plotServer;
	}

	public void setPlotServer(PlotServer plotServer) {
		this.plotServer = plotServer;
	}

	public ExecutorService getExecSvc() {
		return execSvc;
	}

	public void setExecSvc(ExecutorService execSvc) {
		this.execSvc = execSvc;
	}

}
