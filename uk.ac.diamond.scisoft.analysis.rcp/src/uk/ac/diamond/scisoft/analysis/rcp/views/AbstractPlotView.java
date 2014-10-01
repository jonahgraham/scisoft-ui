/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;

import gda.observable.IObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.views.ISettablePlotView;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.plotserver.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.ExamplePlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlottingUI;

/**
 * Abstract Class from which PlotView and ROIProfilePlotView both extend 
 * Used to create the main Analysis panel that can display any n-D scalar data 
 * it is the replacement of the Data Vector panel inside the new RCP framework
 * (different from uk.ac.diamond.scisoft.analysis.rcp.views.plot.AbstractPlotView)
 */
public abstract class AbstractPlotView extends ViewPart implements ISettablePlotView,
																   IObserver,
																   IGuiInfoManager {

	// Adding in some logging to help with getting this running
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlotView.class);

	/**
	 * the ID of the view
	 */
	protected String        id;
	protected AbstractPlotWindow plotWindow;
	protected String        plotViewName = "Plot View";

	private PlotServer      plotServer;
	private IPlottingUI         plotUI;
	private UUID            plotID;

	private Set<IObserver>   dataObservers = Collections.synchronizedSet(new LinkedHashSet<IObserver>());

	private BlockingDeque<PlotEvent> queue;
	private boolean                  isDisposed;

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
		
		// Blocking queue to which we add plot update events.
		this.queue = new LinkedBlockingDeque<PlotEvent>(25);
		
		// We have a thread which processes the queue
		Thread plotThread = createPlotEventThread();
		plotThread.setDaemon(true);
		plotThread.start();
	}

	private Thread createPlotEventThread() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				while (!isDisposed()) {
					try {
						PlotEvent event = queue.take();
						if (event.getStashedGuiBean()==null && event.getGuiBean()==null) {
							// This event is not of interest
							continue;
						}
						
						GuiBean bean = event.getStashedGuiBean();
						String beanLocation = event.getDataBeanAvailable();
	
						// if there is a stashedGUIBean to update then do that update first
						if (bean != null) {
							event.setStashedGuiBean(null);
							if (plotWindow != null)
								plotWindow.processGUIUpdate(bean);
						}
	
						// once the guiBean has been sorted out, see if there is any need to update the dataBean
						if (beanLocation != null) {
							event.setDataBeanAvailable(null);
							try {
								final DataBean dataBean;
								dataBean = getPlotServer().getData(beanLocation);
	
								if (dataBean == null) continue;
	
								// update the GUI if needed
								GuiBean guiBean = event.getGuiBean();
								if (guiBean == null) guiBean = new GuiBean();

								// do not add plot mode as this is done in plot window
								if (dataBean.getGuiParameters() != null) {
									guiBean.merge(dataBean.getGuiParameters());
								}
								if (plotWindow != null) {
									plotWindow.processPlotUpdate(dataBean);
								}
								notifyDataObservers(dataBean);
							} catch (Exception e) {
								logger.error("There has been an issue retrieving the databean from the plotserver", e);
							}
						}
					} catch (Throwable ne) {
						logger.debug("Exception raised in plot server job");
						continue; // We still keep going until the part is disposed
					}
				}
			}
		}, "Plot View Update Daemon '"+plotID+"'");
	}

	protected boolean isDisposed() {
		return isDisposed;
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

		final GuiBean bean = getGUIBean();
		plotWindow = createPlotWindow(parent, this, getViewSite().getActionBars(), getSite().getPage(), plotViewName);
		plotWindow.updatePlotMode(bean, false);

		// plotConsumer.addIObserver(this);
		final PlotEvent evt = new PlotEvent();
		evt.setDataBeanAvailable(plotViewName);
		evt.setStashedGuiBean(bean);
		offer(evt);

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
														IGuiInfoManager manager,
														IActionBars bars, 
														IWorkbenchPage page, 
														String name);

	@Override
	public void setFocus() {
		plotWindow.setFocus();
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

	@Override
	public void dispose() {
		
		this.isDisposed = true;
		queue.clear();
		queue.add(new PlotEvent());
				
		if (plotWindow != null) plotWindow.dispose();

		getPlotServer().deleteIObserver(this);
		deleteDataObservers();
	}

	/**
	 * Tries to put plot event into queue or 
	 * drops some events from the queue
	 * 
	 * TODO dropping of events should be done if
	 * the event is a plot update. Region events
	 * should never be dropped.
	 * 
	 * @param evt
	 */
	private synchronized void offer(PlotEvent evt) {
		if (queue.offer(evt)) return;
		queue.remove(); // drop the head - TODO FIXME not region events!
		if (!queue.offer(evt)) {
			throw new RuntimeException("Cannot offer plot events to queue!");
		}
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

	public IPlottingSystem getPlottingSystem() {
		return plotWindow.getPlottingSystem();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		
		if (changeCode instanceof String && changeCode.equals(plotViewName)) {
			logger.debug("Getting a plot data update for {}; thd {}",  plotViewName, Thread.currentThread().getId());
			GuiBean     guiBean = getGUIBean();
			final PlotEvent evt = new PlotEvent();
			evt.setDataBeanAvailable(plotViewName);
			evt.setGuiBean(guiBean);
			offer(evt);
			
		} else if (changeCode instanceof GuiUpdate) {
			GuiUpdate gu = (GuiUpdate) changeCode;
			if (gu.getGuiName().contains(plotViewName)) {
				
				GuiBean        bean = gu.getGuiData();
				final PlotEvent evt = new PlotEvent();
				GuiBean     guiBean = getGUIBean();
				
				UUID id = (UUID) bean.get(GuiParameters.PLOTID);
				if (id == null || plotID.compareTo(id) != 0) { // filter out own beans
					logger.debug("Getting a plot gui update for {}; thd {}; bean {}", new Object[] {plotViewName, Thread.currentThread().getId(), bean});
					if (guiBean == null) {
						guiBean = bean.copy(); // cache a local copy
					} else {
						guiBean.merge(bean); // or merge it
					}
					guiBean.remove(GuiParameters.ROICLEARALL); // this parameter must not persist
					evt.setStashedGuiBean(bean);
					evt.setGuiBean(guiBean);
					offer(evt);
				}
			}
		}
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
		return getGUIBean();
	}

	private GuiBean getGUIBean() {
		GuiBean guiBean = null;
		try {
			guiBean = getPlotServer().getGuiState(plotViewName);
		} catch (Exception e) {
			logger.warn("Problem with getting GUI data from plot server");
		}
		if (guiBean == null) {
			logger.error("This should not happen!");
			guiBean = new GuiBean();
		}
		return guiBean;
	}

	/**
	 * Push GUI information back to plot server
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void putGUIInfo(GuiParameters key, Serializable value) {
		GuiBean guiBean = getGUIBean();

		guiBean.put(key, value);

		sendGUIInfo(guiBean);
	}

	/**
	 * Remove GUI information from plot server
	 * 
	 * @param key
	 */
	@Override
	public void removeGUIInfo(GuiParameters key) {
		GuiBean guiBean = getGUIBean();

		guiBean.remove(key);

		sendGUIInfo(guiBean);
	}

	@Override
	public void sendGUIInfo(GuiBean guiBean) {
		guiBean.put(GuiParameters.PLOTID, plotID); // put plotID in bean

		try {
			getPlotServer().updateGui(plotViewName, guiBean);
		} catch (Exception e) {
			logger.warn("Problem with updating plot server with GUI data");
			e.printStackTrace();
		}
	}

	@Override
	public void updateData(Serializable data, Class<?> clazz) {
		if (data instanceof ArrayList<?>) {
			ArrayList<?> list = (ArrayList<?>) data;
			if (clazz.getName().equals(IPeak.class.getName())) {
				if (list.isEmpty()) {
					this.removeGUIInfo(GuiParameters.FITTEDPEAKS);
				} else {
					this.putGUIInfo(GuiParameters.FITTEDPEAKS, list);
				}
			}
		}
	}

	public String getPlotViewName() {
		return plotViewName;
	}

	/**
	 * @return plot UI
	 */
	private IPlottingUI getPlotUI() {
		return plotUI;
	}

	/**
	 * @return id
	 */
	private String getId() {
		return id;
	}

	private PlotServer getPlotServer() {
		return plotServer;
	}

	private void setPlotServer(PlotServer plotServer) {
		this.plotServer = plotServer;
	}

	public AbstractPlotWindow getPlotWindow() {
		return this.plotWindow;
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
					case ROLE_3D:
						toolViewId ="org.dawb.workbench.plotting.views.toolPageView.3D";
						break;
					default:
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
