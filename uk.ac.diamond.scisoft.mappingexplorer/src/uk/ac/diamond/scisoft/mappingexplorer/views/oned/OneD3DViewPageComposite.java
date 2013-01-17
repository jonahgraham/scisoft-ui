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

import gda.analysis.io.ScanFileHolderException;

import java.util.Collections;
import java.util.Iterator;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.mappingexplorer.MappingExplorerPlugin;
import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingView3dData;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingViewData;
import uk.ac.diamond.scisoft.mappingexplorer.views.MappingViewSelectionChangedEvent;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.ITwoDSelection.IPixelSelection;
import uk.ac.gda.monitor.IMonitor;
import uk.ac.gda.ui.components.IStepperSelectionListener;
import uk.ac.gda.ui.components.Stepper;
import uk.ac.gda.ui.components.StepperChangedEvent;

/**
 * Composite that is added to the page book to represent 3D data on a OneD view.
 * 
 * @author rsr31645
 */
public class OneD3DViewPageComposite extends BaseViewPageComposite {
	private Button rdDimension1;
	private Button rdDimension2;
	private Button rdDimension3;
	private AbstractPlottingSystem plottingSystem;
	private Composite axisSelectionComposite;

	private Stepper firstDimStepper;
	private Stepper secondDimStepper;
	private IMappingView3dData mappingViewData;
	private String secondaryViewId;

	private final static Logger logger = LoggerFactory.getLogger(OneD3DViewPageComposite.class);

	public OneD3DViewPageComposite(Composite parent, int style) {
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

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.setAxisModes(AxisMode.LINEAR, AxisMode.LINEAR, AxisMode.LINEAR);

		plottingSystem.getComposite().setLayoutData(new GridData(GridData.FILL_BOTH));

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
		plottingSystem.cleanUp();
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
		if (mappingViewData == null) {
			throw new IllegalArgumentException("Mapping View Data not available");
		}

		ILazyDataset dataset = mappingViewData.getDataSet();
		int[] shape = dataset.getShape();

		IDataset slice = null;
		int[] finalShape = null;
		int firstDimmerSel = firstDimStepper.getSelection();
		int secondDimmerSel = secondDimStepper.getSelection();
		String xAxislabel = null;
		AxisValues axisValues = null;

		try{
			if (rdDimension1.getSelection()) {
				slice = dataset.getSlice((IMonitor)null, new Slice(null), new Slice(firstDimmerSel, firstDimmerSel + 1), new Slice(
						secondDimmerSel, secondDimmerSel + 1));
				finalShape = new int[] { shape[0] };

				xAxislabel = mappingViewData.getDimension1Label();
				if (mappingViewData.getDimension1Values() != null) {
					axisValues = new AxisValues();
					axisValues.setValues(mappingViewData.getDimension1Values());
				}
			} else if (rdDimension2.getSelection()) {
				slice = dataset.getSlice((IMonitor)null,new Slice(firstDimmerSel, firstDimmerSel + 1), new Slice(null), new Slice(
						secondDimmerSel, secondDimmerSel + 1));
				finalShape = new int[] { shape[1] };
				xAxislabel = mappingViewData.getDimension2Label();
				if (mappingViewData.getDimension2Values() != null) {
					axisValues = new AxisValues();
					axisValues.setValues(mappingViewData.getDimension2Values());
				}
			} else if (rdDimension3.getSelection()) {
				slice = dataset.getSlice((IMonitor)null, new Slice(firstDimmerSel, firstDimmerSel + 1), new Slice(secondDimmerSel,
						secondDimmerSel + 1), new Slice(null));

				finalShape = new int[] { shape[2] };
				xAxislabel = mappingViewData.getDimension3Label();
				if (mappingViewData.getDimension3Values() != null) {
					axisValues = new AxisValues();
					axisValues.setValues(mappingViewData.getDimension3Values());
				}
			}
			if (slice != null) {
				slice.setShape(finalShape);
				if (axisValues != null) {
					plottingSystem.setPlotUpdateOperation(true);
					plottingSystem.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
					plottingSystem.setXAxisValues(axisValues, 1);
					plottingSystem.setSecondaryXAxisValues(null,"");
					plottingSystem.replaceAllPlots(Collections.singletonList(slice));
					plottingSystem.updateAllAppearance();
				} else {
					plottingSystem.replaceCurrentPlot(slice);
				}
				plottingSystem.setXAxisLabel(xAxislabel);
				plottingSystem.refresh(false);
			}
		} catch(ScanFileHolderException ex){
			throw new Exception("Error loading data from file during update",ex);
		}
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
						try {
							fireUpdatePlot();
						} catch (Exception e) {
							logger.error("Problem setting radio button control", e);
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
			UIJob job = new UIJob(getDisplay(), "Prepare Pixel Selection") {

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
			if (MappingViewSelectionChangedEvent.DIMENSION_SELECTION == sel.getChangedEvent()) {
				AxisSelection selectedChangedObject = sel.getAxisDimensionSelection();
				if (selectedChangedObject != null) {
					selectDimensionAxis(selectedChangedObject);
				}

			} else if (MappingViewSelectionChangedEvent.PIXEL_SELECTION == sel.getChangedEvent()) {
				selectDimensionAxis(sel.getAxisDimensionSelection());
				selectPixel(sel.getPixelSelection(), sel.isFlipped());
			}
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
	public void cleanup() {
		// dataSetPlotter.cleanUp();
	}

	@Override
	public IMappingViewData getMappingViewData() {
		return mappingViewData;
	}

}