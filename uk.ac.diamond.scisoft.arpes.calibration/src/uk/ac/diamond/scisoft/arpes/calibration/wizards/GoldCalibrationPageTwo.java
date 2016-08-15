/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.message.DataMessageComponent;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.arpes.calibration.functions.PrepareFermiGaussianFunction;
import uk.ac.diamond.scisoft.arpes.calibration.utils.ARPESCalibrationConstants;

public class GoldCalibrationPageTwo extends FunctionFittingCalibrationWizardPage {

	public GoldCalibrationPageTwo(DataMessageComponent calibrationData) {
		super(calibrationData, "Fermi Fitting", "Set up the Fermi function fitting");
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setFunction();
			// when page is visible, update plot with correct data, axis and region
			IDataset xaxisData = (IDataset)calibrationData.getList(ARPESCalibrationConstants.ENERGY_AXIS);
			IDataset data = (IDataset)calibrationData.getList(ARPESCalibrationConstants.AVERAGE_DATANAME);
			if (system.getTraces().isEmpty())
				setFitRegion(xaxisData);
			system.updatePlot1D(xaxisData,  Arrays.asList(new IDataset[] { data }), null);
			setMean();
			setPageComplete(true);
		}
		super.setVisible(visible);
	}

	/**
	 * Set mean dataset based on regionDataset
	 */
	private void setMean() {
		IDataset iregionDataset = (IDataset)calibrationData.getList(ARPESCalibrationConstants.REGION_DATANAME);
		Dataset regionDataset = DatasetUtils.cast(iregionDataset, DTypeUtils.getDType(iregionDataset));
		Dataset meanDataset = regionDataset.mean(0);
		meanDataset.setName(iregionDataset.getName()+"_mean");
		calibrationData.addList(ARPESCalibrationConstants.MEAN_DATANAME, meanDataset);
	}

	private void setFunction() {
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		PrepareFermiGaussianFunction pFunc = new PrepareFermiGaussianFunction(calibrationData);
		IFunction fermiGaussianFunc = pFunc.getPreparedFunction();
		functions.put("_initial_", fermiGaussianFunc);

		functionFittingTool.setFunctions(functions);
	}

	@Override
	public boolean runProcess() throws InterruptedException {
		System.out.println("Page 2");
		return true;
	}

	@Override
	public int getPageNumber() {
		return 2;
	}

	@Override
	public String getFunctionName() {
		return ARPESCalibrationConstants.FUNCTION_NAME;
	}
}