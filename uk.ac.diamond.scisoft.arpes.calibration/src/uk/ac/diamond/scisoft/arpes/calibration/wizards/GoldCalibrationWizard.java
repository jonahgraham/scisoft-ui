/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.wizards;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.arpes.calibration.utils.ARPESCalibrationConstants;

public class GoldCalibrationWizard extends Wizard implements INewWizard {

	protected GoldCalibrationPageOne one;
	protected GoldCalibrationPageTwo two;
	protected GoldCalibrationPageThree three;
	protected GoldCalibrationPageFour four;

	private DataMessageComponent calibrationData;
	private IPageChangedListener pageChangeListener;
	private IPageChangingListener pageChangingListener;
	private boolean isProcessOKToRun;
	private GoldCalibrationPageFive five;

	public GoldCalibrationWizard() {
		super();
		setNeedsProgressMonitor(true);
		calibrationData = new DataMessageComponent();
	}

	@Override
	public String getWindowTitle() {
		return "Gold Calibration Wizard";
	}

	@Override
	public boolean performFinish() {
		CalibrationWizardPage page = (CalibrationWizardPage) getContainer().getCurrentPage();
		// if last page
		if (page.getPageNumber() == 5) {
			try {
				if (page.runProcess())
					return true;
			} catch (InterruptedException e) {
				String errorMessage = e.getMessage();
				MessageDialog dialog = new MessageDialog(getShell(), "Saving process interrupted", null, errorMessage,
						MessageDialog.ERROR, new String[] { "OK" }, 0);
				dialog.open();
			}
		}
		return false;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		IWizardContainer wd = getContainer();
		if (wd instanceof WizardDialog) {
			((WizardDialog) wd).addPageChangedListener(pageChangeListener);
			((WizardDialog) wd).addPageChangingListener(pageChangingListener);
		}
		getShell().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					event.doit = false;
				}
			}
		});
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		one = new GoldCalibrationPageOne(calibrationData);
		two = new GoldCalibrationPageTwo(calibrationData);
		three = new GoldCalibrationPageThree(calibrationData);
		four = new GoldCalibrationPageFour(calibrationData);
		five = new GoldCalibrationPageFive(calibrationData);
		pageChangeListener = new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {
				if (event.getSelectedPage() instanceof CalibrationWizardPage) {
					CalibrationWizardPage page = (CalibrationWizardPage) event.getSelectedPage();
					// not the last page
					if (isProcessOKToRun && page.getPageNumber() != 5) {
						try {
							page.runProcess();
						} catch (InterruptedException e) {
							MessageDialog dialog = new MessageDialog(getShell(), "Calibrating process interrupted", null,
									e.getMessage(), MessageDialog.WARNING, new String[] { "OK" }, 0);
							dialog.open();
						}
					}
				}
			}
		};
		pageChangingListener = new IPageChangingListener() {
			@Override
			public void handlePageChanging(PageChangingEvent event) {
				CalibrationWizardPage currentpage = (CalibrationWizardPage) event.getCurrentPage();
				CalibrationWizardPage targetpage = (CalibrationWizardPage) event.getTargetPage();
				if (currentpage.getPageNumber() > targetpage.getPageNumber())
					isProcessOKToRun = false;
				else
					isProcessOKToRun = true;
			}
		};

		addPage(one);
		addPage(two);
		addPage(three);
		addPage(four);
		addPage(five);

		Object selected = selection.getFirstElement();
		String path = "";
		if (selected instanceof IFile) {
			IFile ifile = (IFile) selected;
			path = ifile.getLocation().toOSString();
		} else if (selected instanceof File) {
			File file = (File) selected;
			path = file.getPath();
		}
		setData(path);
	}

	private void setData(String path) {
		try {
			IDataHolder holder = LoaderFactory.getData(path);
			// String[] names = holder.getNames();
			ILazyDataset data = holder.getLazyDataset(ARPESCalibrationConstants.DATANAME);
			IDataset slicedData = data.getSlice(new Slice(0, data.getShape()[0], data.getShape()[1])).squeeze();
			ILazyDataset xaxis = holder.getLazyDataset(ARPESCalibrationConstants.XAXIS_DATANAME);
			IDataset slicedXaxis = xaxis.getSlice(new Slice(0, xaxis.getShape()[0], xaxis.getElementsPerItem()))
					.squeeze();
			ILazyDataset yaxis = holder.getLazyDataset(ARPESCalibrationConstants.YAXIS_DATANAME);
			IDataset slicedYaxis = yaxis.getSlice(new Slice(0, yaxis.getShape()[0], yaxis.getElementsPerItem()))
					.squeeze();
			slicedXaxis.setName("energy");
			slicedYaxis.setName("angle");
			ILazyDataset temp = holder.getLazyDataset(ARPESCalibrationConstants.TEMPERATURE_PATH);
			double temperature = temp.getSlice(new Slice(0, temp.getShape()[0], temp.getElementsPerItem()))
					.getDouble(0);

			calibrationData.addList(ARPESCalibrationConstants.DATANAME, slicedData);
			calibrationData.addList(ARPESCalibrationConstants.XAXIS_DATANAME, slicedXaxis);
			calibrationData.addList(ARPESCalibrationConstants.YAXIS_DATANAME, slicedYaxis);
			calibrationData.addUserObject(ARPESCalibrationConstants.TEMPERATURE_PATH, temperature);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean canFinish() {
		CalibrationWizardPage page = (CalibrationWizardPage) getContainer().getCurrentPage();
		if (page.getPageNumber() == 5)
			return true;
		return false;
	}

}