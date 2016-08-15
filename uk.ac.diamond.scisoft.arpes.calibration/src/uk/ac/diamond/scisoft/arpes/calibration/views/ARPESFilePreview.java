/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.arpes.calibration.Activator;
import uk.ac.diamond.scisoft.arpes.calibration.utils.ARPESCalibrationConstants;

public class ARPESFilePreview {

	private static final Logger logger = LoggerFactory.getLogger(ARPESFilePreview.class);

	private Text txtSelectedFile;
	private IPlottingSystem<Composite> thumbnailPlot;

	private String path;

	public ARPESFilePreview() {
		try {
			thumbnailPlot = PlottingFactory.createPlottingSystem();
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
	}

	@PostConstruct
	public Composite createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.horizontalSpacing = 2;
		composite_1.setLayout(gl_composite_1);

		Label lblSelectedFile = new Label(composite_1, SWT.NONE);
		lblSelectedFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSelectedFile.setText("Selected File");

		txtSelectedFile = new Text(composite_1, SWT.BORDER);
		txtSelectedFile.setText("Selected File");
		txtSelectedFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSelectedFile.setEditable(false);

		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
		btnNewButton_2.setText("...");
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.NONE);
//				dialog.setFilterExtensions(new String[] {".nxs", ".hdf", ".hdf5"});
				dialog.setText("Choose a Nexus file");
				path = dialog.open();
				txtSelectedFile.setText(path);
				plotFile(path);
			}
		});

		Composite composite_2 = new Composite(parent, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		thumbnailPlot.createPlotPart(parent, "Arpes preview", null, PlotType.IMAGE, null);
		thumbnailPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return parent;
	}

	/**
	 * Selection listener
	 * 
	 * @param selection
	 */
	@Inject
	public void selectionChanged(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection) {
		if (selection instanceof IStructuredSelection) {
			Object file = ((IStructuredSelection) selection).getFirstElement();
			String filename = null;
			if (file instanceof IFile) {
				String fileExtension = ((IFile) file).getFileExtension();
				if (fileExtension != null && fileExtension.equals("nxs")) {
					filename = ((IFile) file).getRawLocation().toOSString();
				}
			} else if (file instanceof File) {
				if (!((File) file).isDirectory()) {
					filename = ((File) file).getAbsolutePath();
				}
			}
			if (filename != null) {
				if (isArpesFile(filename, null)) {
					txtSelectedFile.setText(filename);
					plotFile(filename);
				}
			}
		}
	}

	private void plotFile(String filename) {
		try {
			IDataHolder data = LoaderFactory.getData(filename);
			Map<String, ILazyDataset> map = data.toLazyMap();
			ILazyDataset value = map.get(ARPESCalibrationConstants.DATA_NODE);
			value = value.getSlice(new Slice(1)).squeeze();
			ILazyDataset energies = map.get(ARPESCalibrationConstants.ENERGY_NODE);
			ILazyDataset angles = map.get(ARPESCalibrationConstants.ANGLE_NODE);
			thumbnailPlot.clear();
			if (value.getShape().length == 2) {
				Dataset image = DatasetUtils.sliceAndConvertLazyDataset(value);
				ArrayList<IDataset> axes = new ArrayList<IDataset>(2);
				if (energies == null) {
					axes.add(null);
				} else {
					axes.add(DatasetUtils.sliceAndConvertLazyDataset(energies));
				}
				if (angles == null) {
					axes.add(null);
				} else {
					axes.add(DatasetUtils.sliceAndConvertLazyDataset(angles));
				}
				thumbnailPlot.updatePlot2D(image, axes, null);
			} else {
				logger.warn("Dataset not the right shape for showing in the preview");
			}
		} catch (Exception e) {
			logger.error("Something went wrong when creating a overview plot", e);
		}
	}

	public static boolean isArpesFile(String filename, IMonitor monitor) {
		
		// Reading the whole file is inefficient. Although for ARPES files
		// this may not be too bad - there are other HDF5 files which DAWN supports!
		// These grind to a halt as expanding the tree forces them to read all their 
		// data. Therefore we do NOT USE LoaderFactory to get a complete DataHolder.
		// Instead we do something cheap and fast.
		ILoaderService service = Activator.getService(ILoaderService.class);
		try {
			IDataHolder holder = service.getData(filename, true, monitor);
			ILazyDataset lazy = holder.getLazyDataset(ARPESCalibrationConstants.DATA_NODE);
			return lazy!=null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	@PreDestroy
	public void dispose() {
		// E4 takes care of removing the listeners?
	}
}
