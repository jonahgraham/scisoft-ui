/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;

public interface IPlotData {

	public IPlotData clone();

	public void clear();

	public int size();

	public boolean isDataSetValid();

	public boolean isMulti();

	public Map<String, ? extends Dataset> getDataMap();

	public Dataset getDataSet();

	public boolean isDataSetsValid();

	public List<Dataset> getDataSets();

}
