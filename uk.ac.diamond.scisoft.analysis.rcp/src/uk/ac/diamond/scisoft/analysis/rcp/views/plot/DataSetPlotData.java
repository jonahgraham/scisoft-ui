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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;

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
