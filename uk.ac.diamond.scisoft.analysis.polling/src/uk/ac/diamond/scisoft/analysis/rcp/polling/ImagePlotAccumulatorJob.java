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
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.sda.polling.jobs.FilenameReaderUpdateOnlyJob;

public class ImagePlotAccumulatorJob extends FilenameReaderUpdateOnlyJob {

	private static final Object PLOT_VIEW_NAME = "PlotViewName";
	private static final Object MAX_IMAGES_TO_ACCUMULATE = "MaxImagesToAccumulate";
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		try {	
			// get the end of the list
			int listEnd = filenames.size()-1;

			ArrayList<IDataset> images = new ArrayList<IDataset>();
			
			int position = 0;
			while (listEnd-position >= 0 && position < Integer.parseInt(getJobParameters().get(MAX_IMAGES_TO_ACCUMULATE))) {
				IDataHolder data = LoaderFactory.getData(filenames.get(listEnd-position));
				images.add(data.getDataset(0));
				position++;
			}			
				
			Dataset accumulator =  DatasetFactory.zeros((Dataset) images.get(0), Dataset.ARRAYFLOAT64);
			for(int i = 0; i < images.size(); i++) {
				accumulator.iadd(images.get(i));
			}
			
			SDAPlotter.imagePlot(getJobParameters().get(PLOT_VIEW_NAME), accumulator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
