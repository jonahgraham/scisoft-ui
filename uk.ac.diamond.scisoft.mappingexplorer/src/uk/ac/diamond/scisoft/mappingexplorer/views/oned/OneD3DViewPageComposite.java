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
package uk.ac.diamond.scisoft.mappingexplorer.views.oned;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.mappingexplorer.MappingExplorerPlugin;
import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingViewData;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection.IPixelSelection;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

/**
 * Composite that is added to the page book to represent 3D data on a OneD view.
 * 
 * @author rsr31645
 */
public class OneD3DViewPageComposite extends BaseViewPageComposite {
	private static final String PREPARE_PIXEL_SELECTION = "Prepare Pixel Selection";
	private static final String PLOT_PART_NAME = "OneDPlot";
	private Button rdDimension1;
	private Button rdDimension2;
	private Button rdDimension3;
	private IPlottingSystem plottingSystem;
	private Composite axisSelectionComposite;

	private Stepper firstDimStepper;
	private Stepper secondDimStepper;
	private IMappingView3dData mappingViewData;
	private String secondaryViewId;
	private UpdatePlotJob updatePlotJob;

	private final static Logger logger = LoggerFactory.getLogger(OneD3DViewPageComposite.class);

	public OneD3DViewPageComposite(Page page, Composite parent, int style) {
		super(parent, style);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		this.setBackground(ColorConstants.white);

		axisSelectionComposite = new Composite(this, SWT.None);
		axisSelectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		axisSelectionComposite.setBackground(ColorConstants.white);
		axisSelectionComposite.setLayout(new GridLayout(3, true));

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

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Problem creating plotting system", e);
		}
		plottingSystem.createPlotPart(plotComposite, PLOT_PART_NAME, page.getSite().getActionBars(),
				PlotType.XY_STACKED, null);

		disablePlottingSystemActions(plottingSystem);

		Composite configurerComposite = new Composite(this, SWT.None);
		configurerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout2 = new GridLayout();
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;

		configurerComposite.setLayout(layout2);
		configurerComposite.setBackground(ColorConstants.white);

		firstDimStepper = new Stepper(configurerComposite, SWT.None);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 50;
		firstDimStepper.setLayoutData(layoutData);
		firstDimStepper.addStepperSelectionListener(stepperSelectionListener);

		secondDimStepper = new Stepper(configurerComposite, SWT.None);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 50;
		secondDimStepper.setLayoutData(layoutData);
		secondDimStepper.addStepperSelectionListener(stepperSelectionListener);

		createPlotUpdateJob();
	}

	private void createPlotUpdateJob() {
		if (updatePlotJob == null) {
			updatePlotJob = new UpdatePlotJob(getDisplay());
		}
	}

	public void setMappingViewData(IMappingView3dData mappingViewData) {
		this.mappingViewData = mappingViewData;
	}

	private SelectionListener rdDimensionSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				// Need to do this check so that only one request to updatePlot
				// is sent.
				Control control = (Control) e.getSource();
				boolean changed = false;
				if (control.equals(rdDimension1) && rdDimension1.getSelection()) {
					changed = true;
				} else if (control.equals(rdDimension2) && rdDimension2.getSelection()) {
					changed = true;
				} else if (control.equals(rdDimension3) && rdDimension3.getSelection()) {
					changed = true;
				}
				if (changed) {
					firstDimStepper.setSelection(0);
					secondDimStepper.setSelection(0);
					fireUpdatePlot();
				}
			} catch (Exception e1) {
				logger.error("Error updating plot", e1);
			}
		}
	};

	private void fireUpdatePlot() throws Exception {
		if (rdDimension1.getSelection()) {
			firstDimStepper.setSteps(mappingViewData.getDataSet().getShape()[1], mappingViewData.getDimension2Values());
			secondDimStepper
					.setSteps(mappingViewData.getDataSet().getShape()[2], mappingViewData.getDimension3Values());

			firstDimStepper.setText(mappingViewData.getDimension2Label());
			secondDimStepper.setText(mappingViewData.getDimension3Label());

		} else if (rdDimension2.getSelection()) {
			firstDimStepper.setSteps(mappingViewData.getDataSet().getShape()[0], mappingViewData.getDimension1Values());
			secondDimStepper
					.setSteps(mappingViewData.getDataSet().getShape()[2], mappingViewData.getDimension3Values());

			firstDimStepper.setText(mappingViewData.getDimension1Label());
			secondDimStepper.setText(mappingViewData.getDimension3Label());
		} else if (rdDimension3.getSelection()) {
			firstDimStepper.setSteps(mappingViewData.getDataSet().getShape()[0], mappingViewData.getDimension1Values());
			secondDimStepper
					.setSteps(mappingViewData.getDataSet().getShape()[1], mappingViewData.getDimension2Values());

			firstDimStepper.setText(mappingViewData.getDimension1Label());
			secondDimStepper.setText(mappingViewData.getDimension2Label());
		}

		updatePlot();
	}

	@Override
	public void dispose() {
		firstDimStepper.removeStepperSelectionListener(stepperSelectionListener);
		secondDimStepper.removeStepperSelectionListener(stepperSelectionListener);
		super.dispose();
	}

	private IStepperSelectionListener stepperSelectionListener = new IStepperSelectionListener() {

		@Override
		public void stepperChanged(StepperChangedEvent e) {
			if (e.getSource().equals(firstDimStepper)) {
				fireStepperValueChanged();
				try {
					updatePlot();
				} catch (Exception e1) {
					logger.error("Error plotting One D {}", e);
				}
			} else if (e.getSource().equals(secondDimStepper)) {
				fireStepperValueChanged();
				try {
					updatePlot();
				} catch (Exception e1) {
					logger.error("Error plotting One D {}", e);
				}
			}
		}
	};

	protected void fireStepperValueChanged() {
		AxisSelection axisSelection1 = new AxisSelection(firstDimStepper.getText(), firstDimStepper.getSelection());
		AxisSelection axisSelection2 = new AxisSelection(secondDimStepper.getText(), secondDimStepper.getSelection());
		notifyListeners(new IOneDSelection.OneDSelection(secondaryViewId, axisSelection1, axisSelection2));
	}

	@Override
	public void updatePlot() throws Exception {
		final int firstDimmerSel = firstDimStepper.getSelection();
		final int secondDimmerSel = secondDimStepper.getSelection();
		final boolean dim1Selection = rdDimension1.getSelection();
		final boolean dim2Selection = rdDimension2.getSelection();
		final boolean dim3Selection = rdDimension3.getSelection();

		updatePlotJob.cancel();

		updatePlotJob.setDim1Selection(dim1Selection);
		updatePlotJob.setDim2Selection(dim2Selection);
		updatePlotJob.setDim3Selection(dim3Selection);
		updatePlotJob.setStepper1Value(firstDimmerSel);
		updatePlotJob.setStepper2Value(secondDimmerSel);
		updatePlotJob.schedule(200);

	}

	public void disableAxisComposite() {
		axisSelectionComposite.setVisible(false);
	}

	public void enableAxisComposite() {
		axisSelectionComposite.setVisible(true);
	}

	@Override
	public void initialPlot() throws Exception {
		firstDimStepper.setText(mappingViewData.getDimension1Label());
		secondDimStepper.setText(mappingViewData.getDimension2Label());
		// assuming it is a 3d dataset
		int[] shape = mappingViewData.getDataSet().getShape();
		rdDimension3.setSelection(true);
		firstDimStepper.setSteps(shape[0], mappingViewData.getDimension1Values());
		secondDimStepper.setSteps(shape[1], mappingViewData.getDimension2Values());

		rdDimension1.setText(mappingViewData.getDimension1Label());
		rdDimension2.setText(mappingViewData.getDimension2Label());
		rdDimension3.setText(mappingViewData.getDimension3Label());
		updatePlot();
	}

	private boolean isSecondaryIdSame(String comparingPartSecondaryId) {
		return (comparingPartSecondaryId == null && secondaryViewId == null)
				|| (secondaryViewId != null && secondaryViewId.equals(comparingPartSecondaryId));
	}

	private class UpdatePlotJob extends Job {

		private static final String DATA_SLICE_TO_BE_DISPLAYED = "Data slice";
		private final Display display;
		private boolean dim1Selection;
		private boolean dim2Selection;
		private boolean dim3Selection;
		private int stepper1Val;
		private int stepper2Val;

		public UpdatePlotJob(Display display) {
			super("Update OneD Plot");
			this.display = display;
		}

		public void setDim1Selection(boolean dim1Selection) {
			this.dim1Selection = dim1Selection;

		}

		public void setDim2Selection(boolean dim2Selection) {
			this.dim2Selection = dim2Selection;

		}

		public void setDim3Selection(boolean dim3Selection) {
			this.dim3Selection = dim3Selection;

		}

		public void setStepper1Value(int stepper1Val) {
			this.stepper1Val = stepper1Val;
		}

		public void setStepper2Value(int stepper2Val) {
			this.stepper2Val = stepper2Val;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (mappingViewData == null) {
				throw new IllegalArgumentException("Mapping View Data not available");
			}

			ILazyDataset dataset;
			try {
				dataset = mappingViewData.getDataSet();
			} catch (DatasetException e1) {
				return new Status(IStatus.ERROR, "uk.ac.diamond.scisoft.mappingexplorer", 0, "Could not get dataset", e1);
			}
			final int[] shape = dataset.getShape();
			IDataset slice = null;
			int[] finalShape = null;
			String xAxislabel = null;
			Dataset axisValues = null;
			try {
				if (dim1Selection) {
					slice = dataset.getSlice((IMonitor) null, new Slice(null), new Slice(stepper1Val,
							stepper1Val + 1), new Slice(stepper2Val, stepper2Val + 1));
					finalShape = new int[] { shape[0] };

					xAxislabel = mappingViewData.getDimension1Label();
					if (mappingViewData.getDimension1Values() != null) {
						axisValues = DatasetFactory.createFromObject(mappingViewData.getDimension1Values());
					}
				} else if (dim2Selection) {
					slice = dataset.getSlice((IMonitor) null,
							new Slice(stepper1Val, stepper1Val + 1), new Slice(null), new Slice(stepper2Val,
									stepper2Val + 1));
					finalShape = new int[] { shape[1] };
					xAxislabel = mappingViewData.getDimension2Label();
					if (mappingViewData.getDimension2Values() != null) {
						axisValues = DatasetFactory.createFromObject(mappingViewData.getDimension2Values());
					}
				} else if (dim3Selection) {
					slice = dataset.getSlice((IMonitor) null,
							new Slice(stepper1Val, stepper1Val + 1), new Slice(stepper2Val, stepper2Val + 1),
							new Slice(null));

					finalShape = new int[] { shape[2] };
					xAxislabel = mappingViewData.getDimension3Label();
					if (mappingViewData.getDimension3Values() != null) {
						axisValues = DatasetFactory.createFromObject(mappingViewData.getDimension3Values());
					}
				}
				final int[] shapeToplot = finalShape;
				final IDataset sliceToPlot = slice;
				final String xAxisLabelToPlot = xAxislabel;

				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {

						@Override
						public void run() {

							if (sliceToPlot != null) {
								sliceToPlot.setShape(shapeToplot);
								sliceToPlot.setName(DATA_SLICE_TO_BE_DISPLAYED);
								if (xAxisLabelToPlot != null) {
									plottingSystem.getSelectedXAxis().setTitle(xAxisLabelToPlot);
								}
								plottingSystem.updatePlot1D(null, Arrays.asList(sliceToPlot), new NullProgressMonitor());
								plottingSystem.setTitle("One D plot across slices");
								plottingSystem.autoscaleAxes();
							}
						}
					});
				}
			} catch (DatasetException ex) {
				logger.error("Error loading data from file during update", ex);
			} catch (Exception e) {
				logger.error("Error getting slice of data", e);
			}
			return Status.OK_STATUS;
		}
	}

	private void selectDimensionAxis(final AxisSelection axisSelection) {
		if (getDisplay() != null) {
			UIJob job = new UIJob(getDisplay(), "Prepare Axis Selection") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					boolean changed = false;
					int dimension = axisSelection.getDimension();
					if (!isDisposed() && getDisplay() != null) {
						if (dimension == 1 && !rdDimension1.getSelection()) {
							changed = true;
							rdDimension1.setSelection(true);
							rdDimension2.setSelection(false);
							rdDimension3.setSelection(false);
						} else if (dimension == 2 && !rdDimension2.getSelection()) {
							changed = true;
							rdDimension1.setSelection(false);
							rdDimension2.setSelection(true);
							rdDimension3.setSelection(false);
						} else if (dimension == 3 && !rdDimension3.getSelection()) {
							changed = true;
							rdDimension1.setSelection(false);
							rdDimension2.setSelection(false);
							rdDimension3.setSelection(true);
						}
						if (changed) {
							try {
								fireUpdatePlot();
							} catch (Exception e) {
								logger.error("Problem setting radio button control", e);
							}
						}
					}
					return new Status(IStatus.OK, MappingExplorerPlugin.PLUGIN_ID, Boolean.toString(changed));
				}
			};
			job.schedule();
		}
	}

	private void selectPixel(final IPixelSelection pixelSelection, final boolean isFlipped) {
		final int x = pixelSelection.getSelectedPixel()[0];
		final int y = pixelSelection.getSelectedPixel()[1];
		if (getDisplay() != null) {
			UIJob job = new UIJob(getDisplay(), PREPARE_PIXEL_SELECTION) {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					int pos1 = x;
					int pos2 = y;
					boolean changed = false;
					if (isFlipped) {
						if (pos1 >= firstDimStepper.getSteps()) {
							pos1 = firstDimStepper.getSteps() - 1;
						}
						if (pos2 >= secondDimStepper.getSteps()) {
							pos2 = secondDimStepper.getSteps() - 1;
						}
						if (firstDimStepper.getSelection() != pos1) {
							firstDimStepper.setSelection(pos1);
							changed = true;
						}
						if (secondDimStepper.getSelection() != pos2) {
							changed = true;
							secondDimStepper.setSelection(pos2);
						}
					} else {
						if (pos1 >= secondDimStepper.getSteps()) {
							pos1 = secondDimStepper.getSteps() - 1;
						}
						if (pos2 >= firstDimStepper.getSteps()) {
							pos2 = firstDimStepper.getSteps() - 1;
						}
						if (firstDimStepper.getSelection() != pos2) {
							changed = true;

							firstDimStepper.setSelection(pos2);
						}
						if (secondDimStepper.getSelection() != pos1) {
							changed = true;
							secondDimStepper.setSelection(pos1);
						}
					}
					if (changed) {
						try {
							fireUpdatePlot();
						} catch (Exception e) {
							logger.error("Problem updating Plot {}", e);
						}
					}
					return new Status(IStatus.OK, MappingExplorerPlugin.PLUGIN_ID, null);
				}
			};
			job.schedule();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		reactToSelection(selection);
	}

	@SuppressWarnings("rawtypes")
	private void reactToSelection(ISelection selection) {
		ITwoDSelection sel = null;
		if (selection instanceof StructuredSelection) {
			Iterator selIterator = ((StructuredSelection) selection).iterator();
			while (selIterator.hasNext()) {
				Object next = selIterator.next();
				if (next instanceof ITwoDSelection) {
					sel = (ITwoDSelection) next;
					break;
				}
			}
		} else if (selection instanceof ITwoDSelection) {
			sel = (ITwoDSelection) selection;
		}
		if (sel != null && isSecondaryIdSame(sel.getSecondaryViewId())) {
			disableAxisComposite();
			AxisSelection selectedChangedObject = sel.getAxisDimensionSelection();
			if (selectedChangedObject != null) {
				selectDimensionAxis(selectedChangedObject);
			}
			selectPixel(sel.getPixelSelection(), sel.isFlipped());
		}
		if (selection == null) {
			// apparently this should be true when the twod view is disposed.
			enableAxisComposite();
		}
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public IMappingViewData getMappingViewData() {
		return mappingViewData;
	}

}
