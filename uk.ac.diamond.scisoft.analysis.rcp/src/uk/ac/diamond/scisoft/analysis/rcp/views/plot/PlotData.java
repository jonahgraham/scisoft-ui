/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;

/**
 * Class to wrap vertex data in Lists of Doubles (as they are generated)
 * They are only translated to DataSets as needed, since this creates an
 * extra double array.
 */
public class PlotData implements IPlotData {

	private Map<String,List<Double>>   data;
	
	/**
	 * Main constructor
	 */
	public PlotData() {
		this.data = new LinkedHashMap<String, List<Double>>();
	}
	
	/**
	 * Used in testing
	 * @param data 
	 */
	public PlotData(Map<String,List<Double>>   data) {
		this.data = data;
	}
	/**
	 * Convenience constructor
	 * @param name
	 * @param data
	 */
	public PlotData(final String name, final List<Double> data) {
		this();
		this.data.put(name, data);
	}

	/**
	 * @return Returns the data.
	 */
//	@Override
	public List<Double> getData() {
		if (data.size()>1) throw new RuntimeException("Multiple Data - cannot determine which data in the getData() method.");
		return data.values().iterator().next();
	}
	
	/**
	 * 
	 * @return d
	 */
	@Override
	public Map<String,Dataset> getDataMap() {
		final Map<String,Dataset> ret = new LinkedHashMap<String,Dataset>(data.size());
		for (String name : data.keySet()) {
			final List<Double> list = data.get(name);
			if (!validateData(list)) continue;
			ret.put(name, DatasetFactory.createFromList(list));
		}
		return ret;
	}
	
	public static boolean validateData(List<Double> list) {
		if (list.indexOf(null)>-1)       return false;
		if (list.indexOf(Double.NaN)>-1) return false;
		if (list.indexOf(Double.NEGATIVE_INFINITY)>-1) return false;
		if (list.indexOf(Double.POSITIVE_INFINITY)>-1) return false;
		return true;
	}
	
	public static boolean validateData(double [] list) {
		for (int i = 0; i < list.length; i++) {
			if (Double.isNaN(list[i]))      return false;
			if (Double.isInfinite(list[i])) return false;
		}
		return true;
	}

	/**
	 * Adds data for several points.
	 * @param name
	 * @param data
	 */
	public void addData(final String name, final List<Double> data) {
		this.data.put(name, data);
	}

	/**
	 * Adds data for one point.
	 * @param name
	 * @param point
	 */
	public void addData(String name, Double point) {
		if (point==null) {
			data.remove(name);
			return;
		}
		List<Double> d = data.get(name);
		if (d == null) {
			d = new ArrayList<Double>(89);
			data.put(name, d);
		}
		d.add(point);
	}

	/**
	 * @return Returns the isMulti.
	 */
	@Override
	public boolean isMulti() {
		return (data.size()>1);
	}
	
	/**
	 * 
	 * @return size
	 */
	@Override
	public int size() {
		return data.values().iterator().next().size();
	}

	/**
	 * 
	 * @return s
	 */
	@Override
	public Dataset getDataSet() {
		return DatasetFactory.createFromList(getData());
	}

	/**
	 * 
	 * @return s
	 */
	@Override
	public List<Dataset> getDataSets() {
		final List<Dataset> sets = new ArrayList<Dataset>(this.data.size());
		for (List<Double> s : data.values()) sets.add(DatasetFactory.createFromList(s));
		return sets;
	}

	/**
	 * Removes any data
	 */
	@Override
	public void clear() {
		data.clear();
	}
	
	/**
	 * Deep copy of entire data.
	 */
	@Override
	public PlotData clone() {
		final PlotData ret = new PlotData();
		final Map<String,List<Double>> copy = new LinkedHashMap<String,List<Double>>(data.size());
		for (String name : data.keySet()) {
			copy.put(name, new ArrayList<Double>(data.get(name)));
		}
		ret.data = copy;
		return ret;
	}

	/** 
	 * @return true if data has no NaNs or infinities
	 */
	@Override
	public boolean isDataSetValid() {
		return validateData(getData());
	}

	/**
	 * @return true if data has no NaNs or infinities
	 */
	@Override
	public boolean isDataSetsValid() {
		for (List<Double> s : data.values()) if (validateData(s)) return false;
		return true;
	}
}
