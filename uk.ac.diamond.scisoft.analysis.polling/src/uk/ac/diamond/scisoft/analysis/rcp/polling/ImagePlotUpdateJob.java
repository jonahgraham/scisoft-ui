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

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.sda.polling.jobs.FilenameReaderUpdateOnlyJob;

public class ImagePlotUpdateJob extends FilenameReaderUpdateOnlyJob {

	public static final String PLOT_VIEW_NAME = "PlotViewName";
	
	public ImagePlotUpdateJob() {
		super();
	}

	@Override
	protected void processFile(ArrayList<String> filenames) {
		// this one will simply plot the last image in the list
		
		try {	
			SDAPlotter.imagePlot(getJobParameters().get(PLOT_VIEW_NAME), filenames.get(filenames.size()-1));		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
