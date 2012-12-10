/*
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.ClearPlottingSystemAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DuplicatePlotAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.InjectPyDevConsoleHandler;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.PlotExportUtil;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;

/**
 * Abstract Class used to implement PlotWindows that implement IObserver, IObservable
 */
public abstract class AbstractPlotWindow implements IPlotWindow, IObserver, IObservable{
	static private Logger logger = LoggerFactory.getLogger(AbstractPlotWindow.class);

	protected Composite parentComp;
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
	 * COnstructor
	 * @param parent
	 * @param manager
	 * @param notifyListener
	 * @param bars
	 * @param page
	 * @param name
	 */
	public AbstractPlotWindow(final Composite parent, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, 
			IWorkbenchPage page, String name) {
		this.parentComp = parent;
		this.manager = manager;
		this.notifyListener = notifyListener;
		this.bars = bars;
		this.page = page;
		this.name = name;
		this.setRoiManager(new ROIManager(manager));
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
	protected IGuiInfoManager getGuiManager(){
		return manager;
	}

	/**
	 * Returns the notifyListener of the window
	 * @return notifyListener
	 */
	protected IUpdateNotificationListener getNotififyListener(){
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

	public List<IObserver> getObservers(){
		return observers;
	}

	public void notifyUpdateFinished() {
		if (notifyListener != null)
			notifyListener.updateProcessed();
	}

	/**
	 * Process a plot with data packed in bean - remember to update plot mode first if you do not know the current mode
	 * or if it is to change
	 * To be Override
	 * @param dbPlot
	 */
	public void processPlotUpdate(@SuppressWarnings("unused") final DataBean dbPlot) {
		
	}

	/**
	 * To be Override
	 * @param bean
	 */
	public void processGUIUpdate(@SuppressWarnings("unused") GuiBean bean) {

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
	 * To be override
	 * @param plotMode
	 */
	public void updatePlotMode(@SuppressWarnings("unused") GuiPlotMode plotMode) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * To be Override
	 * @param plotMode
	 */
	public void updatePlotModeAsync(@SuppressWarnings("unused") GuiPlotMode plotMode) {
		// TODO Auto-generated method stub
		
	}

	public void updatePlotMode(GuiPlotMode plotMode, boolean async) {
		if (plotMode != null) {
			if (async)
				updatePlotModeAsync(plotMode);
			else
				updatePlotMode(plotMode);
		}
	}

	public void updatePlotMode(GuiBean bean, boolean async) {
		if (bean != null) {
			if (bean.containsKey(GuiParameters.PLOTMODE)) { // bean does not necessarily have a plot mode (eg, it
															// contains ROIs only)
				GuiPlotMode plotMode = (GuiPlotMode) bean.get(GuiParameters.PLOTMODE);
				updatePlotMode(plotMode, async);
			}
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

	public void setRoiManager(ROIManager roiManager) {
		this.roiManager = roiManager;
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 * TODO To be overriden
	 */
	public Object getAdapter(@SuppressWarnings({ "unused", "rawtypes" }) Class clazz) {
		// TODO Auto-generated method stub
		return null;
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
