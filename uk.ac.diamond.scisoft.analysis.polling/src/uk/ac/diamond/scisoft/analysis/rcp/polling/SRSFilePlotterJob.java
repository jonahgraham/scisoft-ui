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
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.CompoundDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.SRSLoader;
import uk.ac.diamond.sda.polling.jobs.FilenameReaderJob;

public class SRSFilePlotterJob extends FilenameReaderJob {

	public static final String PLOT_VIEW_NAME = "PlotViewName";
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		// only process the last given file
		SRSLoader srsLoader = new SRSLoader(filenames.get(filenames.size()-1));
		try {
			DataHolder holder = srsLoader.loadFile();
			
			String[] dataPlotNames = getJobParameters().get("YAxis").split(",");
						
			ArrayList<CompoundDataset> list = new ArrayList<CompoundDataset>();
		
			for (String name : dataPlotNames) {				
				
				AbstractDataset[] acd = new AbstractDataset[] { holder.getDataset(getJobParameters().get("XAxis")), holder.getDataset(name.trim()) };
				CompoundDataset cdd = DatasetUtils.cast(acd, acd[0].getDtype());
				list.add(cdd);			
			}
			
			int[] sizes = new int[list.size()];
			for (int i = 0 ; i < sizes.length; i++) {
				sizes[i] = 5;
			}
			
			SDAPlotter.scatter2DPlot(getJobParameters().get(PLOT_VIEW_NAME),
					list.toArray(new CompoundDataset[0]),
					sizes);
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
