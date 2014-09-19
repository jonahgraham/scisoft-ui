/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.polling;

import java.util.ArrayList;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
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
