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

import java.util.Collection;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.tool.IProfileToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolPageFactory;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.widgets.ROIWidget;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.BoxLineType;

/**
 * PlotWindow equivalent with two side plots which display boxline profiles of a Rectangular ROI on the main plot
 */
public class ROIProfilePlotWindow extends AbstractPlotWindow {
	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	static private Logger logger = LoggerFactory.getLogger(ROIProfilePlotWindow.class);

	private AbstractPlottingSystem plottingSystem;
	private IProfileToolPage sideProfile1;
	private IProfileToolPage sideProfile2;

	private Composite plotSystemComposite;
	private SashForm sashForm;
	private SashForm sashForm2;
	private SashForm sashForm3;

	//private RROITableInfo xaxisMetadataVertical;
	//private RROITableInfo xaxisMetadataHorizontal;
	private ROIWidget myROIWidget;
	private ROIWidget verticalProfileROIWidget;
	private ROIWidget horizontalProfileROIWidget;
	private AbstractPlottingSystem verticalProfilePlottingSystem;
	private AbstractPlottingSystem horizontalProfilePlottingSystem;
	
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

	public ROIProfilePlotWindow(Composite parent, GuiPlotMode plotMode, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, plotMode, null, null, bars, page, name);
	}

	public ROIProfilePlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, IWorkbenchPage page, String name) {
		super(parent, manager, notifyListener, bars, page, name);

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
			
			plottingSystem.createPlotPart(sashForm2, getName(), bars, PlotType.XY, (IViewPart)getGuiManager());
			plottingSystem.repaint();
			
			sideProfile1 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
			sideProfile1.setLineType(BoxLineType.HORIZONTAL_TYPE);
			sideProfile1.setToolSystem(plottingSystem);
			sideProfile1.setPlottingSystem(plottingSystem);
			sideProfile1.setTitle(getName()+"_profile1");
			sideProfile1.setPart((IViewPart)getGuiManager());
			sideProfile1.setToolId(String.valueOf(sideProfile1.hashCode()));
			sideProfile1.createControl(sashForm2);
			sideProfile1.activate();
			
			sideProfile2 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
			sideProfile2.setLineType(BoxLineType.VERTICAL_TYPE);
			sideProfile2.setToolSystem(plottingSystem);
			sideProfile2.setPlottingSystem(plottingSystem);
			sideProfile2.setTitle(getName()+"_profile2");
			sideProfile2.setPart((IViewPart)getGuiManager());
			sideProfile2.setToolId(String.valueOf(sideProfile2.hashCode()));
			sideProfile2.createControl(sashForm3);
			sideProfile2.activate();

			verticalProfilePlottingSystem = (AbstractPlottingSystem)sideProfile2.getToolPlottingSystem();
			horizontalProfilePlottingSystem = (AbstractPlottingSystem)sideProfile1.getToolPlottingSystem();

			//start metadata
			final ScrolledComposite scrollComposite = new ScrolledComposite(sashForm3, SWT.H_SCROLL | SWT.V_SCROLL);
			final Composite contentComposite = new Composite(scrollComposite, SWT.FILL);
			contentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
			contentComposite.setLayout(new GridLayout(1, false));
			
			ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					logger.trace("regionsExpander");
					Rectangle r = scrollComposite.getClientArea();
					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
					contentComposite.layout();
				}
			};
			
			Label metadataLabel = new Label(contentComposite, SWT.NONE);
			metadataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			metadataLabel.setAlignment(SWT.CENTER);
			metadataLabel.setText("ROI MetaData");

			//main
			ExpandableComposite mainRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			mainRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			mainRegionInfoExpander.setLayout(new GridLayout(1, false));
			mainRegionInfoExpander.setText("Main Region Of Interest");
			
			Composite mainRegionComposite = new Composite(mainRegionInfoExpander, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			mainRegionComposite.setLayout(new GridLayout(1, false));
			mainRegionComposite.setLayoutData(gridData);
			
			myROIWidget = new ROIWidget(mainRegionComposite, plottingSystem);
			myROIWidget.createWidget();
			myROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = myROIWidget.getROI();
					String regionName = myROIWidget.getRegionName();
					
					IRegion region = plottingSystem.getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});
			
//			mainROIMetadata = new RROITableInfo(mainRegionComposite, "Main ROI", plottingSystem, false);
			
			mainRegionInfoExpander.setClient(mainRegionComposite);
			mainRegionInfoExpander.addExpansionListener(expansionAdapter);
			mainRegionInfoExpander.setExpanded(false);
			
			//vertical
			ExpandableComposite verticalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			verticalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			verticalRegionInfoExpander.setLayout(new GridLayout(1, false));
			verticalRegionInfoExpander.setText("Vertical Profile Region Of Interest");
			
			Composite verticalProfileComposite = new Composite(verticalRegionInfoExpander, SWT.NONE);
			verticalProfileComposite.setLayout(new GridLayout(1, false));
			verticalProfileComposite.setLayoutData(gridData);

			verticalProfileROIWidget = new ROIWidget(verticalProfileComposite, verticalProfilePlottingSystem);
			verticalProfileROIWidget.setIsProfile(true);
			verticalProfileROIWidget.showSumMinMax(false);
			verticalProfileROIWidget.createWidget();
			verticalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = verticalProfileROIWidget.getROI();
					String regionName = verticalProfileROIWidget.getRegionName();
					
					IRegion region = verticalProfilePlottingSystem.getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});
			
			
//			xaxisMetadataVertical = new RROITableInfo(verticalProfileComposite, "Vertical Profile", 
//					(AbstractPlottingSystem)sideProfile2.getToolPlottingSystem(), true);
			
			verticalRegionInfoExpander.setClient(verticalProfileComposite);
			verticalRegionInfoExpander.addExpansionListener(expansionAdapter);
			verticalRegionInfoExpander.setExpanded(false);
			
			//horizontal
			ExpandableComposite horizontalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			horizontalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			horizontalRegionInfoExpander.setLayout(new GridLayout(1, false));
			horizontalRegionInfoExpander.setText("Horizontal Profile Region Of Interest");
			
			Composite horizontalProfileComposite = new Composite(horizontalRegionInfoExpander, SWT.NONE);
			horizontalProfileComposite.setLayout(new GridLayout(1, false));
			horizontalProfileComposite.setLayoutData(gridData);

			horizontalProfileROIWidget = new ROIWidget(horizontalProfileComposite, horizontalProfilePlottingSystem);
			horizontalProfileROIWidget.setIsProfile(true);
			horizontalProfileROIWidget.showSumMinMax(false);
			horizontalProfileROIWidget.createWidget();
			horizontalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = horizontalProfileROIWidget.getROI();
					String regionName = horizontalProfileROIWidget.getRegionName();
					
					IRegion region = horizontalProfilePlottingSystem.getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});

//			xaxisMetadataHorizontal = new RROITableInfo(horizontalProfileComposite, "Horizontal Profile", 
//					(AbstractPlottingSystem)sideProfile1.getToolPlottingSystem(), true);
//			
			horizontalRegionInfoExpander.setClient(horizontalProfileComposite);
			horizontalRegionInfoExpander.addExpansionListener(expansionAdapter);
			horizontalRegionInfoExpander.setExpanded(false);
			
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
			plottingSystem.addRegionListener(getRoiManager());
			
		} catch (Exception e) {
			logger.error("Cannot locate any Abstract plotting System!", e);
		}
	}

	/**
	 * @return plot UI
	 */
	public IPlotUI getPlotUI() {
		return plotUI;
	}

	/**
	 * Process a plot with data packed in bean - remember to update plot mode first if you do not know the current mode
	 * or if it is to change
	 * 
	 * @param dbPlot
	 */
	@Override
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
			plotUI.processPlotUpdate(dbPlot, isUpdatePlot());
			setDataBean(dbPlot);
		} finally {
			undoBlock();
		}
	}

	//Abstract plotting System
	private void setupPlotting1D() {
		plotUI = new Plotting1DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		updateGuiBeanPlotMode(GuiPlotMode.ONED);
	}

	//Abstract plotting System
	private void setupPlotting2D() {
		plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		updateGuiBeanPlotMode(GuiPlotMode.TWOD);
	}

	//Abstract plotting System
	private void setupScatterPlotting2D() {
		plotUI = new PlottingScatter2DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		updateGuiBeanPlotMode(GuiPlotMode.SCATTER2D);
	}

	/**
	 * @param plotMode
	 */
	@Override
	public void updatePlotMode(GuiPlotMode plotMode) {
		if (plotMode.equals(GuiPlotMode.ONED) && getPreviousMode() != GuiPlotMode.ONED){ 
			setupPlotting1D();
			setPreviousMode(GuiPlotMode.ONED);
		}  else if (plotMode.equals(GuiPlotMode.TWOD) && getPreviousMode() != GuiPlotMode.TWOD) {
			setupPlotting2D();
			setPreviousMode(GuiPlotMode.TWOD);
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D) && getPreviousMode() != GuiPlotMode.SCATTER2D) {
			setupScatterPlotting2D();
			setPreviousMode(GuiPlotMode.SCATTER2D);
		} else if (plotMode.equals(GuiPlotMode.EMPTY) && getPreviousMode() != GuiPlotMode.EMPTY) {
			clearPlot();
			setPreviousMode(GuiPlotMode.EMPTY);
		}
	}

	@Override
	public void clearPlot() {
		if (plottingSystem != null) {
			plottingSystem.clearRegions();
			plottingSystem.reset();
			plottingSystem.repaint();
		}
	}

	@Override
	public void updatePlotModeAsync(GuiPlotMode plotMode) {

		if (plotMode.equals(GuiPlotMode.ONED) && getPreviousMode() != GuiPlotMode.ONED){
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupPlotting1D();
						setPreviousMode(GuiPlotMode.ONED);
					} finally {
						undoBlock();
					}
				}
			});
		} else if (plotMode.equals(GuiPlotMode.TWOD) && getPreviousMode() != GuiPlotMode.TWOD) {
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupPlotting2D();
						setPreviousMode(GuiPlotMode.TWOD);
					} finally {
						undoBlock();
					}
				}
			});
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D) && getPreviousMode() != GuiPlotMode.SCATTER2D){
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						setupScatterPlotting2D();
						setPreviousMode(GuiPlotMode.SCATTER2D);
					} finally {
						undoBlock();
					}
				}
			});
		}else if (plotMode.equals(GuiPlotMode.EMPTY) && getPreviousMode() != GuiPlotMode.EMPTY) {
			doBlock();
			parentComp.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						clearPlot();
						setPreviousMode(GuiPlotMode.EMPTY);
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

	@Override
	public void processGUIUpdate(GuiBean bean) {

		if(parentComp != null && !parentComp.isDisposed()){
			setUpdatePlot(false);
			if (bean.containsKey(GuiParameters.PLOTMODE)) {
				
				if (parentComp.getDisplay().getThread() != Thread.currentThread())
					updatePlotMode(bean, true);
				else
					updatePlotMode(bean, false);
			}

			if (bean.containsKey(GuiParameters.PLOTOPERATION)) {
				String opStr = (String) bean.get(GuiParameters.PLOTOPERATION);
				if (opStr.equals("UPDATE")) {
					setUpdatePlot(true);
				}
			}

			if (bean.containsKey(GuiParameters.ROIDATA) || bean.containsKey(GuiParameters.ROIDATALIST)) {
				plotUI.processGUIUpdate(bean);
			}
		}
	}

	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public void dispose() {
		PlotWindowManager.getPrivateManager().unregisterPlotWindow(this);
		if (plotUI != null) {
			plotUI.deactivate(false);
			plotUI.dispose();
		}
		try {
			plottingSystem.removeRegionListener(getRoiManager());
			plottingSystem.dispose();
			sideProfile1.dispose();
			sideProfile2.dispose();
			myROIWidget.dispose();
			verticalProfileROIWidget.dispose();
			horizontalProfileROIWidget.dispose();
			//xaxisMetadataVertical.dispose();
			//xaxisMetadataHorizontal.dispose();
		} catch (Exception ne) {
			logger.debug("Cannot clean up plotter!", ne);
		}
		deleteIObservers();
		plotUI = null;
		System.gc();
	}

	@Override
	public void update(Object source, Object arg) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Required if you want to make tools work with Abstract Plotting System.
	 */
	@Override
	public Object getAdapter(final Class<?> clazz) {
		if (clazz == IToolPageSystem.class) {
			return plottingSystem;
		}
		return null;
	}
}
