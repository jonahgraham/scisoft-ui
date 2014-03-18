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

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.DisplayUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.AxisOperation;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.ClearPlottingSystemAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DuplicatePlotAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.InjectPyDevConsoleHandler;
import uk.ac.diamond.scisoft.analysis.rcp.views.ExamplePlotView;

/**
 * Abstract Class used to implement PlotWindows that implement IObserver, IObservable
 */
public abstract class AbstractPlotWindow implements IPlotWindow, IObserver, IObservable {

	static private Logger logger = LoggerFactory.getLogger(AbstractPlotWindow.class);

	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	protected IPlottingSystem plottingSystem;

	protected Composite parentComp;
	protected Composite plotSystemComposite;
	private IWorkbenchPage page = null;
	protected IActionBars bars;
	private String name;

	/**
	 * PlotWindow may be given toolbars exclusive to the workbench part. In this case, there is no need to remove
	 * actions from the part.
	 */
	private boolean exclusiveToolars = false;

	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private IGuiInfoManager manager = null;
	private IUpdateNotificationListener notifyListener = null;
	private DataBean myBeanMemory;
	private ROIManager roiManager;

	protected IPlottingUI plotUI = null;
	private boolean isUpdatePlot = false;

	private GuiPlotMode previousMode;

	private CommandContributionItem duplicateWindowCCI;
	private CommandContributionItem openPyDevConsoleCCI;
	private CommandContributionItem updateDefaultPlotCCI;
	private CommandContributionItem getPlotBeanCCI;
	private CommandContributionItem clearPlotCCI;

	/**
	 * Constructor
	 * @param parent
	 * @param manager
	 * @param notifyListener
	 * @param bars
	 * @param page
	 * @param name
	 */
	public AbstractPlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, 
			IWorkbenchPage page, String name) {
		this.parentComp = parent;
		this.manager = manager;
		this.notifyListener = notifyListener;
		this.bars = bars;
		this.page = page;
		this.name = name;
		roiManager = new ROIManager(manager, name);

		plotMode = getPlotMode();

		changePlotMode(plotMode == null ? GuiPlotMode.ONED : plotMode, true);
	}

	/**
	 * Create the IPlottingSystem and other controls if necessary<br>
	 * The IPlottingSystem should be created the following way:<br>
	 * {@code 	plottingSystem = PlottingFactory.createPlottingSystem();}<br>
	 * {@code	plottingSystem.setColorOption(ColorOption.NONE);}<br>
	 * {@code	plottingSystem.createPlotPart(parent, getName(), bars, PlotType.XY, (IViewPart) getGuiManager());}<br>
	 * {@code	plottingSystem.repaint();}<br>
	 * {@code	plottingSystem.addRegionListener(getRoiManager());}<br>
	 * <br>
	 * (see {@link ExamplePlotView} for more info.)
	 * @param composite
	 */
	public abstract void createPlottingSystem(Composite composite);

	/**
	 * Set the default plot mode
	 * @return plotmode
	 */
	public abstract GuiPlotMode getPlotMode();

	/**
	 * @return plot UI
	 */
	public IPlottingUI getPlotUI() {
		return plotUI;
	}

	/**
	 * Cleaning of the plotting system and its composite before the setting up of a datasetPlotter
	 */
	protected void cleanUpPlottingSystem() {
		if (!plottingSystem.isDisposed()) {
			bars.getToolBarManager().removeAll();
			bars.getMenuManager().removeAll();
			for (Iterator<IRegion> iterator = plottingSystem.getRegions().iterator(); iterator.hasNext();) {
				IRegion region = iterator.next();
				plottingSystem.removeRegion(region);
			}
			plottingSystem.removeRegionListener(getRoiManager());
			plottingSystem.dispose();
			plotSystemComposite.dispose();
		}
	}
	/**
	 * Return current page.
	 * @return current page
	 */
	@Override
	public IWorkbenchPage getPage() {
		return page;
	}

	/**
	 * Return the name of the Window
	 * @return name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the IGuiInfoManager of the window
	 * @return manager
	 */
	protected IGuiInfoManager getGuiManager() {
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

	protected void notifyUpdateFinished() {
		if (notifyListener != null)
			notifyListener.updateProcessed();
	}

	SimpleLock simpleLock = new SimpleLock();

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
	 * Method to create the PlotView duplicating actions
	 * for DatasetPlotter and LightWeightPlottingSystem
	 */
	protected void addDuplicateAction(){
		if (duplicateWindowCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, DuplicatePlotAction.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Create Duplicate Plot";
			ccip.icon = AnalysisRCPActivator.getImageDescriptor("icons/chart_curve_add.png");
			duplicateWindowCCI = new CommandContributionItem(ccip);
		}
		bars.getMenuManager().add(duplicateWindowCCI);
	}

	/**
	 * Method to create the PlotView plotting system clear action
	 * for the LightWeightPlottingSystem
	 */
	protected void addClearAction(){
		if (clearPlotCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, ClearPlottingSystemAction.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Clear Plot";
			ccip.icon = AnalysisRCPActivator.getImageDescriptor("icons/clear.gif");
			clearPlotCCI = new CommandContributionItem(ccip);
		}
		bars.getMenuManager().add(clearPlotCCI);
	}

	/**
	 * Method to create the scripting actions
	 * for DatasetPlotter and LightWeightPlottingSystem
	 */
	protected void addScriptingAction(){
		if (openPyDevConsoleCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, InjectPyDevConsoleHandler.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Open New Plot Scripting";
			ccip.icon = AnalysisRCPActivator.getImageDescriptor("icons/application_osx_terminal.png");
			Map<String, String> params = new HashMap<String, String>();
			params.put(InjectPyDevConsoleHandler.CREATE_NEW_CONSOLE_PARAM, Boolean.TRUE.toString());
			params.put(InjectPyDevConsoleHandler.VIEW_NAME_PARAM, name);
			params.put(InjectPyDevConsoleHandler.SETUP_SCISOFTPY_PARAM,
					InjectPyDevConsoleHandler.SetupScisoftpy.ALWAYS.toString());
			ccip.parameters = params;
			openPyDevConsoleCCI = new CommandContributionItem(ccip);
		}

		if (updateDefaultPlotCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, InjectPyDevConsoleHandler.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Set Current Plot As Scripting Default";
			Map<String, String> params = new HashMap<String, String>();
			params.put(InjectPyDevConsoleHandler.VIEW_NAME_PARAM, name);
			ccip.parameters = params;
			updateDefaultPlotCCI = new CommandContributionItem(ccip);
		}

		if (getPlotBeanCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, InjectPyDevConsoleHandler.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Get Plot Bean in Plot Scripting";
			Map<String, String> params = new HashMap<String, String>();
			params.put(InjectPyDevConsoleHandler.INJECT_COMMANDS_PARAM, "bean=dnp.plot.getbean('" + name + "')");
			ccip.parameters = params;
			getPlotBeanCCI = new CommandContributionItem(ccip);
		}
		bars.getMenuManager().add(openPyDevConsoleCCI);
		bars.getMenuManager().add(updateDefaultPlotCCI);
		bars.getMenuManager().add(getPlotBeanCCI);
	}

	/**
	 * Remove the actions previously created
	 */
	protected void removePreviousActions() {

		IContributionItem[] items = bars.getToolBarManager().getItems();
		for (int i = 0; i < items.length; i++)
			items[i].dispose();
		bars.getToolBarManager().removeAll();

		bars.getMenuManager().removeAll();
		bars.getStatusLineManager().removeAll();
	}

	public boolean isExclusiveToolars() {
		return exclusiveToolars;
	}

	public void setExclusiveToolars(boolean exclusiveToolars) {
		this.exclusiveToolars = exclusiveToolars;
	}

	/**
	 * Process a plot with data packed in bean - remember to update plot mode first if you do not know the current mode
	 * or if it is to change
	 * @param dbPlot
	 */
	public void processPlotUpdate(DataBean dbPlot) {
		// check to see what type of plot this is and set the plotMode to the correct one
		if (dbPlot.getGuiPlotMode() != null) {
			if (parentComp.isDisposed()) {
				// this can be caused by the same plot view shown on 2 difference perspectives.
				throw new IllegalStateException("parentComp is already disposed");
			}

			updatePlotMode(dbPlot.getGuiPlotMode(), true);
		}
		// there may be some gui information in the databean, if so this also needs to be updated
		if (dbPlot.getGuiParameters() != null) {
			processGUIUpdate(dbPlot.getGuiParameters());
		}
		try {
			doBlock();
			// Now plot the data as standard
			plotUI.processPlotUpdate(dbPlot, isUpdatePlot());
			setDataBean(dbPlot);
		} finally {
			undoBlock();
		}
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

		if (bean.containsKey(GuiParameters.ROICLEARALL) || bean.containsKey(GuiParameters.ROIDATA) || bean.containsKey(GuiParameters.ROIDATALIST)) {
			try {
				// lock ROI manager
				getRoiManager().acquireLock();
				plotUI.processGUIUpdate(bean);
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
		parentComp.getDisplay().asyncExec(new Runnable() {
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

	private void changePlotMode(GuiPlotMode plotMode, boolean initialize) {
		if (initialize) {
			plotSystemComposite = new Composite(parentComp, SWT.NONE);
			plotSystemComposite.setLayout(new FillLayout());
			createPlottingSystem(plotSystemComposite);
		} else {
			// custom axes can be initialised when plot is empty
			clearPlot(!GuiPlotMode.EMPTY.equals(getPreviousMode()));
		}
		if (plotMode.equals(GuiPlotMode.ONED)) {
			plottingSystem.setPlotType(PlotType.XY);
			plotUI = new Plotting1DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {
			plottingSystem.setPlotType(PlotType.XY_STACKED_3D);
			plotUI = new Plotting1DStackUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.TWOD)) {
			plottingSystem.setPlotType(PlotType.IMAGE);
			plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SURF2D)) {
			plottingSystem.setPlotType(PlotType.SURFACE);
			plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
//			plottingSystem.setPlotType(PlotType.SCATTER2D);
			plotUI = new PlottingScatter2DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.SCATTER3D)) {
			plottingSystem.setPlotType(PlotType.XY_SCATTER_3D);
			plotUI = new PlottingScatter3DUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.MULTI2D)) {
			plottingSystem.setPlotType(PlotType.MULTI_IMAGE);
			plotUI = new Plotting2DMultiUI(plottingSystem);
		} else if (plotMode.equals(GuiPlotMode.EMPTY)) {
			resetAxes();
		}
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	/**
	 * Update the Plot Mode
	 * @param plotMode
	 * @param async
	 */
	public void updatePlotMode(final GuiPlotMode plotMode, boolean async) {
		doBlock();
		DisplayUtils.runInDisplayThread(async, parentComp, new Runnable() {
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
							changePlotMode(plotMode, false);
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

	@Override
	public void update(Object theObserved, Object changeCode) {

	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	public Object getAdapter(final Class<?> clazz) {
		return plottingSystem != null ? plottingSystem.getAdapter(clazz) : null;
	}

	public void dispose() {
		PlotWindowManager.getPrivateManager().unregisterPlotWindow(this);
		if (plotUI != null) {
			plotUI.deactivate(false);
			plotUI.dispose();
		}
		try {
			if (plottingSystem != null){//&& !plottingSystem.isDisposed()) {
				plottingSystem.removeRegionListener(getRoiManager());
				plottingSystem.dispose();
			}
		} catch (Exception ne) {
			logger.debug("Cannot clean up plotter!", ne);
		}
		deleteIObservers();
		plotUI = null;
		System.gc();
	}

	public void setFocus() {
		parentComp.setFocus();
	}
}

class SimpleLock {
	boolean state = false;

	boolean isLocked() {
		return state;
	}

	void lock() {
		state = true;
	}

	void unlock() {
		state = false;
	}
}
