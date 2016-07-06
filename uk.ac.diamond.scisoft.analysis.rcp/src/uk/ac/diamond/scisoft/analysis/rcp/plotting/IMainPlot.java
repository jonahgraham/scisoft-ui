/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;


import java.util.List;

import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;


public interface IMainPlot {

	/**
	 * Returns the current DataSet from the main plot
	 * @return current dataset
	 */
	public abstract IDataset getCurrentDataSet();

	/**
	 * 
	 * @return a list of Datasets, may be null
	 */
			
	public abstract List<IDataset> getCurrentDataSets();

	/**
	 * 
	 * @return list of x data values, may be null
	 */
	public abstract List<AxisValues> getXAxisValues();

	/**
	 * Returns true of the plotted is disposed and no
	 * longer available for plotting.
	 * @return true if disposed.
	 */
	public abstract boolean isDisposed();

}
