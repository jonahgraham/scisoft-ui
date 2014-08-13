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

package uk.ac.diamond.scisoft.analysis.rcp.polling;

import java.util.ArrayList;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.SRSLoader;
import uk.ac.diamond.sda.polling.jobs.FilenameReaderJob;

public class SRSlinePlotJob extends FilenameReaderJob {

	public static final String PLOT_VIEW_NAME = "PlotViewName";

	private void swapItems(int a, int b, Dataset dataSet) {
		Object temp = dataSet.getObject(a);
		dataSet.set(dataSet.getObject(b), a);
		dataSet.set(temp, b);
	}
	
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		// only plot the first file
		SRSLoader srsLoader = new SRSLoader(filenames.get(filenames.size()-1));
		try {
			DataHolder holder = srsLoader.loadFile();
			
			// get all the data
			String[] dataPlotNames = getJobParameters().get("YAxis").split(",");
					
			Dataset xAxis = holder.getDataset(getJobParameters().get("XAxis"));			
			
			ArrayList<Dataset> list = new ArrayList<Dataset>();
		
			for (String name : dataPlotNames) {					
				list.add(holder.getDataset(name));
			}
			
			// order the data, simple sorting routine
			boolean sorted = false;
			while(!sorted) {
				
				sorted = true;
				
				for(int i = 0; i < xAxis.getShape()[0]-1; i++) {
					
					if(xAxis.getDouble(i) > xAxis.getDouble(i+1)) {
						sorted = false;
						
						swapItems(i, i+1, xAxis);
						for (Dataset abstractDataset : list) {
							swapItems(i, i+1, abstractDataset);
						}						
					}					
				}
			}
			
			
			// plot the results
			SDAPlotter.plot(getJobParameters().get(PLOT_VIEW_NAME),
					xAxis,list.toArray(new Dataset[0]));			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
