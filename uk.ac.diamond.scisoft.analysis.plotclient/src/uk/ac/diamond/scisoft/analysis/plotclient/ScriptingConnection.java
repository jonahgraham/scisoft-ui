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

package uk.ac.diamond.scisoft.analysis.plotclient;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotclient.connection.AbstractPlotConnection;
import uk.ac.diamond.scisoft.analysis.plotclient.connection.IPlotConnection;
import uk.ac.diamond.scisoft.analysis.plotclient.connection.PlotConnectionFactory;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisOperation;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;


/**
 * Class for connecting a plotting system with the plot server.
 */
public class ScriptingConnection implements IObservable {
	
	static private Logger logger = LoggerFactory.getLogger(ScriptingConnection.class);

	protected IPlottingSystem plottingSystem;
	protected String name;

	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private IBeanScriptingManager manager = null;
	private IUpdateNotificationListener notifyListener = null;
	
	protected DataBean myBeanMemory;
	protected ROIManager roiManager;

	protected IPlotConnection plotConnection = null;
	private boolean isUpdatePlot = false;

	private GuiPlotMode previousMode;

	/**
	 * 
	 * @param manager
	 * @param name
	 */
	public ScriptingConnection(String name) {
		this(new BeanScriptingManagerImpl(PlotServerProvider.getPlotServer(), name), name);
	}

	/**
	 * 
	 * @param manager
	 * @param name
	 */
	public ScriptingConnection(IBeanScriptingManager manager, String name) {
		
		this.manager = manager;
		this.name    = name;
		roiManager = new ROIManager(manager, name);
	}
	
	/**
	 * Set to null not to receive updates
	 * @param listener
	 */
	public void setNotifyListener(IUpdateNotificationListener listener) {
		this.notifyListener = listener;
	}
	
	/**
	 * Call to set the plotting system to which we will connect.
	 * 
	 * This must be called!
	 * 
	 * @param system
	 */
	public void setPlottingSystem(IPlottingSystem system) {
		
		if (plottingSystem!=null) throw new IllegalArgumentException("The plotting system has already been set!");
		
		this.plottingSystem = system;
		
		system.addRegionListener(getRoiManager());
		system.addTraceListener(getRoiManager().getTraceListener());
		
		if (manager instanceof BeanScriptingManagerImpl) {
			
			BeanScriptingManagerImpl man = (BeanScriptingManagerImpl)manager;
			man.setConnection(this);
							
			GuiBean bean = manager.getGUIInfo();
			updatePlotMode(bean, false);
			
			final PlotEvent evt = new PlotEvent();
			evt.setDataBeanAvailable(name);
			evt.setStashedGuiBean(manager.getGUIInfo());
			man.offer(evt);

		}

		GuiPlotMode plotMode = getPlotMode();
		changePlotMode(plotMode == null ? GuiPlotMode.ONED : plotMode);
	}


	/**
	 * Set the default plot mode
	 * @return plotmode
	 */
	public GuiPlotMode getPlotMode() {
		return PlotConnectionFactory.getPlotMode(plottingSystem);
	}

	/**
	 * @return plot UI
	 */
	public IPlotConnection getPlotUI() {
		return plotConnection;
	}

	/**
	 * Cleaning of the plotting system and its composite before the setting up of a datasetPlotter
	 */
	protected void cleanUpPlottingSystem() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clearRegions();
			plottingSystem.removeRegionListener(getRoiManager());
			plottingSystem.removeTraceListener(getRoiManager().getTraceListener());
			plottingSystem.dispose();
		}
	}

	/**
	 * Return the name of the Window
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the IBeanScriptingManager of the window
	 * @return manager
	 */
	protected IBeanScriptingManager getGuiManager() {
		return manager;
	}

	/**
	 * Returns the notifyListener of the window
	 * @return notifyListener
	 */
	protected IUpdateNotificationListener getNotififyListener() {
		return notifyListener;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observers.add(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void deleteIObservers() {
		observers.clear();
	}

	protected List<IObserver> getObservers() {
		return observers;
	}

	public void notifyUpdateFinished() {
		if (notifyListener != null)
			notifyListener.updateProcessed();
	}

	private SimpleLock simpleLock = new SimpleLock();

	protected void doBlock() {
		logger.debug("doBlock " + Thread.currentThread().getId());
		synchronized (simpleLock) {
			if (simpleLock.isLocked()) {
				try {
					logger.debug("doBlock  - waiting " + Thread.currentThread().getId());
					simpleLock.wait();
					logger.debug("doBlock  - locking " + Thread.currentThread().getId());
				} catch (InterruptedException e) {
					// do nothing - but return
				}
			} else {
				logger.debug("doBlock  - waiting not needed " + Thread.currentThread().getId());
			}
			simpleLock.lock();
		}
	}

	protected void undoBlock() {
		synchronized (simpleLock) {
			logger.debug("undoBlock " + Thread.currentThread().getId());
			simpleLock.unlock();
			simpleLock.notifyAll();
		}
	}


	/**
	 * Process a plot with data packed in bean - remember to update plot mode first if you do not know the current mode
	 * or if it is to change
	 * @param dbPlot
	 */
	public void processPlotUpdate(DataBean dbPlot) {
		// check to see what type of plot this is and set the plotMode to the correct one
		if (dbPlot.getGuiPlotMode() != null) {
			if (plottingSystem!=null && plottingSystem.isDisposed()) {
				// this can be caused by the same plot view shown on 2 difference perspectives.
				throw new IllegalStateException("parentComp is already disposed");
			}

			updatePlotMode(dbPlot.getGuiPlotMode(), true);
		}
		// there may be some gui information in the databean, if so this also needs to be updated
		if (dbPlot.getGuiParameters() != null) {
			processGUIUpdate(dbPlot.getGuiParameters());
		}
		if (plotConnection != null) {
			try {
				doBlock();
				// Now plot the data as standard
				plotConnection.processPlotUpdate(dbPlot, isUpdatePlot());
				setDataBean(dbPlot);
				createRegion();
			} finally {
				undoBlock();
			}
		}
	}

	/**
	 * Creates a region if needed
	 */
	public void createRegion() {
		
	}

	/**
	 * Update the GuiBean
	 * @param bean
	 */
	public void processGUIUpdate(GuiBean bean) {
		setUpdatePlot(false);
		if (bean.containsKey(GuiParameters.PLOTMODE)) {
			updatePlotMode(bean, true);
		}

		if (bean.containsKey(GuiParameters.AXIS_OPERATION)) {
			AxisOperation operation = (AxisOperation) bean.get(GuiParameters.AXIS_OPERATION);
			processAxisOperation(operation);
		}

		if (bean.containsKey(GuiParameters.PLOTOPERATION)) {
			String opStr = (String) bean.get(GuiParameters.PLOTOPERATION);
			if (opStr.equals(GuiParameters.PLOTOP_UPDATE)) {
				setUpdatePlot(true);
			}
		}

		if (plotConnection != null && (bean.containsKey(GuiParameters.ROICLEARALL) || bean.containsKey(GuiParameters.ROIDATA) || bean.containsKey(GuiParameters.ROIDATALIST))) {
			try {
				// lock ROI manager
				getRoiManager().acquireLock();
				plotConnection.processGUIUpdate(bean);
			} finally {
				// release ROI manager
				getRoiManager().releaseLock();
			}
		}
		if (bean.containsKey(GuiParameters.QUIET_UPDATE)) {
			manager.sendGUIInfo(bean);
		}
	}

	// this map is needed as axes from the plotting system get their titles changed
	private Map<IAxis, String> axes = new LinkedHashMap<IAxis, String>();

	private void processAxisOperation(final AxisOperation operation) {
		if (plottingSystem == null || plottingSystem.isDisposed())
			return;

		doBlock();
		plottingSystem.getPlotComposite().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					final List<IAxis> pAxes = plottingSystem.getAxes();
					if (axes.size() != 0 && axes.size() != pAxes.size()) {
						logger.warn("Axes are out of synch! {} cf {}", axes, pAxes);
						axes.clear();
					}
					if (axes.size() == 0) {
						for (IAxis i : pAxes) {
							String t = i.getTitle();
							if (i.isPrimaryAxis()) {
								if (t == null || t.length() == 0) { // override if empty
									t = i.isYAxis() ? "Y-Axis" : "X-Axis";
								}
							}
							axes.put(i, t);
						}
					}
					String title = operation.getTitle();
					String type = operation.getOperationType();
					IAxis a = null;
					if (axes.containsValue(title)) {
						for (IAxis i : axes.keySet()) {
							if (title.equals(axes.get(i))) {
								a = i;
								break;
							}
						}
					}
					if (type.equals(AxisOperation.CREATE)) {
						boolean isYAxis = operation.isYAxis();
						if (a != null) {
							if (isYAxis == a.isYAxis()) {
								logger.warn("Axis already exists: {}", title);
								return;
							}
							logger.debug("Axis is opposite orientation already exists");
						}
						a = plottingSystem.createAxis(title, isYAxis, operation.getSide());
						axes.put(a, title);
						logger.trace("Created: {}", title);
						return;
					} else if (type.equals(AxisOperation.RENAMEX)) {
						a = plottingSystem.getSelectedXAxis();
						a.setTitle(title);
						axes.put(a, title);
						logger.trace("Renamed x: {}", title);
					} else if (type.equals(AxisOperation.RENAMEY)) {
						a = plottingSystem.getSelectedYAxis();
						a.setTitle(title);
						axes.put(a, title);
						logger.trace("Renamed y: {}", title);
					}
				} finally {
					undoBlock();
				}
			}
		});
	}

	protected void changePlotMode(GuiPlotMode plotMode) {
		
		clearPlot(!GuiPlotMode.EMPTY.equals(getPreviousMode()));
		
		// TODO Baha check that this is ok - it seems to work and is
		// better than creating infinitely many plotUIs and not disposing
		// them, one would have imagined...
		if (plotConnection != null) {
			plotConnection.deactivate(false);
			plotConnection.dispose();
		}

		this.plotConnection = PlotConnectionFactory.getConnection(plotMode, plottingSystem);
		if (plotConnection!=null) ((AbstractPlotConnection)plotConnection).setManager(getRoiManager());
		
		if (plotMode.equals(GuiPlotMode.EMPTY)) {
			resetAxes();
		}
		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	/**
	 * Update the Plot Mode
	 * @param plotMode
	 * @param async
	 */
	public void updatePlotMode(final GuiPlotMode plotMode, boolean async) {
		
		doBlock();
		
		DisplayUtils.runInDisplayThread(async, plottingSystem.getPlotComposite(), new Runnable() {
			@Override
			public void run() {
				try {
					if (plotMode.equals(GuiPlotMode.EXPORT)) {
						GuiBean bean = getGuiManager().getGUIInfo();
						getPlottingSystem().savePlotting((String)bean.get(GuiParameters.SAVEPATH), 
														(String)bean.get(GuiParameters.FILEFORMAT));
					} else if (plotMode.equals(GuiPlotMode.RESETAXES)) {
						resetAxes();
					} else {
						GuiPlotMode oldMode = getPreviousMode();
						if (oldMode == null || !plotMode.equals(oldMode)) {
							changePlotMode(plotMode);
							setPreviousMode(plotMode);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Error exporting plot:"+e.getMessage());
				} finally {
					undoBlock();
				}
			}
		});
	}

	/**
	 * Update the Plot mode with a GuiBean
	 * @param bean
	 * @param async
	 */
	public void updatePlotMode(GuiBean bean, boolean async) {
		if (bean != null) {
			if (bean.containsKey(GuiParameters.PLOTMODE)) { // bean does not necessarily have a plot mode (eg, it
															// contains ROIs only)
				GuiPlotMode plotMode = (GuiPlotMode) bean.get(GuiParameters.PLOTMODE);
				if (plotMode != null)
					updatePlotMode(plotMode, async);
			}
		}
	}

	/**
	 * Clear the Plot Window and its components
	 */
	public void clearPlot() {
		clearPlot(true);
	}

	private void clearPlot(boolean resetAxes) {
		if (plottingSystem != null) {
			plottingSystem.clear();
			if (resetAxes)
				plottingSystem.resetAxes();
		}
	}

	/**
	 * Sets the visibility of region according to Plot type
	 * @param type
	 */
	protected void setVisibleByPlotType(PlotType type) {
		Collection<IRegion> regions = plottingSystem.getRegions();
		if (regions == null)
			return;
		for (IRegion iRegion : regions) {
			iRegion.setVisible(iRegion.getPlotType().equals(type));
		}
	}

	/**
	 * Reset the axes in the Plot Window
	 */
	private void resetAxes() {
		if (plottingSystem != null) {
			plottingSystem.resetAxes();
		}
	}

	public DataBean getDataBean() {
		return myBeanMemory;
	}

	public void setDataBean(DataBean bean) {
		myBeanMemory = bean;
	}

	public boolean isUpdatePlot() {
		return isUpdatePlot;
	}

	public void setUpdatePlot(boolean isUpdatePlot) {
		this.isUpdatePlot = isUpdatePlot;
	}

	public GuiPlotMode getPreviousMode() {
		return previousMode;
	}

	public void setPreviousMode(GuiPlotMode previousMode) {
		this.previousMode = previousMode;
	}

	public ROIManager getRoiManager() {
		return roiManager;
	}

	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	public Object getAdapter(final Class<?> clazz) {
		return plottingSystem != null ? plottingSystem.getAdapter(clazz) : null;
	}

	public void dispose() {
		
		if (plotConnection != null) {
			plotConnection.deactivate(false);
			plotConnection.dispose();
		}
		try {
			if (plottingSystem != null && !plottingSystem.isDisposed()) {
				plottingSystem.removeTraceListener(getRoiManager().getTraceListener());
				plottingSystem.removeRegionListener(getRoiManager());
			}
		} catch (Exception ne) {
			logger.debug("Cannot clean up plotter!", ne);
		}
		deleteIObservers();
		plotConnection = null;
	}
}
