/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.scisoft.mappingexplorer.views.twod;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.axis.IPositionListener;
import org.dawnsci.plotting.api.axis.PositionEvent;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.MouseEvent;
import org.dawnsci.plotting.api.region.MouseListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.tool.IToolChangeListener;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.tool.ToolChangeEvent;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView2dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.histogram.HistogramSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.oned.IOneDSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection.PixelSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection.TwoDSelection;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

/**
 * Composite that contains the dataset plotter and represents 3D data with the plotter showing 2D image and a stepper
 * providing ability to traverse through the third dimension.
 * 
 * @author rsr31645
 */
public class TwoDDataSetPlotterContainingPage extends BaseViewPageComposite {
	private int xPoint;
	private int yPoint;

	private static final String REGION_X_STATIC = "X-Static";
	private static final String REGION_Y_STATIC = "Y-Static";
	private static final String HISTOGRAM_ALL_lbl = "Histogram All";
	private static final String REGION_Y_HAIR = "Y-Hair";
	private static final String REGION_X_HAIR = "X-Hair";
	private static final String PLOT_NAME_TWO_D_PLOT = "TwoDPlot";
	private static final String REGION_AREA_SELECTION = "AreaSelection";
	private static final String INTENSITY_lbl = "Value: ";
	private static final String Y_lbl = "y: ";
	private static final String X_lbl = "x: ";
	private static final String POSITION_lbl = "Pos";
	private static final String PIXEL_lbl = "Pixel";
	private static final String HISTOGRAM_lbl = "Histogram";
	private static final String NOTIFY_PIXEL_CHANGED_job_lbl = "Notify Pixel Changed";
	private static final String NOTIFY_AREA_SELECTED_CHANGED_job_lbl = "Notify Area Selected Changed";
	private static final String FLIP_AXIS_lbl = "Flip Axis";
	private static final String NOTIFY_DIMENSION_CHANGED = "Notify dimension changed";
	private Stepper thirdDimensionScaler;
	private Button btnFlipAxis;
	private static final Logger logger = LoggerFactory.getLogger(TwoDDataSetPlotterContainingPage.class);
	private Button rdDimension1;
	private Button rdDimension2;
	private Button rdDimension3;
	private IMappingView3dData mapping3DData;
	private IMappingView2dData mapping2DData;
	private String secondaryId;
	private IPlottingSystem plottingSystem;
	private Composite axisSelectionComposite;

	private IRegion areaRegion;
	private Label lblXValue;
	private Label lblYValue;
	private Label lblIntensityValue;
	private Button btnPixelSelection;
	private Button btnAreaSelection;

	private IRegion xHair;
	private IRegion yHair;

	private Button btnHistogramAll;

	public TwoDDataSetPlotterContainingPage(Page page, Composite parent, int style, String secondaryViewId) {
		super(parent, style);
		FormToolkit formToolkit = new FormToolkit(parent.getDisplay());
		this.secondaryId = secondaryViewId;
		GridLayout layout = new GridLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 2;
		this.setLayout(layout);
		this.setBackground(ColorConstants.white);
		axisSelectionComposite = formToolkit.createComposite(this);
		axisSelectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		axisSelectionComposite.setBackground(ColorConstants.white);
		axisSelectionComposite.setLayout(new GridLayout(4, true));

		rdDimension1 = formToolkit.createButton(axisSelectionComposite, "", SWT.RADIO);
		rdDimension1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension1.setBackground(ColorConstants.white);
		rdDimension1.addSelectionListener(rdDimensionSelectionListener);

		rdDimension2 = formToolkit.createButton(axisSelectionComposite, "", SWT.RADIO);
		rdDimension2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension2.setBackground(ColorConstants.white);
		rdDimension2.addSelectionListener(rdDimensionSelectionListener);

		rdDimension3 = formToolkit.createButton(axisSelectionComposite, "", SWT.RADIO);
		rdDimension3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension3.setBackground(ColorConstants.white);
		rdDimension3.addSelectionListener(rdDimensionSelectionListener);

		btnFlipAxis = formToolkit.createButton(axisSelectionComposite, "", SWT.TOGGLE | SWT.RIGHT);
		btnFlipAxis.setText(FLIP_AXIS_lbl);
		btnFlipAxis.setLayoutData(new GridData());
		btnFlipAxis.setBackground(ColorConstants.white);
		btnFlipAxis.addSelectionListener(flipBtnSelectionListener);

		Composite mainComposite = formToolkit.createComposite(this);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;

		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;

		mainComposite.setLayout(gl);

		Composite plotComposite = formToolkit.createComposite(mainComposite);
		//
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComposite, PLOT_NAME_TWO_D_PLOT, page.getSite().getActionBars(),
					PlotType.XY_STACKED, null);
			plottingSystem.addPositionListener(new IPositionListener() {

				@Override
				public void positionChanged(PositionEvent evt) {

					int xPoint = (int) plottingSystem.getSelectedXAxis().getPositionValue(evt.getxPos());
					int yPoint = (int) plottingSystem.getSelectedYAxis().getPositionValue(evt.getyPos());

					lblXValue.setText(Integer.toString(xPoint));
					lblYValue.setText(Integer.toString(yPoint));

					if (!plottingSystem.getTraces().isEmpty()) {
						ITrace trace = plottingSystem.getTraces().iterator().next();
						IDataset datasetFromTrace = trace.getData();
						double intensityValue = datasetFromTrace.getDouble(new int[] { yPoint, xPoint });

						lblIntensityValue.setText(Double.toString(intensityValue));

					}
				}
			});

			final IToolPageSystem system = (IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class);
			system.addToolChangeListener(new IToolChangeListener() {

				@Override
				public void toolChanged(ToolChangeEvent evt) {
					logger.warn("Tool changed, {}", evt.getNewPage().getToolId());
				}
			});

			plottingSystem.addRegionListener(new IRegionListener() {

				@Override
				public void regionsRemoved(RegionEvent evt) {
					logger.warn("Regions removed");
					areaRegionRemoved(evt);
				}

				private void areaRegionRemoved(RegionEvent evt) {
					if (REGION_AREA_SELECTION.equals(evt.getRegion().getName())) {
						areaRegion = null;
					}
				}

				@Override
				public void regionRemoved(RegionEvent evt) {
					areaRegionRemoved(evt);
				}

				@Override
				public void regionCreated(RegionEvent evt) {
					// logger.warn("Region created");
				}

				@Override
				public void regionAdded(RegionEvent evt) {
					// logger.warn("Region added");
				}

				@Override
				public void regionCancelled(RegionEvent evt) {
					// TODO Auto-generated method stub
					
				}
			});

			plottingSystem.addTraceListener(new ITraceListener.Stub() {
				@Override
				public void traceAdded(TraceEvent evt) {
					logger.warn("Trace Added : {}", evt.getSource());
				}
			});
			disablePlottingSystemActions(plottingSystem);

		} catch (RuntimeException ex) {
			logger.error("There is a problem with datasetPlotter.setMode()");
		} catch (Exception e) {
			logger.error("Problem creating plotting system", e);
		}

		//
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite infoComposite = formToolkit.createComposite(mainComposite);
		infoComposite.setBackground(ColorConstants.white);
		infoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gl2 = new GridLayout();
		gl2.marginWidth = 0;
		gl2.marginHeight = 0;
		gl2.verticalSpacing = 0;
		gl2.horizontalSpacing = 0;

		infoComposite.setLayout(gl2);

		Composite positionComposite = formToolkit.createComposite(infoComposite);
		positionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl4 = new GridLayout(7, true);
		gl4.marginWidth = 0;
		gl4.marginHeight = 0;
		gl4.verticalSpacing = 0;
		gl4.horizontalSpacing = 0;
		positionComposite.setLayout(gl4);

		formToolkit.createLabel(positionComposite, POSITION_lbl, SWT.LEFT).setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		formToolkit.createLabel(positionComposite, X_lbl, SWT.RIGHT).setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		lblXValue = formToolkit.createLabel(positionComposite, "", SWT.LEFT);
		lblXValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		formToolkit.createLabel(positionComposite, Y_lbl, SWT.RIGHT).setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		lblYValue = formToolkit.createLabel(positionComposite, "", SWT.LEFT);
		lblYValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		formToolkit.createLabel(positionComposite, INTENSITY_lbl, SWT.RIGHT).setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		lblIntensityValue = formToolkit.createLabel(positionComposite, "", SWT.LEFT);
		lblIntensityValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite cmpAddnlButtons = formToolkit.createComposite(infoComposite);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		cmpAddnlButtons.setLayoutData(layoutData);
		GridLayout gl3 = new GridLayout(3, true);
		gl3.marginWidth = 0;
		gl3.marginHeight = 0;
		gl3.verticalSpacing = 0;
		gl3.horizontalSpacing = 0;
		cmpAddnlButtons.setLayout(gl3);

		btnHistogramAll = formToolkit.createButton(cmpAddnlButtons, HISTOGRAM_ALL_lbl, SWT.PUSH);
		btnHistogramAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnHistogramAll.addSelectionListener(btnSelectionListener);

		btnAreaSelection = formToolkit.createButton(cmpAddnlButtons, HISTOGRAM_lbl, SWT.TOGGLE);
		btnAreaSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAreaSelection.addSelectionListener(btnSelectionListener);

		btnPixelSelection = formToolkit.createButton(cmpAddnlButtons, PIXEL_lbl, SWT.TOGGLE);
		btnPixelSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnPixelSelection.addSelectionListener(btnSelectionListener);

		Composite thirdDimensionComposite = formToolkit.createComposite(this);
		thirdDimensionComposite.setBackground(ColorConstants.white);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		thirdDimensionComposite.setLayoutData(gd);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		thirdDimensionComposite.setLayout(layout);

		thirdDimensionScaler = new Stepper(thirdDimensionComposite, SWT.None);
		thirdDimensionScaler.setNotifyWhenDragged(true);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 50;

		thirdDimensionScaler.setLayoutData(gd);
		thirdDimensionScaler.addStepperSelectionListener(stepperSelectionListener);
		createUpdatePlotJob();
	}

	private Update2DPlotJob updatePlotJob;

	private void createUpdatePlotJob() {
		if (updatePlotJob == null) {
			updatePlotJob = new Update2DPlotJob(getDisplay());
		}
	}

	private SelectionAdapter btnSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnAreaSelection)) {
				if (btnAreaSelection.getSelection()) {
					createAreaRegion();
					if (btnPixelSelection.getSelection()) {
						btnPixelSelection.setSelection(false);
						removeCrossHairRegion();
					}
				} else {
				}
			} else if (e.getSource().equals(btnHistogramAll)) {
				selectAllForHistogram();
				btnAreaSelection.setSelection(false);
			} else if (e.getSource().equals(btnPixelSelection)) {
				if (btnPixelSelection.getSelection()) {
					createCrossHairRegion();
				} else {
					removeCrossHairRegion();
				}
				if (btnAreaSelection.getSelection()) {
					btnAreaSelection.setSelection(false);
				}
			}
		}
	};

	private IRegion createStaticRegion(String nameStub, final IROI bounds, final Color snapShotColor,
			final RegionType regionType) throws Exception {

		final IRegion region = plottingSystem.createRegion(nameStub, regionType);
		region.setRegionColor(snapShotColor);
		plottingSystem.addRegion(region);
		region.setMobile(false);
		region.setUserRegion(false);
		region.setROI(bounds);
		return region;
	}

	private MouseListener xHairMouseListener = new MouseListener() {

		@Override
		public void mousePressed(MouseEvent me) {
			try {
				IRegion xStaticRegion = plottingSystem.getRegion(REGION_X_STATIC);
				if (xStaticRegion != null && xStaticRegion.getROI() != null) {
					plottingSystem.removeRegion(xStaticRegion);
				}

				xStaticRegion = createStaticRegion(REGION_X_STATIC, xHair.getROI(), ColorConstants.buttonDarkest,
						xHair.getRegionType());
			} catch (Exception e) {
				logger.error("Cannot create x static region", e);
			}
			try {
				IRegion yStaticRegion = plottingSystem.getRegion(REGION_Y_STATIC);
				if (yStaticRegion != null && yStaticRegion.getROI() != null) {
					plottingSystem.removeRegion(yStaticRegion);
				}
				yStaticRegion = createStaticRegion(REGION_Y_STATIC, yHair.getROI(), ColorConstants.buttonDarkest,
						yHair.getRegionType());
			} catch (Exception e) {
				logger.error("Cannot create y static region", e);
			}

			try {
				xPoint = (int) plottingSystem.getSelectedXAxis().getPositionValue(me.getX());
				yPoint = (int) plottingSystem.getSelectedYAxis().getPositionValue(me.getY());

				notifyPixelChanged();
			} catch (Exception e) {
				logger.error("Problem getting point in axis co-ordinates.", e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			// do nothing
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			// do nothing
		}
	};

	private void createCrossHairRegion() {
		if (xHair == null || plottingSystem.getRegion(REGION_X_HAIR) == null) {
			try {
				this.xHair = plottingSystem.createRegion(REGION_X_HAIR, IRegion.RegionType.XAXIS_LINE);
				xHair.setVisible(false);
				xHair.setTrackMouse(true);
				xHair.setRegionColor(ColorConstants.red);
				xHair.setUserRegion(false); // They cannot see preferences or change it!
				plottingSystem.addRegion(xHair);
				xHair.addMouseListener(xHairMouseListener);
			} catch (Exception e) {
				logger.error("TODO put description of error here", e);
			} finally {
			}
		}
		if (yHair == null || plottingSystem.getRegion(REGION_Y_HAIR) == null) {
			try {
				this.yHair = plottingSystem.createRegion(REGION_Y_HAIR, IRegion.RegionType.YAXIS_LINE);
				yHair.setVisible(false);
				yHair.setTrackMouse(true);
				yHair.setRegionColor(ColorConstants.red);
				yHair.setUserRegion(false); // They cannot see preferences or change it!
				plottingSystem.addRegion(yHair);
			} catch (Exception e) {
				logger.error("TODO put description of error here", e);
			} finally {
			}
		}
	}

	protected void removeCrossHairRegion() {
		if (xHair != null) {
			IRegion xRegion = plottingSystem.getRegion(REGION_X_HAIR);
			if (((IFigure) xHair).getParent() != null) {
				xHair.removeMouseListener(xHairMouseListener);
			}
			plottingSystem.removeRegion(xRegion);
		}
		if (yHair != null) {
			IRegion yRegion = plottingSystem.getRegion(REGION_Y_HAIR);
			plottingSystem.removeRegion(yRegion);
		}
	}

	private void createAreaRegion() {
		if (areaRegion == null || (areaRegion != null && areaRegion.getROI() == null)) {
			try {
				areaRegion = plottingSystem.createRegion(REGION_AREA_SELECTION, RegionType.BOX);
				areaRegion.addROIListener(new IROIListener() {

					@Override
					public void roiSelected(ROIEvent evt) {
						// logger.info("ROI selected");
					}

					@Override
					public void roiDragged(ROIEvent evt) {
						notifyAreaSelectedChanged();
					}

					@Override
					public void roiChanged(ROIEvent evt) {
						notifyAreaSelectedChanged();
					}
				});
			} catch (Exception ex) {
				logger.error("Problem creating area region", ex);
			}

		} else {
			logger.warn("Area region, point x:{}", areaRegion.getROI().getPointX());
			logger.warn("Area region, point y:{}", areaRegion.getROI().getPointY());
		}
	}

	private SelectionListener rdDimensionSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				boolean changed = false;
				int steps = 0;
				String stepperLabel = null;
				double[] data = null;
				if (e.getSource().equals(rdDimension1) && rdDimension1.getSelection()) {
					steps = mapping3DData.getDataSet().getShape()[0];
					stepperLabel = mapping3DData.getDimension1Label();
					data = mapping3DData.getDimension1Values();
					changed = true;
				} else if (e.getSource().equals(rdDimension2) && rdDimension2.getSelection()) {
					steps = mapping3DData.getDataSet().getShape()[1];
					stepperLabel = mapping3DData.getDimension2Label();
					data = mapping3DData.getDimension2Values();
					changed = true;
				} else if (e.getSource().equals(rdDimension3) && rdDimension3.getSelection()) {
					steps = mapping3DData.getDataSet().getShape()[2];
					stepperLabel = mapping3DData.getDimension3Label();
					data = mapping3DData.getDimension3Values();
					changed = true;
				}

				if (changed) {
					thirdDimensionScaler.setSteps(steps, data);
					thirdDimensionScaler.setText(stepperLabel);
					removeCrossHairRegion();
					removeStaticCrossHairRegions();
					removeAreaRegion();

					fireUpdatePlot();
					notifyPixelChanged();
				}

				if (plottingSystem != null) {
					removeAreaRegion();
					if (btnAreaSelection.getSelection()) {
						btnAreaSelection.setSelection(false);
					}
				}

			} catch (Exception e1) {
				logger.error("Error updating plot", e1);
			}
		}

	};

	private void fireUpdatePlot() throws Exception {
		thirdDimensionScaler.setSelection(0);
		updatePlot();
	}

	private SelectionListener flipBtnSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				fireUpdatePlot();
			} catch (Exception e1) {
				logger.error("Problem flipping image {}", e1);
			}
		}

	};

	private String getSelectedDimensionLabel(int sel) {
		switch (sel) {
		case 1:
			return mapping3DData.getDimension1Label();
		case 2:
			return mapping3DData.getDimension2Label();
		default:
			return mapping3DData.getDimension3Label();
		}
	}

	private int getSelectedRdBtnVal() {
		if (rdDimension1.getSelection()) {
			return 1;
		}
		if (rdDimension2.getSelection()) {
			return 2;
		}
		return 3;
	}

	private AxisSelection getSelectedDimension() {
		if (mapping3DData != null) {
			return new AxisSelection(getSelectedDimensionLabel(getSelectedRdBtnVal()), getSelectedRdBtnVal());
		}
		return null;
	}

	protected void fireNotifyDimensionChanged() {
		UIJob job = new UIJob(getDisplay(), NOTIFY_DIMENSION_CHANGED) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				List<ISelection> selElements = new ArrayList<ISelection>();
				if (getSelectedDimension() != null) {
					TwoDSelection selection = new TwoDSelection(secondaryId, getSelectedDimension(),
							getPixelSelection(), btnFlipAxis.getSelection());
					selElements.add(selection);
					StructuredSelection sel = new StructuredSelection(selElements);
					notifyListeners(sel);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private IStepperSelectionListener stepperSelectionListener = new IStepperSelectionListener() {

		@Override
		public void stepperChanged(StepperChangedEvent e) {
			try {
				updatePlot();
			} catch (Exception e1) {
				logger.error("Problem flipping image {}", e1);
			}
		}
	};
	private AbstractDataset currentSlice;

	@Override
	public void dispose() {
		if (!thirdDimensionScaler.isDisposed()) {
			thirdDimensionScaler.removeStepperSelectionListener(stepperSelectionListener);
		}
		super.dispose();
	}

	@Override
	public void initialPlot() throws Exception {
		if (mapping3DData == null && mapping2DData == null) {
			throw new IllegalArgumentException("No data to plot");
		}
		if (mapping3DData != null) {
			rdDimension1.setText(mapping3DData.getDimension1Label());
			rdDimension1.setToolTipText(mapping3DData.getDimension1Label());

			rdDimension2.setText(mapping3DData.getDimension2Label());
			rdDimension2.setToolTipText(mapping3DData.getDimension2Label());

			rdDimension3.setText(mapping3DData.getDimension3Label());
			rdDimension3.setToolTipText(mapping3DData.getDimension3Label());

			// initialise we first dimension set as the one into the page as
			// this matches the order in which data is normally written to the file
			// the first dimension is normally the scan point
			rdDimension1.setSelection(true);
			int steps = mapping3DData.getDataSet().getShape()[0];
			thirdDimensionScaler.setSelection(0);
			thirdDimensionScaler.setSteps(steps, mapping3DData.getDimension1Values());
			thirdDimensionScaler.setText(mapping3DData.getDimension1Label());
			thirdDimensionScaler.layout();
		}
		updatePlot();
	}

	private class Update2DPlotJob extends Job {

		private final Display jobDisplay;
		private int scalerValue;
		private boolean radio1;
		private boolean radio2;
		private boolean radio3;
		private boolean isFlip;

		public Update2DPlotJob(Display jobDisplay) {
			super("Updating 2D Plot");
			this.jobDisplay = jobDisplay;
		}

		public void setDimensionScaler(int scalerValue) {
			this.scalerValue = scalerValue;
		}

		public void setRadioSelection(boolean radio1, boolean radio2, boolean radio3) {
			this.radio1 = radio1;
			this.radio2 = radio2;
			this.radio3 = radio3;
		}

		public void setIsFlip(boolean isFlip) {
			this.isFlip = isFlip;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (mapping3DData == null && mapping2DData == null) {
				throw new IllegalArgumentException("Mapping View Data not available");
			}

			currentSlice = null;

			AxisValues xAxisValues = null;
			AxisValues yAxisValues = null;
			String xAxisLabel = null;
			String yAxisLabel = null;

			if (mapping3DData != null) {
				ILazyDataset dataset = mapping3DData.getDataSet();
				int[] shape = dataset.getShape();

				if (radio1) {
					currentSlice = (AbstractDataset) dataset.getSlice(new Slice(scalerValue, scalerValue + 1),
							new Slice(null), new Slice(null));
					currentSlice.setShape(shape[1], shape[2]);

					xAxisLabel = mapping3DData.getDimension3Label();
					yAxisLabel = mapping3DData.getDimension2Label();

					if (mapping3DData.getDimension3Values() != null) {
						xAxisValues = new AxisValues(mapping3DData.getDimension3Values());
					}
					if (mapping3DData.getDimension2Values() != null) {
						yAxisValues = new AxisValues(mapping3DData.getDimension2Values());
					}
				}
				if (radio2) {
					currentSlice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(scalerValue,
							scalerValue + 1), new Slice(null));
					currentSlice.setShape(shape[0], shape[2]);

					xAxisLabel = mapping3DData.getDimension3Label();
					yAxisLabel = mapping3DData.getDimension1Label();

					if (mapping3DData.getDimension3Values() != null) {
						xAxisValues = new AxisValues(mapping3DData.getDimension3Values());
					}
					if (mapping3DData.getDimension1Values() != null) {
						yAxisValues = new AxisValues(mapping3DData.getDimension1Values());
					}
				}
				if (radio3) {
					currentSlice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(null), new Slice(
							scalerValue, scalerValue + 1));
					currentSlice.setShape(shape[0], shape[1]);

					xAxisLabel = mapping3DData.getDimension2Label();
					yAxisLabel = mapping3DData.getDimension1Label();

					if (mapping3DData.getDimension2Values() != null) {
						xAxisValues = new AxisValues(mapping3DData.getDimension2Values());
					}
					if (mapping3DData.getDimension1Values() != null) {
						yAxisValues = new AxisValues(mapping3DData.getDimension1Values());
					}
				}
				if (isFlip) {
					String tmp = yAxisLabel;
					yAxisLabel = xAxisLabel;
					xAxisLabel = tmp;

					AxisValues tmpA = yAxisValues;
					yAxisValues = xAxisValues;
					xAxisValues = tmpA;

					if (currentSlice != null) {
						currentSlice = currentSlice.transpose();
					}
				}
			} else if (mapping2DData != null) {
				ILazyDataset dataset = mapping2DData.getDataSet();

				xAxisLabel = mapping2DData.getDimension2Label();
				yAxisLabel = mapping2DData.getDimension1Label();

				if (mapping2DData.getDimension2Values() != null) {
					xAxisValues = new AxisValues(mapping2DData.getDimension2Values());
				}

				if (mapping2DData.getDimension1Values() != null) {
					yAxisValues = new AxisValues(mapping2DData.getDimension1Values());
				}

				currentSlice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(null));
			}

			final String xLabel = xAxisLabel;
			final String yLabel = yAxisLabel;
			final List<IDataset> axisList = new ArrayList<IDataset>();
			if (!jobDisplay.isDisposed()) {
				jobDisplay.asyncExec(new Runnable() {

					@Override
					public void run() {
						if (currentSlice != null) {
							plottingSystem.updatePlot2D(currentSlice, axisList, new NullProgressMonitor());
						}
						plottingSystem.getSelectedXAxis().setTitle(xLabel);
						plottingSystem.getSelectedYAxis().setTitle(yLabel);
					}
				});
			}
			notifyAreaSelectedChanged();

			return Status.OK_STATUS;
		}
	}

	@Override
	public void updatePlot() throws Exception {
		updatePlotJob.cancel();
		updatePlotJob.setDimensionScaler(thirdDimensionScaler.getSelection());
		updatePlotJob.setIsFlip(btnFlipAxis.getSelection());
		updatePlotJob.setRadioSelection(rdDimension1.getSelection(), rdDimension2.getSelection(),
				rdDimension3.getSelection());
		updatePlotJob.schedule(100);
	}

	private void removeStaticCrossHairRegions() {
		IRegion xRegion = plottingSystem.getRegion(REGION_X_STATIC);

		if (xRegion != null) {
			plottingSystem.removeRegion(xRegion);
		}

		IRegion yRegion = plottingSystem.getRegion(REGION_Y_STATIC);
		if (yRegion != null) {
			plottingSystem.removeRegion(yRegion);
		}
		xPoint = 0;
		yPoint = 0;
	}

	@Override
	public ISelection getSelection() {
		List<ISelection> selections = new ArrayList<ISelection>();
		TwoDSelection twdSel = new TwoDSelection(secondaryId, getSelectedDimension(), getPixelSelection(),
				btnFlipAxis.getSelection());
		selections.add(twdSel);
		selections.add(getHistogramSelectionDataset());
		return new StructuredSelection(selections);
	}

	private Point[] getRectangleRegionRoi() {
		IROI roi = areaRegion.getROI();
		if (roi instanceof RectangularROI) {
			RectangularROI rectRoi = (RectangularROI) roi;
			double[] endPoint = rectRoi.getEndPoint();
			int[] intEndPoint = normalize(new int[] { (int) endPoint[0], (int) endPoint[1] });
			int[] normalisedEndPoint = normalize(intEndPoint);
			int[] startPoint = rectRoi.getIntPoint();
			int[] normalisedStartPoint = normalize(startPoint);

			return new Point[] { new Point(normalisedStartPoint[0], normalisedStartPoint[1]),
					new Point(normalisedEndPoint[0], normalisedEndPoint[1]) };
		}
		return null;
	}

	protected ISelection getHistogramSelectionDataset() {
		IDataset slice = null;
		if (areaRegion != null) {

			Point[] rc = getRectangleRegionRoi();
			if (rc != null) {
				slice = currentSlice.getSlice(new Slice(rc[0].y, rc[1].y), new Slice(rc[0].x, rc[1].x));
			} else {
				slice = currentSlice;
			}
			if (btnFlipAxis.getSelection()) {
				slice = ((AbstractDataset) slice).transpose();
			}
		} else {
			slice = currentSlice;
		}
		return new HistogramSelection(slice);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IOneDSelection) {
			IOneDSelection oneDSel = (IOneDSelection) selection;
			int pos1 = -1;
			int pos2 = -1;
			AxisSelection dim1 = oneDSel.getDimension1Selection();
			String xTitle = plottingSystem.getSelectedXAxis().getTitle();
			String yTitle = plottingSystem.getSelectedYAxis().getTitle();
			if (dim1.getLabel().equals(xTitle)) {
				pos1 = dim1.getDimension();
			} else if (dim1.getLabel().equals(yTitle)) {
				pos2 = dim1.getDimension();
			}

			AxisSelection dim2 = oneDSel.getDimension2Selection();
			if (dim2.getLabel().equals(xTitle)) {
				pos1 = dim2.getDimension();
			} else if (dim2.getLabel().equals(yTitle)) {
				pos2 = dim2.getDimension();
			}
			updateStaticRegion(pos1, pos2);
		}
	}

	private void updateStaticRegion(int pos1, int pos2) {
		IRegion xStaticRegion = plottingSystem.getRegion(REGION_X_STATIC);
		if (xStaticRegion != null) {
			xStaticRegion.setROI(new RectangularROI(pos1, 0, 1, 1, 0));// ;new int[] { pos1 }));
		} else {
			try {
				createStaticRegion(REGION_X_STATIC, new RectangularROI(pos1, 0, 1, 1, 0), ColorConstants.black,
						RegionType.XAXIS_LINE);
			} catch (Exception e) {
				logger.warn("Unable to create X static region for cross hair", e);
			}
		}

		IRegion yStaticRegion = plottingSystem.getRegion(REGION_Y_STATIC);
		if (yStaticRegion != null) {
			yStaticRegion.setROI(new RectangularROI(0, pos2, 1, 1, 0));
		} else {
			try {
				createStaticRegion(REGION_Y_STATIC, new RectangularROI(0, pos2, 1, 1, 0), ColorConstants.black,
						RegionType.YAXIS_LINE);
			} catch (Exception e) {
				logger.warn("Unable to create Y static region for cross hair", e);
			}
		}
		xPoint = pos1;
		yPoint = pos2;
	}

	private void notifyAreaSelectedChanged() {
		UIJob job = new UIJob(getDisplay(), NOTIFY_AREA_SELECTED_CHANGED_job_lbl) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				notifyListeners(getHistogramSelectionDataset());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void notifyPixelChanged() {
		UIJob job = new UIJob(getDisplay(), NOTIFY_PIXEL_CHANGED_job_lbl) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				TwoDSelection selection = new TwoDSelection(secondaryId, getSelectedDimension(), getPixelSelection(),
						btnFlipAxis.getSelection());

				notifyListeners(selection);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	public void setMappingViewData(IMappingView2dData mappingViewData) {
		if (mappingViewData instanceof IMappingView3dData) {
			mapping3DData = (IMappingView3dData) mappingViewData;
			mapping2DData = null;
			if (!thirdDimensionScaler.isDisposed()) {
				thirdDimensionScaler.setVisible(true);
			}
			if (!axisSelectionComposite.isDisposed()) {
				axisSelectionComposite.setVisible(true);
			}
		} else {
			mapping2DData = mappingViewData;
			mapping3DData = null;
			if (!thirdDimensionScaler.isDisposed()) {
				thirdDimensionScaler.setVisible(false);
			}
			if (!axisSelectionComposite.isDisposed()) {
				axisSelectionComposite.setVisible(false);
			}
		}
		fireMappingDataSet();
	}

	private void fireMappingDataSet() {
		// StructuredSelection selection = new StructuredSelection(getHistogramSelectionDataset());
		// notifyListeners(selection);
	}

	@Override
	public IMappingView2dData getMappingViewData() {
		if (mapping3DData != null) {
			return mapping3DData;
		}
		return mapping2DData;
	}

	@Override
	public HistogramDataUpdate getHistogramDataUpdate() {
		IDataset slice = getEntireDataSlice();
		return new HistogramDataUpdate(slice);
	}

	/**
	 * @return slice of the entire dataset
	 */
	protected IDataset getEntireDataSlice() {
		IDataset slice = null;
		if (mapping3DData != null) {
			int[] shape = mapping3DData.getDataSet().getShape();
			int thirdDim = thirdDimensionScaler.getSelection();
			switch (getSelectedRdBtnVal()) {
			case 1:
				slice = mapping3DData.getDataSet().getSlice(new Slice(thirdDim, thirdDim + 1), new Slice(null),
						new Slice(null));
				slice.setShape(shape[1], shape[2]);
				break;
			case 2:
				slice = mapping3DData.getDataSet().getSlice(new Slice(null), new Slice(thirdDim, thirdDim + 1),
						new Slice(null));
				slice.setShape(shape[0], shape[2]);
				break;
			case 3:
				slice = mapping3DData.getDataSet().getSlice(new Slice(null), new Slice(null),
						new Slice(thirdDim, thirdDim + 1));
				slice.setShape(shape[0], shape[1]);
			}
			if (slice != null) {
				if (btnFlipAxis.getSelection()) {
					slice = ((AbstractDataset) slice).transpose();
				}
			}
		} else if (mapping2DData != null) {
			slice = mapping2DData.getDataSet().getSlice(new Slice(null), new Slice(null));
		}
		return slice;
	}

	@Override
	public void applyHistogramUpdate(HistogramUpdate update) {
		if (plottingSystem != null) {
			// FIXME - check apply colour cast
			// plottingSystem.applyColourCast(update.getRedMapFunction(), update.getGreenMapFunction(),
			// update.getBlueMapFunction(), update.getAlphaMapFunction(), update.inverseRed(),
			// update.inverseGreen(), update.inverseBlue(), update.inverseAlpha(), update.getMinValue(),
			// update.getMaxValue());
			// plottingSystem.refresh(true);
		}
	}

	@Override
	public void selectAllForHistogram() {
		removeAreaRegion();
		notifyAreaSelectedChanged();
	}

	private void removeAreaRegion() {
		IRegion areaRegion = plottingSystem.getRegion(REGION_AREA_SELECTION);
		if (areaRegion != null) {
			plottingSystem.removeRegion(areaRegion);
		}

	}

	private int[] normalize(int[] is) {
		int maxX = currentSlice.getShape()[1] - 1;
		if (is[0] > maxX)
			is[0] = maxX;
		if (is[0] < 0)
			is[0] = 0;

		int maxY = currentSlice.getShape()[0] - 1;
		if (is[1] > maxY)
			is[1] = maxY;
		if (is[1] < 0)
			is[1] = 0;
		return is;
	}

	@Override
	public boolean setFocus() {
		logger.warn("focus set in 2 d plot composite");
		return super.setFocus();
	}

	private PixelSelection getPixelSelection() {
		return new ITwoDSelection.PixelSelection(new int[] { xPoint, yPoint });
	}

}
