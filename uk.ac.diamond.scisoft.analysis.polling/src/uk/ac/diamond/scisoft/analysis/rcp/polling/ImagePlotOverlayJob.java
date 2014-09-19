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

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.sda.polling.jobs.FilenameReaderUpdateOnlyJob;

public class ImagePlotOverlayJob extends FilenameReaderUpdateOnlyJob {

	private static final Object PLOT_VIEW_NAME = "PlotViewName";
	private static final Object MAX_IMAGES_TO_OVERLAY = "MaxImagesToOverlay";
	
	@Override
	protected void processFile(ArrayList<String> filenames) {
		try {	
			// get the end of the list
			int listEnd = filenames.size()-1;

			ArrayList<IDataset> images = new ArrayList<IDataset>();
			
			int position = 0;
			while (listEnd-position >= 0 && position < Integer.parseInt(getJobParameters().get(MAX_IMAGES_TO_OVERLAY))) {
				IDataHolder data = LoaderFactory.getData(filenames.get(listEnd-position));
				images.add(data.getDataset(0));
				position++;
			}			
				
			SDAPlotter.imagesPlot(getJobParameters().get(PLOT_VIEW_NAME), images.toArray(new IDataset[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}



}
