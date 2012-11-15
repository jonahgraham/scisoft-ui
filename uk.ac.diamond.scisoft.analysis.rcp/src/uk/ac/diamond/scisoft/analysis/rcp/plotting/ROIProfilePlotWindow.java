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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.roi.RROITableInfo;
import org.dawb.common.ui.plot.tool.IProfileToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolPageFactory;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DuplicatePlotAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.InjectPyDevConsoleHandler;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROIList;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIPair;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.BoxLineType;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROIList;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROIList;

/**
 * PlotWindow equivalent with two side plots which display boxline profiles of a Rectangular ROI on the main plot
 * TODO temporary replicate of PlotWindow class: make it lighter and not a copy like it is currently (too many dependencies on PlotView to modify it)
 */
public class ROIProfilePlotWindow implements IObserver, IObservable, IPlotWindow, IROIListener{
	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	static private Logger logger = LoggerFactory.getLogger(ROIProfilePlotWindow.class);

	private IPlotUI plotUI = null;
	private boolean isUpdatePlot = false;
	private Composite parentComp;
	private IWorkbenchPage page = null;
	private IActionBars bars;
	private String name;

	private AbstractPlottingSystem plottingSystem;
	private IProfileToolPage sideProfile1;
	private IProfileToolPage sideProfile2;

	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	private IGuiInfoManager manager = null;
	private IUpdateNotificationListener notifyListener = null;
	private DataBean myBeanMemory;

	private Composite plotSystemComposite;
	private SashForm sashForm;
	private SashForm sashForm2;
	private SashForm sashForm3;

	protected List<ROIPair<String, ROIBase>> roiPairList = new ArrayList<ROIPair<String, ROIBase>>();
	protected ROIPair<String, ROIBase> currentRoiPair;
	protected ROIPair<String, ROIBase> previousRoiPair;

	private CommandContributionItem duplicateWindowCCI;
	private CommandContributionItem openPyDevConsoleCCI;
	private CommandContributionItem updateDefaultPlotCCI;
	private CommandContributionItem getPlotBeanCCI;
	private RROITableInfo mainROIMetadata;
	private RROITableInfo xaxisMetadataVertical;
	private RROITableInfo xaxisMetadataHorizontal;
	
	/**
	 * Obtain the IPlotWindowManager for the running Eclipse.
	 * 
	 * @return singleton instance of IPlotWindowManager
	 */
	public static IPlotWindowManager getManager() {
		// get the private manager for use only within the framework and
		// "upcast" it to IPlotWindowManager
		return PlotWindowManager.getPrivateManager();
	}

	/**
	 * default constructor
	 */
	public ROIProfilePlotWindow(){
		
	}

	/**
	 * Constructor used in children class ROIProfilePlotWindow
	 * @param manager
	 * @param notifyListener
	 * @param bars
	 * @param page
	 * @param name
	 */
	public ROIProfilePlotWindow(final Composite parent, IGuiInfoManager manager, IUpdateNotificationListener notifyListener, IActionBars bars,
			IWorkbenchPage page, String name) {
		this.manager = manager;
		this.notifyListener = notifyListener;
		this.parentComp = parent;
		this.page = page;
		this.bars = bars;
		this.name = name;
		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
	}

	public ROIProfilePlotWindow(Composite parent, GuiPlotMode plotMode, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, plotMode, null, null, bars, page, name);
	}

	public ROIProfilePlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, IWorkbenchPage page, String name) {

		this.manager = manager;
		this.notifyListener = notifyListener;
		this.parentComp = parent;
		this.page = page;
		this.bars = bars;
		this.name = name;

		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);

		if (plotMode == null)
			plotMode = GuiPlotMode.ONED;

		createMultiPlottingSystem();
		
		// Setting up
		if (plotMode.equals(GuiPlotMode.ONED)) {
			setupPlotting1D();
		} else if (plotMode.equals(GuiPlotMode.TWOD)) {
			setupPlotting2D();
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
			setupScatterPlotting2D();
		}

		PlotWindowManager.getPrivateManager().registerPlotWindow(this);
	}

	/**
	 * Create a plotting system layout with a main plotting system and two side plot profiles
	 */
	private void createMultiPlottingSystem(){
		parentComp.setLayout(new FillLayout());
		plotSystemComposite = new Composite(parentComp, SWT.NONE);
		plotSystemComposite.setLayout(new GridLayout(1, true));
		sashForm = new SashForm(plotSystemComposite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
		sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm3 = new SashForm(sashForm, SWT.VERTICAL);
		sashForm3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.NONE);
			
			plottingSystem.createPlotPart(sashForm2, name, bars, PlotType.XY, (IViewPart)manager);
			plottingSystem.repaint();
			
			sideProfile1 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
			sideProfile1.setLineType(BoxLineType.HORIZONTAL_TYPE);
			sideProfile1.setToolSystem(plottingSystem);
			sideProfile1.setPlottingSystem(plottingSystem);
			sideProfile1.setTitle(name+"_profile1");
			sideProfile1.setPart((IViewPart)manager);
			sideProfile1.setToolId(String.valueOf(sideProfile1.hashCode()));
			sideProfile1.createControl(sashForm2);
			sideProfile1.activate();
			
			sideProfile2 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
			sideProfile2.setLineType(BoxLineType.VERTICAL_TYPE);
			sideProfile2.setToolSystem(plottingSystem);
			sideProfile2.setPlottingSystem(plottingSystem);
			sideProfile2.setTitle(name+"_profile2");
			sideProfile2.setPart((IViewPart)manager);
			sideProfile2.setToolId(String.valueOf(sideProfile2.hashCode()));
			sideProfile2.createControl(sashForm3);
			sideProfile2.activate();

			//start metadata
			final ScrolledComposite scrollComposite = new ScrolledComposite(sashForm3, SWT.H_SCROLL | SWT.V_SCROLL);
			final Composite contentComposite = new Composite(scrollComposite, SWT.FILL);
			contentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
			contentComposite.setLayout(new GridLayout(1, false));
			
			Label metadataLabel = new Label(contentComposite, SWT.NONE);
			metadataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			metadataLabel.setAlignment(SWT.CENTER);
			metadataLabel.setText("ROI MetaData");
			
			mainROIMetadata = new RROITableInfo(contentComposite, "Rectangular ROI", plottingSystem, false);
			
			xaxisMetadataHorizontal = new RROITableInfo(contentComposite, "Vertical", 
					(AbstractPlottingSystem)sideProfile2.getToolPlottingSystem(), true);
			xaxisMetadataVertical = new RROITableInfo(contentComposite, "Horizontal", 
					(AbstractPlottingSystem)sideProfile1.getToolPlottingSystem(), true);
			
			scrollComposite.setContent(contentComposite);
			scrollComposite.setExpandHorizontal(true);
			scrollComposite.setExpandVertical(true);
			scrollComposite.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					Rectangle r = scrollComposite.getClientArea();
					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
				}
			});
			//end metadata
			
			sashForm.setWeights(new int[]{1, 1});
			this.regionListener = getRegionListener();
			this.plottingSystem.addRegionListener(this.regionListener);
			
		} catch (Exception e) {
			logger.error("Cannot locate any Abstract plotting System!", e);
		}
	}

	/**
	 * Return current page.
	 * 
	 * @return current page
	 */
	@Override
	public IWorkbenchPage getPage() {
		return page;
	}

	/**
	 * @return plot UI
	 */
	public IPlotUI getPlotUI() {
		return plotUI;
	}

	/**
	 * Return the name of the Window
	 * 
	 * @return name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Process a plot with data packed in bean - remember to update plot mode first if you do not know the current mode
	 * or if it is to change
	 * 
	 * @param dbPlot
	 */
	public void processPlotUpdate(final DataBean dbPlot) {
		// check to see what type of plot this is and set the plotMode to the correct one
		if (dbPlot.getGuiPlotMode() != null) {
			if(parentComp.isDisposed()){
				//this can be caused by the same plot view shown on 2 difference perspectives.
				throw new IllegalStateException("parentComp is already disposed");
			}
			if (parentComp.getDisplay().getThread() != Thread.currentThread())
				updatePlotMode(dbPlot.getGuiPlotMode(), true);
			else
				updatePlotMode(dbPlot.getGuiPlotMode(), false);
		}
		// there may be some gui information in the databean, if so this also needs to be updated
		if (dbPlot.getGuiParameters() != null) {
			processGUIUpdate(dbPlot.getGuiParameters());
		}

		try {
			doBlock();
			// Now plot the data as standard
			plotUI.processPlotUpdate(dbPlot, isUpdatePlot);
			myBeanMemory = dbPlot;
		} finally {
			undoBlock();
		}
	}

	/**
	 * Create the PlotView duplicating actions
	 */
	private void addDuplicateAction(){
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
	 * create the scripting actions
	 */
	private void addScriptingAction(){
		
		if (openPyDevConsoleCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, InjectPyDevConsoleHandler.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Open New Plot Scripting";
			ccip.icon = AnalysisRCPActivator.getImageDescriptor("icons/application_osx_terminal.png");
			Map<String, String> params = new HashMap<String, String>();
			params.put(InjectPyDevConsoleHandler.CREATE_NEW_CONSOLE_PARAM, Boolean.TRUE.toString());
			params.put(InjectPyDevConsoleHandler.VIEW_NAME_PARAM, getName());
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
			params.put(InjectPyDevConsoleHandler.VIEW_NAME_PARAM, getName());
			ccip.parameters = params;
			updateDefaultPlotCCI = new CommandContributionItem(ccip);
		}

		if (getPlotBeanCCI == null) {
			CommandContributionItemParameter ccip = new CommandContributionItemParameter(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow(), null, InjectPyDevConsoleHandler.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);
			ccip.label = "Get Plot Bean in Plot Scripting";
			Map<String, String> params = new HashMap<String, String>();
			params.put(InjectPyDevConsoleHandler.INJECT_COMMANDS_PARAM, "bean=dnp.plot.getbean('" + getName() + "')");
			ccip.parameters = params;
			getPlotBeanCCI = new CommandContributionItem(ccip);
		}
		bars.getMenuManager().add(openPyDevConsoleCCI);
		bars.getMenuManager().add(updateDefaultPlotCCI);
		bars.getMenuManager().add(getPlotBeanCCI);
	}

	//Abstract plotting System
	private void setupPlotting1D() {
		plotUI = new Plotting1DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		setupAxes();
	}

	//Abstract plotting System
	private void setupPlotting2D() {
		plotUI = new Plotting2DUI(this, plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		setupAxes();
	}

	// AbstractPlottingSystem
	private void setupAxes(){
		// set the profiles axes
		Collection<ITrace> traces = plottingSystem.getTraces();

		Iterator<ITrace> it = traces.iterator();
		while (it.hasNext()) {
			ITrace iTrace = it.next();
			if(iTrace instanceof IImageTrace){
				IImageTrace image = (IImageTrace)iTrace;

				List<AbstractDataset> axes = image.getAxes();

				sideProfile1.setAxes(axes);
				sideProfile2.setAxes(axes);
			}
		}
	}

	//Abstract plotting System
	private void setupScatterPlotting2D() {
		plotUI = new PlottingScatter2DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
	}

	/**
	 * @param plotMode
	 */
	public void updatePlotMode(GuiPlotMode plotMode) {
		if (plotMode.equals(GuiPlotMode.ONED)) 
			setupPlotting1D();
		else if (plotMode.equals(GuiPlotMode.TWOD)) 
			setupPlotting2D();
		else if (plotMode.equals(GuiPlotMode.SCATTER2D)) 
			setupScatterPlotting2D();
		else if (plotMode.equals(GuiPlotMode.EMPTY))
			clearPlot();
	}

	public void clearPlot() {
		if (plottingSystem != null) {
			plottingSystem.clearRegions();
			plottingSystem.reset();
			plottingSystem.repaint();
		}
	}

	private void updatePlotModeAsync(GuiPlotMode plotMode) {
		if (plotMode.equals(GuiPlotMode.ONED)){
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupPlotting1D();
					} finally {
						undoBlock();
					}
				}
			});
		} else if (plotMode.equals(GuiPlotMode.TWOD)) {
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupPlotting2D();
					} finally {
						undoBlock();
					}
				}
			});
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D)){
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupScatterPlotting2D();
					} finally {
						undoBlock();
					}
				}
			});
		}else if (plotMode.equals(GuiPlotMode.EMPTY)) {
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						clearPlot();
					} finally {
						undoBlock();
					}
				}
			});
		}
	}

	//not used
	public PlottingMode getPlottingSystemMode(){
		final Collection<ITrace> traces = plottingSystem.getTraces();
		if (traces==null) return PlottingMode.EMPTY;
		for (ITrace iTrace : traces) {
			if (iTrace instanceof ILineTrace) return PlottingMode.ONED;
			if (iTrace instanceof IImageTrace) return PlottingMode.TWOD;
		}
		return PlottingMode.EMPTY;
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

	public void updatePlotMode(GuiPlotMode plotMode, boolean async) {
		if (plotMode != null) {
			if (async)
				updatePlotModeAsync(plotMode);
			else
				updatePlotMode(plotMode);
		}
	}

	public boolean isUpdatePlot() {
		return isUpdatePlot;
	}

	public void setUpdatePlot(boolean isUpdatePlot) {
		this.isUpdatePlot = isUpdatePlot;
	}

	public void processGUIUpdate(GuiBean bean) {
		isUpdatePlot = false;
		if (bean.containsKey(GuiParameters.PLOTMODE)) {
			if (parentComp.getDisplay().getThread() != Thread.currentThread())
				updatePlotMode(bean, true);
			else
				updatePlotMode(bean, false);
		}

		if (bean.containsKey(GuiParameters.PLOTOPERATION)) {
			String opStr = (String) bean.get(GuiParameters.PLOTOPERATION);
			if (opStr.equals("UPDATE")) {
				isUpdatePlot = true;
			}
		}

		if (bean.containsKey(GuiParameters.ROIDATA) || bean.containsKey(GuiParameters.ROIDATALIST)) {
			plotUI.processGUIUpdate(bean);
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof HistogramView) {
			HistogramUpdate update = (HistogramUpdate) changeCode;

			if (plotUI instanceof Plot2DUI) {
				Plot2DUI plot2Dui = (Plot2DUI) plotUI;
				plot2Dui.getSidePlotView().sendHistogramUpdate(update);
			}
		}
	}

	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return plottingSystem;
		}
		return null;
	}

	public void dispose() {
		PlotWindowManager.getPrivateManager().unregisterPlotWindow(this);
		if (plotUI != null) {
			plotUI.deactivate(false);
			plotUI.dispose();
		}
		try {
			if (plottingSystem != null && !plottingSystem.isDisposed()) {
				plottingSystem.removeRegionListener(regionListener);
				plottingSystem.dispose();
			}
			if(sideProfile1 != null && !sideProfile1.isDisposed()){
				sideProfile1.dispose();
			}
			if(sideProfile2 != null && !sideProfile2.isDisposed()){
				sideProfile2.dispose();
			}
			mainROIMetadata.dispose();
			xaxisMetadataVertical.dispose();
			xaxisMetadataHorizontal.dispose();
		} catch (Exception ne) {
			logger.debug("Cannot clean up plotter!", ne);
		}
		deleteIObservers();
		plotUI = null;
		System.gc();
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

	public void notifyUpdateFinished() {
		if (notifyListener != null)
			notifyListener.updateProcessed();
	}

	public DataBean getDataBean() {
		return myBeanMemory;
	}

	SimpleLock simpleLock = new SimpleLock();

	private void doBlock() {
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

	private void undoBlock() {
		synchronized (simpleLock) {
			logger.debug("undoBlock " + Thread.currentThread().getId());
			simpleLock.unlock();
			simpleLock.notifyAll();
		}
	}

	// Make the PlotWindow a RegionListener (new plotting)
	protected IRegionListener regionListener;
	protected Map<String,Collection<ITrace>> registeredTraces;

	protected IRegionListener getRegionListener(){
		return new IRegionListener.Stub() {
			@Override
			public void regionRemoved(RegionEvent evt) {
				IRegion region = evt.getRegion();
				if (region!=null) {
					region.removeROIListener(ROIProfilePlotWindow.this);
					Object obj = evt.getSource();
					//if we delete current ROI
					if(currentRoiPair.getName().equals(obj.toString())){
						currentRoiPair = previousRoiPair;
						if (roiPairList!=null) {
							for (ROIPair<String, ROIBase> roiPair : roiPairList) {
								if(previousRoiPair.getName().equals(roiPair.getName())){
									roiPairList.remove(roiPair);
									break;
								}
							}
							if(roiPairList.size()>0)
								previousRoiPair = roiPairList.get(0);
							else
								previousRoiPair = null;
						} else {
							previousRoiPair = null;
						}
					}
					//if we delete the previous ROI
					else if(previousRoiPair.getName().equals(obj.toString())){
						for (ROIPair<String, ROIBase> roiPair : roiPairList) {
							if(previousRoiPair.getName().equals(roiPair.getName())){
								roiPairList.remove(roiPair);
								break;
							}
						}
						if(roiPairList.size()>0)
							previousRoiPair = roiPairList.get(0);
						else
							previousRoiPair = null;
					}
					//if we delete a ROI which is in the ROIlist and which is not the current nor the previous
					else {
						for (ROIPair<String, ROIBase> roiPair : roiPairList) {
							if(roiPair.getName().equals(obj.toString())){
								roiPairList.remove(roiPair);
								break;
							}
						}
					}
					if(currentRoiPair!=null)
						updateGuiBean(currentRoiPair.getRoi());
					else
						updateGuiBean(null);
					clearTraces(evt.getRegion());
				}
			}
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion()!=null) {
					ROIBase roi = evt.getRegion().getROI();
					if(roi!=null){
						currentRoiPair = new ROIPair<String, ROIBase>(evt.getSource().toString(), roi);
						if(roiPairList.size()>0){
							//Remove the current ROI from ROI List and replace it by previous one
							for (ROIPair<String, ROIBase> roiPair : roiPairList) {
								if(roiPair.getName().equals(currentRoiPair.getName())){
									roiPairList.remove(roiPair);
									roiPairList.add(new ROIPair<String, ROIBase>(previousRoiPair.getName(), previousRoiPair.getRoi()));
								//	break;
								}
							}
						}
						
					}
					updateGuiBean(roi);
				}
			}
			@Override
			public void regionCreated(RegionEvent evt) {
				if (evt.getRegion()!=null) {
					evt.getRegion().addROIListener(ROIProfilePlotWindow.this);
					IRegion region = evt.getRegion();
					ROIBase roi = region.getROI();
					if(roi!=null){
						updateGuiBean(roi);
					}
					
				}
			}
		};
	}

	protected void clearTraces(final IRegion region) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered!=null) for (ITrace iTrace : registered) {
			plottingSystem.removeTrace(iTrace);
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		ROIBase roi = evt.getROI();
		if(roi!=null){
			String id = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getId();
			if(id.startsWith(PlotView.ID)){
			
				ROIPair<String, ROIBase> evtPair = new ROIPair<String, ROIBase>(evt.getSource().toString(), roi);
				if(currentRoiPair!=null && !evtPair.getName().equals(currentRoiPair.getName()))
					previousRoiPair = currentRoiPair;
				currentRoiPair = evtPair;
			
				if(previousRoiPair!=null && !roiPairList.contains(previousRoiPair))
					roiPairList.add(new ROIPair<String, ROIBase>(previousRoiPair.getName(), previousRoiPair.getRoi()));
				if (roiPairList.size()>0){
					//Remove the current ROI from ROI List and replace it by previous one
					for (ROIPair<String, ROIBase> roiPair : roiPairList) {
						if(roiPair.getName().equals(currentRoiPair.getName())){
							roiPairList.remove(roiPair);
							break;
						}
					}
				}
				updateGuiBean(roi);
			}
		}
	}

	protected void updateGuiBean(ROIBase roib){
		manager.removeGUIInfo(GuiParameters.ROIDATA);
		manager.putGUIInfo(GuiParameters.ROIDATA, roib);
		manager.removeGUIInfo(GuiParameters.ROIDATALIST);
		if(roib instanceof RectangularROI)
			manager.putGUIInfo(GuiParameters.ROIDATALIST, createNewRROIList());
		if(roib instanceof LinearROI)
			manager.putGUIInfo(GuiParameters.ROIDATALIST, createNewLROIList());
		if(roib instanceof SectorROI)
			manager.putGUIInfo(GuiParameters.ROIDATALIST, createNewSROIList());
	}

	public LinearROIList createNewLROIList() {
		LinearROIList list = new LinearROIList();
		if (roiPairList != null) {
			for (ROIPair<String, ROIBase> roiPair: roiPairList) {
				if(roiPair.getRoi() instanceof LinearROI){
					list.add((LinearROI) roiPair.getRoi());
				}
			}
			if(roiPairList.size()==0)
				return null;
		}
		return list;
	}

	public RectangularROIList createNewRROIList() {
		RectangularROIList list = new RectangularROIList();
		if (roiPairList != null) {
			for (ROIPair<String, ROIBase> roiPair: roiPairList) {
				if(roiPair.getRoi() instanceof RectangularROI){
					list.add((RectangularROI) roiPair.getRoi());
				}
			}
			if(roiPairList.size()==0)
				return null;
		}
		return list;
	}

	public SectorROIList createNewSROIList() {
		SectorROIList list = new SectorROIList();
		if (roiPairList != null) {
			for (ROIPair<String, ROIBase> roiPair: roiPairList) {
				if(roiPair.getRoi() instanceof SectorROI){
					list.add((SectorROI) roiPair.getRoi());
				}
			}
			if(roiPairList.size()==0)
				return null;
		}
		return list;
	}
}
