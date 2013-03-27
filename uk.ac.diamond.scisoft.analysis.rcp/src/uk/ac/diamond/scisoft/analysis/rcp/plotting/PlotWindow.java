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

import gda.observable.IObserver;

import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.util.DisplayUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.core.AxisMode;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
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
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 * Actual PlotWindow that can be used inside a View- or EditorPart
 */
@SuppressWarnings("deprecation")
public class PlotWindow extends AbstractPlotWindow {
	public static final String RPC_SERVICE_NAME = "PlotWindowManager";
	public static final String RMI_SERVICE_NAME = "RMIPlotWindowManager";

	static private Logger logger = LoggerFactory.getLogger(PlotWindow.class);

	private DataSetPlotter mainPlotter;

	private AbstractPlottingSystem plottingSystem;

	private Composite plotSystemComposite;
	private Composite mainPlotterComposite;
	private Label txtPos;
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

	public PlotWindow(Composite parent, GuiPlotMode plotMode, IActionBars bars, IWorkbenchPage page, String name) {
		this(parent, plotMode, null, null, bars, page, name);
	}

	public PlotWindow(final Composite parent, GuiPlotMode plotMode, IGuiInfoManager manager,
			IUpdateNotificationListener notifyListener, IActionBars bars, IWorkbenchPage page, String name) {
		super(parent, manager, notifyListener, bars, page, name);

		if (plotMode == null)
			plotMode = GuiPlotMode.ONED;

		// this needs to be started in 1D as later mode changes will not work as plot UIs are not setup
		if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
			createDatasetPlotter(PlottingMode.ONED);

		if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM) {
			createPlottingSystem();
			cleanUpDatasetPlotter();
		}
		// Setting up
		if (plotMode.equals(GuiPlotMode.ONED)) {
			if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
				setup1D();
			else
				setupPlotting1D();
		} else if (plotMode.equals(GuiPlotMode.ONED_THREED)) {
			setupMulti1DPlot();
		} else if (plotMode.equals(GuiPlotMode.TWOD)) {
			if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
				setup2D();
			else
				setupPlotting2D();
		} else if (plotMode.equals(GuiPlotMode.SURF2D)) {
			setup2DSurface();
		} else if (plotMode.equals(GuiPlotMode.SCATTER2D)) {
			if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
				setupScatter2DPlot();
			else
				setupScatterPlotting2D();
		} else if (plotMode.equals(GuiPlotMode.SCATTER3D)) {
			setupScatter3DPlot();
		} else if (plotMode.equals(GuiPlotMode.MULTI2D)) {
			setupMulti2D();
		}

		parentAddControlListener();

		PlotWindowManager.getPrivateManager().registerPlotWindow(this);
	}

	private void parentAddControlListener() {
		// for some reason, this window does not get repainted
		// when a perspective is switched and the view is resized
		parentComp.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				if (e.widget.equals(parentComp)) {
					parentComp.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (mainPlotter != null && !mainPlotter.isDisposed())
								mainPlotter.refresh(false);
						}
					});
				}
			}

			@Override
			public void controlMoved(ControlEvent e) {
			}
		});
	}

	private void createDatasetPlotter(PlottingMode mode) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parentComp.setLayout(layout);

		txtPos = new Label(parentComp, SWT.LEFT);
		{
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			txtPos.setLayoutData(gridData);
		}
		Composite plotArea = new Composite(parentComp, SWT.NONE);
		plotArea.setLayout(new FillLayout());
		{
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.verticalAlignment = SWT.FILL;
			plotArea.setLayoutData(gridData);
		}
		
		mainPlotterComposite = new Composite(plotArea, SWT.NONE);
		mainPlotterComposite.setLayout(new FillLayout());
		mainPlotter = new DataSetPlotter(mode, mainPlotterComposite, true);
		mainPlotter.setAxisModes(AxisMode.LINEAR, AxisMode.LINEAR, AxisMode.LINEAR);
		mainPlotter.setXAxisLabel("X-Axis");
		mainPlotter.setYAxisLabel("Y-Axis");
		mainPlotter.setZAxisLabel("Z-Axis");

	}

	private void createPlottingSystem() {
		parentComp.setLayout(new FillLayout());
		plotSystemComposite = new Composite(parentComp, SWT.NONE);
		plotSystemComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotSystemComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.NONE);

			plottingSystem.createPlotPart(plotSystemComposite, getName(), bars, PlotType.XY, (IViewPart) getGuiManager());
			plottingSystem.repaint();

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
	public void processPlotUpdate(DataBean dbPlot) {
		// check to see what type of plot this is and set the plotMode to the correct one
		if (dbPlot.getGuiPlotMode() != null) {
			if (parentComp.isDisposed()) {
				// this can be caused by the same plot view shown on 2 difference perspectives.
				throw new IllegalStateException("parentComp is already disposed");
			}

			internalUpdatePlotMode(dbPlot.getGuiPlotMode(), true);
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

	private void cleanUpFromOldMode(final boolean leaveSidePlotOpen) {
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
	private void cleanUp(GuiPlotMode mode) {
		if (mode.equals(GuiPlotMode.ONED) || mode.equals(GuiPlotMode.TWOD) || mode.equals(GuiPlotMode.SCATTER2D)) {
			cleanUpDatasetPlotter();
			if (plottingSystem == null || plottingSystem.isDisposed())
				createPlottingSystem();
		} else if (mode.equals(GuiPlotMode.ONED_THREED)) {
			cleanUpPlottingSystem();
			if (mainPlotter == null || mainPlotter.isDisposed())
				createDatasetPlotter(PlottingMode.ONED_THREED);
			cleanUpFromOldMode(true);
		} else if (mode.equals(GuiPlotMode.SURF2D)) {
			cleanUpPlottingSystem();
			if (mainPlotter == null || mainPlotter.isDisposed())
				createDatasetPlotter(PlottingMode.SURF2D);
			cleanUpFromOldMode(true);
		} else if (mode.equals(GuiPlotMode.SCATTER3D)) {
			cleanUpPlottingSystem();
			if (mainPlotter == null || mainPlotter.isDisposed())
				createDatasetPlotter(PlottingMode.SCATTER3D);
			cleanUpFromOldMode(true);
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
	private void cleanUpDatasetPlotter() {
		if (mainPlotter != null && !mainPlotter.isDisposed()) {
			bars.getToolBarManager().removeAll();
			bars.getMenuManager().removeAll();
			mainPlotter.cleanUp();
			mainPlotterComposite.dispose();

			if(getPreviousMode()==GuiPlotMode.SURF2D){
				EclipseUtils.closeView(DataWindowView.ID);
			}
		}
	}

	/**
	 * Cleaning of the plotting system and its composite before the setting up of a datasetPlotter
	 */
	private void cleanUpPlottingSystem() {
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

	// Datasetplotter
	private void setup1D() {
		mainPlotter.setMode(PlottingMode.ONED);
		plotUI = new Plot1DUIComplete(this, getGuiManager(), bars, parentComp, getPage(), getName());
		((Plot1DUIComplete)plotUI).addPlotActionEventListener(new PlotActionEventListener(){

			@Override
			public void plotActionPerformed(final PlotActionEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						double x = event.getPosition()[0];
						double y = event.getPosition()[1];
						txtPos.setText(String.format("X:%.7g Y:%.7g", x, y));
					}
				});
			}});		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.ONED);
	}

	// Abstract plotting System
	private void setupPlotting1D() {
		plotUI = new Plotting1DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		updateGuiBeanPlotMode(GuiPlotMode.ONED);
	}

	// Datasetplotter
	private void setup2D() {
		mainPlotter.setMode(PlottingMode.TWOD);
		plotUI = new Plot2DUI(this, mainPlotter, getGuiManager(), parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.TWOD);
	}

	// Abstract plotting System
	private void setupPlotting2D() {
		plotUI = new Plotting2DUI(getRoiManager(), plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		updateGuiBeanPlotMode(GuiPlotMode.TWOD);
	}

	private void setupMulti2D() {
		mainPlotter.setMode(PlottingMode.MULTI2D);
		plotUI = new Plot2DMultiUI(this, mainPlotter, getGuiManager(), parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.MULTI2D);
	}

	private void setup2DSurface() {
		mainPlotter.useWindow(true);
		mainPlotter.setMode(PlottingMode.SURF2D);
		plotUI = new PlotSurf3DUI(this, mainPlotter, parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.SURF2D);
	}

	private void setupMulti1DPlot() {
		mainPlotter.setMode(PlottingMode.ONED_THREED);
		plotUI = new Plot1DStackUI(this, bars, mainPlotter, parentComp, getPage());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.ONED_THREED);
	}

	private void setupScatter2DPlot() {
		mainPlotter.setMode(PlottingMode.SCATTER2D);
		plotUI = new PlotScatter2DUI(this, bars, mainPlotter, parentComp, getPage(), getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.SCATTER2D);
	}

	// Abstract plotting System
	private void setupScatterPlotting2D() {
		plotUI = new PlottingScatter2DUI(plottingSystem);
		addScriptingAction();
		addDuplicateAction();
		addClearAction();
		updateGuiBeanPlotMode(GuiPlotMode.SCATTER2D);
	}

	private void setupScatter3DPlot() {
		mainPlotter.setMode(PlottingMode.SCATTER3D);
		plotUI = new PlotScatter3DUI(this, mainPlotter, parentComp, getPage(), bars, getName());
		addCommonActions(mainPlotter);
		bars.updateActionBars();
		updateGuiBeanPlotMode(GuiPlotMode.SCATTER3D);
	}

	/**
	 * @param plotMode
	 */
	@Override
	public void updatePlotMode(GuiPlotMode plotMode) {
		internalUpdatePlotMode(plotMode, false);
	}

	@Override
	public void clearPlot() {
		if (mainPlotter != null && !mainPlotter.isDisposed()) {
			mainPlotter.emptyPlot();
			mainPlotter.refresh(true);
		}
		if (plottingSystem != null) {
			plottingSystem.clearRegions();
			plottingSystem.reset();
			plottingSystem.repaint();
		}
	}

	private void internalUpdatePlotMode(final GuiPlotMode plotMode, boolean async) {
		doBlock();
		DisplayUtils.runInDisplayThread(async, parentComp, new Runnable() {
			@Override
			public void run() {
				try {
					if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM) {
						PlottingMode oldMode = mainPlotter.getMode();
						if (plotMode == GuiPlotMode.ONED && oldMode != PlottingMode.ONED) {
							cleanUpFromOldMode(true);
							setup1D();
						} else if (plotMode == GuiPlotMode.ONED_THREED && oldMode != PlottingMode.ONED_THREED) {
							cleanUpFromOldMode(true);
							setupMulti1DPlot();
						} else if (plotMode == GuiPlotMode.TWOD && oldMode != PlottingMode.TWOD) {
							cleanUpFromOldMode(true);
							setup2D();
						} else if (plotMode == GuiPlotMode.SURF2D && oldMode != PlottingMode.SURF2D) {
							cleanUpFromOldMode(true);
							setup2DSurface();
						} else if (plotMode == GuiPlotMode.SCATTER2D && oldMode != PlottingMode.SCATTER2D) {
							cleanUpFromOldMode(true);
							setupScatter2DPlot();
						} else if (plotMode == GuiPlotMode.SCATTER3D && oldMode != PlottingMode.SCATTER3D) {
							cleanUpFromOldMode(true);
							setupScatter3DPlot();
						} else if (plotMode == GuiPlotMode.MULTI2D && oldMode != PlottingMode.MULTI2D) {
							cleanUpFromOldMode(true);
							setupMulti2D();
						} else if (plotMode == GuiPlotMode.EMPTY && oldMode != PlottingMode.EMPTY) {
							clearPlot();
						}
					}
					if (getDefaultPlottingSystemChoice() == PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM) {
						GuiPlotMode oldMode = getPreviousMode();
						if (plotMode != GuiPlotMode.EMPTY) {
							cleanUp(plotMode);
							if (plotMode == GuiPlotMode.ONED && oldMode != GuiPlotMode.ONED) {
								setupPlotting1D();
								setPreviousMode(GuiPlotMode.ONED);
							} else if (plotMode == GuiPlotMode.TWOD && oldMode != GuiPlotMode.TWOD) {
								setupPlotting2D();
								setPreviousMode(GuiPlotMode.TWOD);
							} else if (plotMode == GuiPlotMode.SCATTER2D && oldMode != GuiPlotMode.SCATTER2D) {
								setupScatterPlotting2D();
								setPreviousMode(GuiPlotMode.SCATTER2D);
							} else if (plotMode == GuiPlotMode.ONED_THREED && oldMode != GuiPlotMode.ONED_THREED) {
								setupMulti1DPlot();
								setPreviousMode(GuiPlotMode.ONED_THREED);
							} else if (plotMode == GuiPlotMode.SURF2D && oldMode != GuiPlotMode.SURF2D) {
								setup2DSurface();
								setPreviousMode(GuiPlotMode.SURF2D);
							} else if (plotMode == GuiPlotMode.SCATTER3D && oldMode != GuiPlotMode.SCATTER3D) {
								setupScatter3DPlot();
								setPreviousMode(GuiPlotMode.SCATTER3D);
							} else if (plotMode == GuiPlotMode.MULTI2D && oldMode != GuiPlotMode.MULTI2D) {
								setupMulti2D();
								setPreviousMode(GuiPlotMode.MULTI2D);
							}
						} else if (oldMode != GuiPlotMode.EMPTY) {
							clearPlot();
							setPreviousMode(GuiPlotMode.EMPTY);
						}
					}
				} finally {
					undoBlock();
				}
			}
		});
	}

	@Override
	public void updatePlotModeAsync(GuiPlotMode plotMode) {
		internalUpdatePlotMode(plotMode, true);
	}

	@Override
	public void processGUIUpdate(GuiBean bean) {
		setUpdatePlot(false);
		if (bean.containsKey(GuiParameters.PLOTMODE)) {
			if (parentComp.getDisplay().getThread() != Thread.currentThread())
				updatePlotMode(bean, true);
			else
				updatePlotMode(bean, false);
		}
		
		if (bean.containsKey(GuiParameters.AXIS_OPERATION)) {
			AxisOperation operation = (AxisOperation) bean.get(GuiParameters.AXIS_OPERATION);
            processAxisOperation(operation);
		}

		if (bean.containsKey(GuiParameters.TITLE) && mainPlotter != null 
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

		if (bean.containsKey(GuiParameters.ROIDATA) || bean.containsKey(GuiParameters.ROIDATALIST)) {
			plotUI.processGUIUpdate(bean);
		}
	}

	private void processAxisOperation(AxisOperation operation) {
		
        if (operation.getOperationType().equals(AxisOperation.CREATE)) {
        	plottingSystem.createAxis(operation.getTitle(), operation.isYAxis(), operation.getSide());
        	
        } else if (operation.getOperationType().equals(AxisOperation.DELETE)) {
        	final List<IAxis> axes = plottingSystem.getAxes();
        	for (IAxis iAxis : axes) {
				if (operation.getTitle().equals(iAxis.getTitle())) plottingSystem.removeAxis(iAxis);
			}
        	
        } else if (operation.getOperationType().equals(AxisOperation.ACTIVEX)) {
        	final List<IAxis> axes = plottingSystem.getAxes();
        	for (IAxis iAxis : axes) {
        		if (iAxis.isYAxis()) continue;
				if (operation.getTitle().equals(iAxis.getTitle())) plottingSystem.setSelectedXAxis(iAxis);
			}
        	
        } else if (operation.getOperationType().equals(AxisOperation.ACTIVEY)) {
        	final List<IAxis> axes = plottingSystem.getAxes();
        	for (IAxis iAxis : axes) {
        		if (!iAxis.isYAxis()) continue;
				if (operation.getTitle().equals(iAxis.getTitle())) plottingSystem.setSelectedYAxis(iAxis);
			}
       	
        }
		
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

	public DataSetPlotter getMainPlotter() {
		return mainPlotter;
	}

	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
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

	private int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? preferenceStore
				.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) : preferenceStore
				.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}
}