/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.functions;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IDataBasedFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;

public class FunctionToDataset {

	private IFunction function;
	private IDataset seedData;
	private IDataset seedAxisData;

	public FunctionToDataset(IFunction function, IDataset seedData, IDataset seedAxisData) {
		this.function = function;
		this.seedData = seedData;
		this.seedAxisData = seedAxisData;
	}

	public IDataset getDataset() {

		populateDataBasedFunctions(function);

		// process the data
		// TODO Add Null Protection here.
		IDataset createdDS = function.calculateValues(seedAxisData);
		createdDS.setName(function.getName());

		return createdDS;
	}

	private void populateDataBasedFunctions(IFunction function) {

		if (function instanceof CompositeFunction) {
			CompositeFunction compositeFunction = (CompositeFunction) function;
			for (IFunction func : compositeFunction.getFunctions()) {
				populateDataBasedFunctions(func);
			}
		}

		if (function instanceof IDataBasedFunction) {
			IDataBasedFunction dbFunction = (IDataBasedFunction) function;
			dbFunction.setData(seedAxisData, seedData);
		}
	}
}
