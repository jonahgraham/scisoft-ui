/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

public class DataSetPlotData implements IPlotData {

	private Map<String, Dataset> data;

	private DataSetPlotData() {
		this.data = new HashMap<String,Dataset>(3);
	}
	
	public DataSetPlotData(String name, Dataset value) {
		this();
		data.put(name, value);
	}


	@Override
	public IPlotData clone() {
		final DataSetPlotData copy = new DataSetPlotData();
		for (String name : data.keySet()) {
			copy.data.put(name, data.get(name).clone());
		}
		return copy;
	}
	
	@Override
	public void clear() {
		data.clear();
	}

//	@Override
//	public List<Double> getData() {
// 	    final double[] da = getDataSet().getData();
// 	    final List<Double> ret = new ArrayList<Double>(da.length);
// 	    for (int i = 0; i < da.length; i++) {
// 	    	ret.add(da[i]);
//		}
// 	    return ret;
//	}

	@Override
	public Map<String, Dataset> getDataMap() {
		return data;
	}

	@Override
	public Dataset getDataSet() {
		return data.values().iterator().next();
	}

	@Override
	public List<Dataset> getDataSets() {
		Collection<Dataset> values = data.values();
		List<Dataset> vector = new Vector<Dataset>();
		
		for(Dataset value : values){
			vector.add(value);
		}
		
		return vector;
	}

	@Override
	public boolean isDataSetValid() {
		for (String name : data.keySet()) {
			final Dataset set = data.get(name);
			if (set.containsInfs()) return false;
			if (set.containsNans()) return false;
		}
		return true;
	}

	@Override
	public boolean isDataSetsValid() {
		return true;
	}

	@Override
	public boolean isMulti() {
		return data.size()>1;
	}

	@Override
	public int size() {
		return data.values().iterator().next().getSize();
	}

}
