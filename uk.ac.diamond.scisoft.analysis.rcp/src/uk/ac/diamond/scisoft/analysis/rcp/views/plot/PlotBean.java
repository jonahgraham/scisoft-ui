/*
 * Copyright 2012 Diamond Light Source Ltd. Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.util.LinkedHashMap;
import java.util.Map;

import org.dawb.common.util.list.PrimitiveArrayEncoder;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Stores information required to restore a plot. Should serialise to XML easily. Resolves plot data to primitive and
 * serializable types. NOTE: Does not work with large datasets. Perhaps should compress data or better still, just write
 * the bean as instructions to load the data in from the data folder instead of reproducing it. Another alternative is
 * to save less points for the graph which would give unexpected results when zooming in.
 */
public class PlotBean {

	private String secondId;
	private String currentPlotName;
	private String partName;
	// NOTE name<->data mapping where data is string as it serialises smaller.
	private Map<String, String> data;
	private String xAxisValues;
	private String xAxis, yAxis;
	private int xAxisMode, yAxisMode;

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}
	
	/**
	 * Convenience method for transferring the double[]'s to DataSets.
	 * 
	 * @return h
	 */
	public Map<String, ? extends Dataset> getDataSets() {
		if (data == null)
			return null;
		final Map<String, Dataset> ret = new LinkedHashMap<String, Dataset>(data.size());
		for (String name : data.keySet()) {
			ret.put(name, new DoubleDataset(PrimitiveArrayEncoder.getDoubleArray(data.get(name))));
		}
		return ret;
	}

	public void setDataSets(final Map<String, ? extends Dataset> ds) {
		if (data == null)
			data = new LinkedHashMap<String, String>(ds.size());
		data.clear();
		for (String name : ds.keySet()) {
			data.put(name, PrimitiveArrayEncoder.getString(new DoubleDataset(ds.get(name)).getData()));
		}
	}
	

	public String getXAxisValues() {
		return xAxisValues;
	}

	public void setXAxisValues(String axisValues) {
		xAxisValues = axisValues;
	}

	public void setXAxisValues(AxisValues values) {
		this.xAxisValues = values.getValues().toString();
	}

	public String getXAxis() {
		return xAxis;
	}

	public void setXAxis(String axis) {
		xAxis = axis;
	}

	public String getYAxis() {
		return yAxis;
	}

	public void setYAxis(String axis) {
		yAxis = axis;
	}

	public int getXAxisMode() {
		return xAxisMode;
	}

	public void setXAxisMode(int axisMode) {
		xAxisMode = axisMode;
	}

	public int getYAxisMode() {
		return yAxisMode;
	}

	public void setYAxisMode(int axisMode) {
		yAxisMode = axisMode;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getCurrentPlotName() {
		return currentPlotName;
	}

	public void setCurrentPlotName(String currentPlotName) {
		this.currentPlotName = currentPlotName;
	}

	public String getSecondId() {
		return secondId;
	}

	public void setSecondId(String secondId) {
		this.secondId = secondId;
	}
}
