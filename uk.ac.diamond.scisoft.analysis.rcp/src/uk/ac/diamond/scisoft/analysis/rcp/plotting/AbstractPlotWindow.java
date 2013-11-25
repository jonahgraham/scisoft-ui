/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.DisplayUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
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
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.ClearPlottingSystemAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DuplicatePlotAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.InjectPyDevConsoleHandler;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView;
import uk.ac.diamond.scisoft.analysis.rcp.views.ExamplePlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 * Abstract Class used to implement PlotWindows that implement IObserver, IObservable
 */
public abstract class AbstractPlotWindow implements IPlotWindow, IObserver, IObservable {

	static private Logger logger = LoggerFactory.getLogger(AbstractPlotWindow.class);

	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	protected IPlottingSystem plottingSystem;
	protected DataSetPlotter mainPlotter;

	protected Composite parentComp;
	protected Composite plotSystemComposite;
	protected Composite mainPlotterComposite;
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

	protected IPlotUI plotUI = null;
	private boolean isUpdatePlot = false;

	private GuiPlotMode previousMode;

	private Action saveGraphAction;
	private Action copyGraphAction;
	private Action printGraphAction;
	private CommandContributionItem duplicateWindowCCI;
	private CommandContributionItem openPyDevConsoleCCI;
	private CommandContributionItem updateDefaultPlotCCI;
	private CommandContributionItem getPlotBeanCCI;
	private CommandContributionItem clearPlotCCI;

	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

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
	 * Create a DatasetPlotter (used for old plotting)
	 * @param mode
	 */
	protected void createDatasetPlotter(PlottingMode mode) {
		mainPlotterComposite = new Composite(parentComp, SWT.NONE);
		mainPlotterComposite.setLayout(new FillLayout());
		mainPlotter = new DataSetPlotter(mode, mainPlotterComposite, true);
		mainPlotter.setAxisModes(AxisMode.LINEAR, AxisMode.LINEAR, AxisMode.LINEAR);
		mainPlotter.setXAxisLabel("X-Axis");
		mainPlotter.setYAxisLabel("Y-Axis");
		mainPlotter.setZAxisLabel("Z-Axis");
	}

	/**
	 * @return plot UI
	 */
	public IPlotUI getPlotUI() {
		return plotUI;
	}

	/**
	 * Used for old plotting
	 * @param leaveSidePlotOpen
	 */
	protected void cleanUpFromOldMode(final boolean leaveSidePlotOpen) {
		setUpdatePlot(false);
		mainPlotter.unregisterUI(plotUI);
		if (plotUI != null) {
			plotUI.deleteIObservers();
			plotUI.deactivate(leaveSidePlotOpen);
			removePreviousActions();
		}
	}

	/**
	 * Cleaning up the plot view according to the current plot mode
	 * 
	 * @param mode
	 */
	protected void cleanUp(GuiPlotMode mode) {
		if (mode.equals(GuiPlotMode.ONED) || mode.equals(GuiPlotMode.TWOD) 
				|| mode.equals(GuiPlotMode.SCATTER2D)
				|| mode.equals(GuiPlotMode.SURF2D)
				|| mode.equals(GuiPlotMode.ONED_THREED)
				|| mode.equals(GuiPlotMode.SCATTER3D)) {
			cleanUpDatasetPlotter();
			if (plottingSystem == null || plottingSystem.isDisposed()) {
				plotSystemComposite = new Composite(parentComp, SWT.NONE);
				plotSystemComposite.setLayout(new FillLayout());
				createPlottingSystem(plotSystemComposite);
			}
		} else if (mode.equals(GuiPlotMode.MULTI2D)) {
			cleanUpPlottingSystem();
			if (mainPlotter == null || mainPlotter.isDisposed())
				createDatasetPlotter(PlottingMode.MULTI2D);
			cleanUpFromOldMode(true);
		}
		parentComp.layout();
	}

	/**
	 * Cleaning of the DatasetPlotter and its composite before the setting up of a Plotting System
	 */
	protected void cleanUpDatasetPlotter() {
		if (mainPlotter != null && !mainPlotter.isDisposed()) {
			bars.getToolBarManager().removeAll();
			bars.getMenuManager().removeAll();
			mainPlotter.cleanUp();
			mainPlotterComposite.dispose();

			if (GuiPlotMode.SURF2D.equals(getPlotMode())) {
				EclipseUtils.closeView(DataWindowView.ID);
			}
		}
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
	 * Method to add the DataSetPlotter actions
	 * @param mainPlotter
	 */
	protected void addCommonActions(final DataSetPlotter mainPlotter) {

		if (saveGraphAction == null) {
			saveGraphAction = new Action() {		
				// Cache file name otherwise they have to keep
				// choosing the folder.
				private String filename;
				
				@Override
				public void run() {
					
					FileDialog dialog = new FileDialog (parentComp.getShell(), SWT.SAVE);
					
					String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
					if (filename!=null) {
						dialog.setFilterPath((new File(filename)).getParent());
					} else {
						String filterPath = "/";
						String platform = SWT.getPlatform();
						if (platform.equals("win32") || platform.equals("wpf")) {
							filterPath = "c:\\";
						}
						dialog.setFilterPath (filterPath);
					}
					dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
					dialog.setFilterExtensions (filterExtensions);
					filename = dialog.open();
					if (filename == null)
						return;

					mainPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
				}
			};
			saveGraphAction.setText(saveButtonText);
			saveGraphAction.setToolTipText(saveToolTipText);
			saveGraphAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));
		}
		
		if (copyGraphAction == null) {
			copyGraphAction = new Action() {
				@Override
				public void run() {
					mainPlotter.copyGraph();
				}
			};
			copyGraphAction.setText(copyButtonText);
			copyGraphAction.setToolTipText(copyToolTipText);
			copyGraphAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));
		}
		
		if (printGraphAction == null) {
			printGraphAction = new Action() {
				@Override
				public void run() {
					mainPlotter.printGraph();
				}
			};
			printGraphAction.setText(printButtonText);
			printGraphAction.setToolTipText(printToolTipText);
			printGraphAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));
		}

		if (bars.getMenuManager().getItems().length > 0)
			bars.getMenuManager().add(new Separator());
		bars.getMenuManager().add(saveGraphAction);
		bars.getMenuManager().add(copyGraphAction);
		bars.getMenuManager().add(printGraphAction);
		bars.getMenuManager().add(new Separator("scripting.group"));
		addScriptingAction();
		bars.getMenuManager().add(new Separator("duplicate.group"));
		addDuplicateAction();
		
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

		if (bean.containsKey(GuiParameters.TITLE) && mainPlotter != null && !mainPlotter.isDisposed()
				&& mainPlotterComposite != null && !mainPlotterComposite.isDisposed()) {
			final String titleStr = (String) bean.get(GuiParameters.TITLE);
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					doBlock();
					try {
						mainPlotter.setTitle(titleStr);
					} finally {
						undoBlock();
					}
					mainPlotter.refresh(true);
				}
			});
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
		int choice = getDefaultPlottingSystemChoice();
		if (choice == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM) {
			if (initialize) {
				// this needs to be started in 1D as later mode changes will not work as plot UIs are not setup
				createDatasetPlotter(PlottingMode.ONED);
			} else {
				cleanUpFromOldMode(true);
			}
	
			// Setting up
			if (plotMode.equals(GuiPlotMode.ONED)) {
				setup1D();
			} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {
				setupMulti1DPlot();
			} else if (plotMode.equals(GuiPlotMode.TWOD)) {
				setup2D();
			} else if (plotMode.equals(GuiPlotMode.SURF2D)) {
				setup2DSurfaceOldPlotting();
			} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
				setupScatter2DPlot();
			} else if (plotMode.equals(GuiPlotMode.SCATTER3D)) {
				setupScatter3DPlot();
			} else if (plotMode.equals(GuiPlotMode.MULTI2D)) {
				setupMulti2D();
			} else if (plotMode.equals(GuiPlotMode.EMPTY)) {
				clearPlot(true);
			}
		} else if (choice == PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM) {
			if (initialize) {
				plotSystemComposite = new Composite(parentComp, SWT.NONE);
				plotSystemComposite.setLayout(new FillLayout());
				createPlottingSystem(plotSystemComposite);
				cleanUpDatasetPlotter();
			} else {
				cleanUp(plotMode);
				// custom axes can be initialised when plot is empty
				clearPlot(!GuiPlotMode.EMPTY.equals(getPreviousMode()));
			}

			if (plotMode.equals(GuiPlotMode.ONED)) {
				setupPlotting1D();
			} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {
				setupMulti1DPlotting();
			} else if (plotMode.equals(GuiPlotMode.TWOD)) {
				setupPlotting2D();
			} else if (plotMode.equals(GuiPlotMode.SURF2D)) {
				setup2DSurfaceNewPlotting();
			} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
				setupScatterPlotting2D();
			} else if (plotMode.equals(GuiPlotMode.SCATTER3D)) {
				setupScatter3DNewPlotting();
			} else if (plotMode.equals(GuiPlotMode.MULTI2D)) {
				setupMulti2D();
			} else if (plotMode.equals(GuiPlotMode.EMPTY)) {
				resetAxes();
			}
		}
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
					if (plotMode.equals(GuiPlotMode.RESETAXES)) {
						resetAxes();
					} else {
						GuiPlotMode oldMode = getPreviousMode();
						if (oldMode == null || !plotMode.equals(oldMode)) {
							changePlotMode(plotMode, false);
							setPreviousMode(plotMode);
						}
					}
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
		if (mainPlotter != null && !mainPlotter.isDisposed()) {
			mainPlotter.emptyPlot();
			mainPlotter.refresh(true);
		}
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

	public DataSetPlotter getMainPlotter() {
		return mainPlotter;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof HistogramView) {
			HistogramUpdate update = (HistogramUpdate) changeCode;
			mainPlotter.applyColourCast(update);

			if (!mainPlotter.isDisposed())
				mainPlotter.refresh(false);
			if (plotUI instanceof Plot2DUI) {
				Plot2DUI plot2Dui = (Plot2DUI) plotUI;
				plot2Dui.getSidePlotView().sendHistogramUpdate(update);
			}
		}
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	public Object getAdapter(final Class<?> clazz) {
		return plottingSystem != null ? plottingSystem.getAdapter(clazz) : null;
	}

	// Datasetplotter
	protected void setup1D() {
		mainPlotter.setMode(PlottingMode.ONED);
		plotUI = new Plot1DUIComplete(this, getGuiManager(), bars, parentComp, getPage(), getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	// Abstract plotting System
	protected void setupPlotting1D() {
		plottingSystem.setPlotType(PlotType.XY);
		plotUI = new Plotting1DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	// Dataset plotter
	protected void setup2D() {
		mainPlotter.setMode(PlottingMode.TWOD);
		plotUI = new Plot2DUI(this, mainPlotter, getGuiManager(), parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	// Abstract plotting System
	protected void setupPlotting2D() {
		plottingSystem.setPlotType(PlotType.IMAGE);
		plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	protected void setupMulti2D() {
		mainPlotter.setMode(PlottingMode.MULTI2D);
		plotUI = new Plot2DMultiUI(this, mainPlotter, getGuiManager(), parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	protected void setup2DSurfaceNewPlotting() {
		plottingSystem.setPlotType(PlotType.SURFACE);
		plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	protected void setup2DSurfaceOldPlotting() {
		mainPlotter.setMode(PlottingMode.SURF2D);
		plotUI = new PlotSurf3DUI(this, mainPlotter, parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	protected void setupMulti1DPlot() {
		mainPlotter.setMode(PlottingMode.ONED_THREED);
		plotUI = new Plot1DStackUI(this, bars, mainPlotter, parentComp, getPage());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	// Abstract plotting System
	protected void setupMulti1DPlotting() {
		plottingSystem.setPlotType(PlotType.XY_STACKED_3D);
		plotUI = new Plotting1DStackUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
	}

	protected void setupScatter2DPlot() {
		mainPlotter.setMode(PlottingMode.SCATTER2D);
		plotUI = new PlotScatter2DUI(this, bars, mainPlotter, parentComp, getPage(), getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	// Abstract plotting System
	protected void setupScatterPlotting2D() {
//		plottingSystem.setPlotType(PlotType.SCATTER2D); TODO create a new plot type
		plotUI = new PlottingScatter2DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
//		setVisibleByPlotType(plottingSystem.getPlotType());
	}

	protected void setupScatter3DPlot() {
		mainPlotter.setMode(PlottingMode.SCATTER3D);
		plotUI = new PlotScatter3DUI(this, mainPlotter, parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
	}

	// Abstract plotting System
	protected void setupScatter3DNewPlotting() {
		plottingSystem.setPlotType(PlotType.XY_SCATTER_3D);
		plotUI = new PlottingScatter3DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
	}

	public void dispose() {
		PlotWindowManager.getPrivateManager().unregisterPlotWindow(this);
		if (plotUI != null) {
			plotUI.deactivate(false);
			plotUI.dispose();
		}
		try {
			if (mainPlotter != null) {
				mainPlotter.cleanUp();
			}
			if (plottingSystem != null){//&& !plottingSystem.isDisposed()) {
				plottingSystem.removeRegionListener(getRoiManager());
				plottingSystem.dispose();
			}
		} catch (Exception ne) {
			logger.debug("Cannot clean up plotter!", ne);
		}
		deleteIObservers();
		mainPlotter = null;
		plotUI = null;
		System.gc();
	}

	public void notifyHistogramChange(HistogramDataUpdate histoUpdate) {
		if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM) {
			Iterator<IObserver> iter = getObservers().iterator();
			while (iter.hasNext()) {
				IObserver listener = iter.next();
				listener.update(this, histoUpdate);
			}
		}
	}

	protected int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? preferenceStore
				.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) : preferenceStore
				.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
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
