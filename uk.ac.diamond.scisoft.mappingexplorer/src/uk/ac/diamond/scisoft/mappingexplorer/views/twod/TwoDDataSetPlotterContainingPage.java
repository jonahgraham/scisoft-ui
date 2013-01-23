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

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
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
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;
import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection.DimensionChanged;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.IDatasetPlotterContainingView;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView2dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.MappingViewSelectionChangedEvent;
import uk.ac.diamond.scisoft.mappingexplorer.views.histogram.HistogramSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.oned.IOneDSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection.TwoDSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDViewOverlayConsumer.IConsumerListener;
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

	private static final String NOTIFY_PIXEL_CHANGED_job_lbl = "Notify Pixel Changed";
	private static final String NOTIFY_AREA_SELECTED_CHANGED_job_lbl = "Notify Area Selected Changed";
	private static final String COMMON_VIEW = "CommonView";
	private static final String UPDATE_COLOUR_MAPPING_lbl = "Update Colour Mapping";
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
	private AbstractPlottingSystem plottingSystem;
	// private TwoDViewOverlayConsumer consumer;
	private Composite axisSelectionComposite;

	private IConsumerListener consumerListener = new IConsumerListener() {

		@Override
		public void pixelSelected(int[] pixel) {
			firePixelSelected(pixel);
		}

		@Override
		public void areaSelected(Point[] rectangleCoordinates) {
			notifyAreaSelectedChanged();
		}
	};

	public TwoDDataSetPlotterContainingPage(Page page, Composite parent, int style, String secondaryViewId) {
		super(parent, style);
		this.secondaryId = secondaryViewId;
		GridLayout layout = new GridLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 2;
		this.setLayout(layout);
		this.setBackground(ColorConstants.white);
		axisSelectionComposite = new Composite(this, SWT.None);
		axisSelectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		axisSelectionComposite.setBackground(ColorConstants.white);
		axisSelectionComposite.setLayout(new GridLayout(4, true));

		rdDimension1 = new Button(axisSelectionComposite, SWT.RADIO);
		rdDimension1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension1.setBackground(ColorConstants.white);
		rdDimension1.addSelectionListener(rdDimensionSelectionListener);

		rdDimension2 = new Button(axisSelectionComposite, SWT.RADIO);
		rdDimension2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension2.setBackground(ColorConstants.white);
		rdDimension2.addSelectionListener(rdDimensionSelectionListener);

		rdDimension3 = new Button(axisSelectionComposite, SWT.RADIO);
		rdDimension3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rdDimension3.setBackground(ColorConstants.white);
		rdDimension3.addSelectionListener(rdDimensionSelectionListener);

		btnFlipAxis = new Button(axisSelectionComposite, SWT.TOGGLE | SWT.RIGHT);
		btnFlipAxis.setText(FLIP_AXIS_lbl);
		btnFlipAxis.setLayoutData(new GridData());
		btnFlipAxis.setBackground(ColorConstants.white);
		btnFlipAxis.addSelectionListener(flipBtnSelectionListener);

		Composite plotComposite = new Composite(this, SWT.None);
		//
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComposite, "TwoDPlot", page.getSite().getActionBars(),
					PlotType.XY_STACKED, null);
		} catch (RuntimeException ex) {
			logger.error("There is a problem with datasetPlotter.setMode()");
		}
		// FIXME - Add roichange listener
		// consumer.addConsumerListener(consumerListener);
		catch (Exception e) {
			logger.error("Problem creating plotting system", e);
		}

		//
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite thirdDimensionComposite = new Composite(this, SWT.None);
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
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 50;

		thirdDimensionScaler.setLayoutData(gd);
		thirdDimensionScaler.addStepperSelectionListener(stepperSelectionListener);
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
					// consumer.clearOverlays();
					thirdDimensionScaler.setSteps(steps, data);
					thirdDimensionScaler.setText(stepperLabel);
					fireUpdatePlot();
					fireNotifyDimensionChanged(DimensionChanged.INDEX);
					fireUpdateColourMapping();
					cleanup();
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
			return new AxisSelection(getSelectedDimensionLabel(getSelectedRdBtnVal()), getSelectedRdBtnVal(),
					thirdDimensionScaler.getSelection());
		}
		return null;
	}

	protected void fireNotifyDimensionChanged(final DimensionChanged dimensionChangeAffected) {
		UIJob job = new UIJob(getDisplay(), NOTIFY_DIMENSION_CHANGED) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				List<ISelection> selElements = new ArrayList<ISelection>();
				TwoDSelection selection = new TwoDSelection(secondaryId,
						MappingViewSelectionChangedEvent.DIMENSION_SELECTION, getSelectedDimension(),
						btnFlipAxis.getSelection());
				selection.getAxisDimensionSelection().setChangeAffected(dimensionChangeAffected);
				selElements.add(selection);
				// selElements.add(getHistogramSelectionDataset());
				StructuredSelection sel = new StructuredSelection(selElements);
				notifyListeners(sel);
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
				fireNotifyDimensionChanged(DimensionChanged.VALUE);

				fireUpdateColourMapping();
			} catch (Exception e1) {
				logger.error("Problem flipping image {}", e1);
			}
		}
	};

	@Override
	public void dispose() {
		if (!thirdDimensionScaler.isDisposed()) {
			thirdDimensionScaler.removeStepperSelectionListener(stepperSelectionListener);
		}
		cleanup();
		// consumer.removeConsumerListener(consumerListener);
		super.dispose();
	}

	protected void fireUpdateColourMapping() {
		UIJob histogramUpdateJob = new UIJob(getDisplay(), UPDATE_COLOUR_MAPPING_lbl) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				HistogramView histogramView = getHistogramView();
				if (histogramView != null) {
					histogramView.setData(getHistogramDataUpdate());
				}
				return Status.OK_STATUS;
			}
		};

		histogramUpdateJob.schedule();

	}

	private HistogramView getHistogramView() {
		IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		for (IViewReference iViewReference : viewReferences) {
			if (iViewReference.getId().equals(HistogramView.ID)) {
				if (secondaryId == null && COMMON_VIEW.equals(iViewReference.getSecondaryId())) {
					return (HistogramView) iViewReference.getView(false);
				}

				if (secondaryId != null && secondaryId.equals(iViewReference.getSecondaryId())) {
					return (HistogramView) iViewReference.getView(false);
				}
			}

		}
		return null;
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

	@Override
	public void updatePlot() throws Exception {
		if (mapping3DData == null && mapping2DData == null) {
			throw new IllegalArgumentException("Mapping View Data not available");
		}
		AbstractDataset slice = null;

		AxisValues xAxisValues = null;
		AxisValues yAxisValues = null;
		String xAxisLabel = null;
		String yAxisLabel = null;
		if (mapping3DData != null) {
			ILazyDataset dataset = mapping3DData.getDataSet();
			int[] shape = dataset.getShape();

			int tdSel = thirdDimensionScaler.getSelection();
			if (rdDimension1.getSelection()) {
				slice = (AbstractDataset) dataset.getSlice(new Slice(tdSel, tdSel + 1), new Slice(null),
						new Slice(null));
				slice.setShape(shape[1], shape[2]);

				xAxisLabel = mapping3DData.getDimension3Label();
				yAxisLabel = mapping3DData.getDimension2Label();

				if (mapping3DData.getDimension3Values() != null) {
					xAxisValues = new AxisValues(mapping3DData.getDimension3Values());
				}
				if (mapping3DData.getDimension2Values() != null) {
					yAxisValues = new AxisValues(mapping3DData.getDimension2Values());
				}
			}
			if (rdDimension2.getSelection()) {
				slice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(tdSel, tdSel + 1),
						new Slice(null));
				slice.setShape(shape[0], shape[2]);

				xAxisLabel = mapping3DData.getDimension3Label();
				yAxisLabel = mapping3DData.getDimension1Label();

				if (mapping3DData.getDimension3Values() != null) {
					xAxisValues = new AxisValues(mapping3DData.getDimension3Values());
				}
				if (mapping3DData.getDimension1Values() != null) {
					yAxisValues = new AxisValues(mapping3DData.getDimension1Values());
				}
			}
			if (rdDimension3.getSelection()) {
				slice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(null),
						new Slice(tdSel, tdSel + 1));
				slice.setShape(shape[0], shape[1]);

				xAxisLabel = mapping3DData.getDimension2Label();
				yAxisLabel = mapping3DData.getDimension1Label();

				if (mapping3DData.getDimension2Values() != null) {
					xAxisValues = new AxisValues(mapping3DData.getDimension2Values());
				}
				if (mapping3DData.getDimension1Values() != null) {
					yAxisValues = new AxisValues(mapping3DData.getDimension1Values());
				}
			}
			if (btnFlipAxis.getSelection()) {
				String tmp = yAxisLabel;
				yAxisLabel = xAxisLabel;
				xAxisLabel = tmp;

				AxisValues tmpA = yAxisValues;
				yAxisValues = xAxisValues;
				xAxisValues = tmpA;

				if (slice != null) {
					slice = slice.transpose();
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

			slice = (AbstractDataset) dataset.getSlice(new Slice(null), new Slice(null));
		}
		// FIXME
		// plottingSystem.setAxisModes(xAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM,
		// yAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM, AxisMode.LINEAR);
		// if (xAxisValues != null)
		// plottingSystem.getSelectedXAxis().setsetXAxisValues(xAxisValues, 1);
		// if (yAxisValues != null)
		// plottingSystem.setYAxisValues(yAxisValues);
		// if (yAxisLabel != null)
		// plottingSystem.setYAxisLabel(yAxisLabel);
		// if (xAxisLabel != null)
		// plottingSystem.setXAxisLabel(xAxisLabel)

		List<AbstractDataset> axisList = new ArrayList<AbstractDataset>();
		plottingSystem.updatePlot2D(slice, axisList, new NullProgressMonitor());
	}

	@Override
	public ISelection getSelection() {
		List<ISelection> selections = new ArrayList<ISelection>();
		selections.add(new TwoDSelection(secondaryId, MappingViewSelectionChangedEvent.DIMENSION_SELECTION,
				getSelectedDimension(), btnFlipAxis.getSelection()));
		// selections.add(getHistogramSelectionDataset());
		return new StructuredSelection(selections);
	}

	// FIXME - Usage of getHistogramSelectionDataset
	protected ISelection getHistogramSelectionDataset() {
		Point[] rc = new Point[] { new Point(20, 20), new Point(40, 40) };// FIXME - consumer.getRectangleCoordinates();
		IDataset slice = null;

		if (mapping3DData != null) {
			int thirdDim = thirdDimensionScaler.getSelection();
			if (rc != null) {
				switch (getSelectedRdBtnVal()) {
				case 1:
					slice = mapping3DData.getDataSet().getSlice(new Slice(thirdDim, thirdDim + 1),
							new Slice(rc[0].y, rc[1].y), new Slice(rc[0].x, rc[1].x));
					slice.setShape(rc[1].y - rc[0].y, rc[1].x - rc[0].x);
					break;
				case 2:
					slice = mapping3DData.getDataSet().getSlice(new Slice(rc[0].y, rc[1].y),
							new Slice(thirdDim, thirdDim + 1), new Slice(rc[0].x, rc[1].x));
					slice.setShape(rc[1].y - rc[0].y, rc[1].x - rc[0].x);
					break;
				case 3:
					slice = mapping3DData.getDataSet().getSlice(new Slice(rc[0].y, rc[1].y),
							new Slice(rc[0].x, rc[1].x), new Slice(thirdDim, thirdDim + 1));
					slice.setShape(rc[1].y - rc[0].y, rc[1].x - rc[0].x);
				}
				if (slice != null) {
					if (btnFlipAxis.getSelection()) {
						slice = ((AbstractDataset) slice).transpose();
					}
				}
				return new HistogramSelection(slice);
			}

			// if no rectangular is selected
			int[] shape = mapping3DData.getDataSet().getShape();

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
			if (rc != null) {
				slice = mapping2DData.getDataSet().getSlice(new Slice(rc[0].y, rc[1].y), new Slice(rc[0].x, rc[1].x));
				slice.setShape(rc[1].y - rc[0].y, rc[1].x - rc[0].x);
			} else {
				slice = mapping2DData.getDataSet().getSlice(new Slice(null), new Slice(null));
			}

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
			updateConsumer(pos1, pos2);
		}
	}

	private void updateConsumer(int pos1, int pos2) {
		// FIXME
		// consumer.clearCrosswire();
		// consumer.drawHighlighterCrossWire(pos1, pos2);
	}

	private void notifyAreaSelectedChanged() {
		UIJob job = new UIJob(getDisplay(), NOTIFY_AREA_SELECTED_CHANGED_job_lbl) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// notifyListeners(getHistogramSelectionDataset());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void notifyPixelChanged(final int[] endPosition) {
		UIJob job = new UIJob(getDisplay(), NOTIFY_PIXEL_CHANGED_job_lbl) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				TwoDSelection selection = new TwoDSelection(secondaryId,
						MappingViewSelectionChangedEvent.PIXEL_SELECTION, getSelectedDimension(),
						btnFlipAxis.getSelection());
				selection.setPixelSelection(new ITwoDSelection.PixelSelection(endPosition));

				notifyListeners(selection);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	protected void firePixelSelected(int[] pixel) {
		// FIXME
		// consumer.clearCrosswire();
		// try {
		// consumer.drawHighlighterCrossWire(pixel[0], pixel[1]);
		// } catch (Exception ex) {
		// logger.error("Error while drawing box:{}", ex);
		// }
		// notifyPixelChanged(pixel);
	}

	public void setMappingViewData(IMappingView2dData mappingViewData) {
		// consumer.clearOverlays();
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
		fireUpdateColourMapping();
	}

	private void fireMappingDataSet() {
		// StructuredSelection selection = new StructuredSelection(getHistogramSelectionDataset());
		// notifyListeners(selection);
	}

	@Override
	public void cleanup() {
		// consumer.clearOverlays();
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
		// FIXME -
		// consumer.clearAreaOverlay();
		// notifyAreaSelectedChanged();
	}

}