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
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Quadratic;
import uk.ac.diamond.scisoft.arpes.calibration.functions.FunctionToDataset;
import uk.ac.diamond.scisoft.arpes.calibration.utils.ARPESCalibrationConstants;

public class GoldCalibrationPageFour extends FunctionFittingCalibrationWizardPage {

	public GoldCalibrationPageFour(DataMessageComponent calibrationData) {
		super(calibrationData, "Quadratic Fermi Edge Fitting", "Specify the function to fit the Fermi Edge with");
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setFunction();
			// when page is visible, update plot with correct data, axis and region
			IDataset xaxisData = (IDataset)calibrationData.getList(ARPESCalibrationConstants.ANGLE_AXIS);
			IDataset data = (IDataset)calibrationData.getList(ARPESCalibrationConstants.MU_DATA);
			if (system.getTraces().isEmpty())
				setFitRegion(xaxisData);
			system.updatePlot1D(xaxisData,  Arrays.asList(new IDataset[] { data }), null);
			setPageComplete(true);
		}
		super.setVisible(visible);
	}

	private void setFunction() {
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		Quadratic function = new Quadratic();
		functions.put("_initial_", function);
		functionFittingTool.setFunctions(functions);
	}

	@Override
	public boolean runProcess() throws InterruptedException {
		System.out.println("Page 4");
		return true;
	}

	@Override
	public int getPageNumber() {
		return 4;
	}

	/**
	 * To override if necessary
	 * @param function
	 */
	@Override
	protected void saveFittedMuData(IFunction function) {
		IDataset seedData = (IDataset) calibrationData.getList(ARPESCalibrationConstants.MU_DATA);
		IDataset seedAxisData = (IDataset) calibrationData.getList(ARPESCalibrationConstants.YAXIS_DATANAME);
		FunctionToDataset fToData = new FunctionToDataset(function, seedData, seedAxisData);
		IDataset functionDataset = fToData.getDataset();
		calibrationData.addList(ARPESCalibrationConstants.FUNCTION_FITTEDMU_DATA, functionDataset);
	}

	@Override
	public String getFunctionName() {
		return ARPESCalibrationConstants.FUNCTION_FITTEDMU;
	}
}