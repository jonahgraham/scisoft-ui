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

import org.dawnsci.plotting.api.IPlottingContainer;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.tool.IToolPage;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
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
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.ExamplePlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IUpdateNotificationListener;

/**
 * Abstract Class from which PlotView and ROIProfilePlotView both extend 
 * Used to create the main Analysis panel that can display any n-D scalar data 
 * it is the replacement of the Data Vector panel inside the new RCP framework
 * (different from uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView)
 */
@SuppressWarnings("deprecation")
public abstract class AbstractPlotView extends ViewPart implements IObserver, IObservable, IGuiInfoManager, IUpdateNotificationListener,
		ISidePlotPart, IPlottingContainer {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlotView.class);

	/**
	 * the ID of the view
	 */
	protected String id;

	protected AbstractPlotWindow plotWindow;

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

	private boolean update;

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

	@Override
	public void createPartControl(Composite parent) {

		if (getId() != null) {
			// process extension configuration
			logger.info("ID: {}", getId());
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
		setStashedGuiBean(bean);
		plotWindow = createPlotWindow(parent, (GuiPlotMode) bean.get(GuiParameters.PLOTMODE), this, this, getViewSite()
				.getActionBars(), getSite().getPage(), plotViewName);
		plotWindow.updatePlotMode(bean, false);

		// plotConsumer.addIObserver(this);
		setDataBeanAvailable(plotViewName);
		updateBeans();
		
		//catch any errors from addTool and ignore so view is always created cleanly
		try {
			addToolIfRequired();
		} catch (Exception e) {
			//do nothing here but log
			logger.warn(e.getMessage());
		}
	}

	/**
	 * Create a custom Plot Window which will enable to create a custom Plot View with custom 
	 * controls side by side with an IPlottingSystem linked to the plot server.<br>
	 * (See {@link ExamplePlotWindow} for more info).
	 * @return AbstractPlotWindow
	 */
	public abstract AbstractPlotWindow createPlotWindow(Composite parent, 
														GuiPlotMode plotMode, 
														IGuiInfoManager manager,
														IUpdateNotificationListener notifyListener, 
														IActionBars bars, 
														IWorkbenchPage page, 
														String name);

	@Override
	public void setFocus() {
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plotWindow.getAdapter(clazz);
		}else if (clazz == IPlottingSystem.class) {
			return getPlottingSystem();
		}
		return super.getAdapter(clazz);
	}

	public void clearPlot() {
		plotWindow.clearPlot();
	}

	public void runUpdate() {
		while (getDataBeanAvailable() != null || getStashedGuiBean() != null) {
			GuiBean bean = getStashedGuiBean();
			String beanLocation = getDataBeanAvailable();

			// if there is a stashedGUIBean to update then do that update first
			if (bean != null) {
				setStashedGuiBean(null);
				if (plotWindow != null)
					plotWindow.processGUIUpdate(guiBean);
			}

			// once the guiBean has been sorted out, see if there is any need to update the dataBean
			if (beanLocation != null) {
				setDataBeanAvailable(null);
				try {
					final DataBean dataBean;
					dataBean = getPlotServer().getData(beanLocation);

					if (dataBean == null)
						return;

					// update the GUI if needed
					updateGuiBean(dataBean);
					if (plotWindow != null)
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

		getPlotServer().deleteIObserver(this);
		getExecSvc().shutdown();
		deleteIObservers();
		deleteDataObservers();
		System.gc();
	}

	public void updatePlotMode(GuiPlotMode mode) {
		plotWindow.updatePlotMode(mode, true);
	}

	public void processPlotUpdate(DataBean dBean) {
		plotWindow.processPlotUpdate(dBean);
		notifyDataObservers(dBean);
	}

	public void processGUIUpdate(GuiBean bean) {
		plotWindow.processGUIUpdate(bean);
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plotWindow.getPlottingSystem();
	}

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
		} else {
			// skips!
			logger.trace("Dropping GUI bean update");
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (changeCode instanceof String && changeCode.equals(plotViewName)) {
			logger.debug("Getting a plot data update from {}; thd {}",  plotViewName, Thread.currentThread().getId());
			setDataBeanAvailable(plotViewName);
			updateBeans();
		}
		if (changeCode instanceof GuiUpdate) {
			GuiUpdate gu = (GuiUpdate) changeCode;
			if (gu.getGuiName().contains(plotViewName)) {
				GuiBean bean = gu.getGuiData();
				UUID id = (UUID) bean.get(GuiParameters.PLOTID);
				if (id == null || plotID.compareTo(id) != 0) { // filter out own beans
					logger.debug("Getting a plot gui update for {}; thd {}; bean {}", new Object[] {plotViewName, Thread.currentThread().getId(), bean});
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

		if (update)
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

		if (update)
			pushGUIState();
	}

	@Override
	public void mute() {
		update = false;
	}

	@Override
	public void unmute() {
		update = true;
		pushGUIState();	
	}

	public String getPlotViewName() {
		return plotViewName;
	}

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

	public AbstractPlotWindow getPlotWindow() {
		return this.plotWindow;
	}

	@Override
	public DataSetPlotter getMainPlotter() {
		return plotWindow.getMainPlotter();
	}

	private void addToolIfRequired() throws Exception {
		IExtension[] extensions = getExtensions("uk.ac.diamond.scisoft.analysis.rcp.views.PlotViewWithTool");
		if (extensions == null) return;

		for(int i=0; i<extensions.length; i++) {

			IExtension extension = extensions[i];
			IConfigurationElement[] configElements = extension.getConfigurationElements();	

			for(int j=0; j<configElements.length; j++) {
				IConfigurationElement config = configElements[j];
				config.toString();

				String view = config.getAttribute("view_id");
				String tool = config.getAttribute("tool_id");
				String id = getViewSite().getId();

				if (id.equals(view)){

					final IToolPageSystem system = (IToolPageSystem)getAdapter(IToolPageSystem.class);
					
					IToolPage toolpage = system.getToolPage(tool);
					String toolViewId = "";
					
					switch (toolpage.getToolPageRole()) {
					case ROLE_1D:
						toolViewId ="org.dawb.workbench.plotting.views.toolPageView.1D";
						break;
					case ROLE_2D:
						toolViewId ="org.dawb.workbench.plotting.views.toolPageView.2D";
						break;
					case ROLE_1D_AND_2D:
						toolViewId ="org.dawb.workbench.plotting.views.toolPageView.1D_2D";
						break;
					case ROLE_3D:
						toolViewId ="org.dawb.workbench.plotting.views.toolPageView.3D";
						break;
					}
					
					if (!toolViewId.isEmpty()) {
						system.setToolVisible(tool, toolpage.getToolPageRole(), toolViewId);
					}
				}
			}
		}
	}

	private IExtension[] getExtensions(String extensionPointId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
		IExtension[] extensions = point.getExtensions();
		return extensions;
	}
}
