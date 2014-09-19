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
import uk.ac.diamond.sda.polling.jobs.FilenameReaderJob;

public class ImagePlotCycleJob extends FilenameReaderJob {

	private static final String MAX_IMAGES_TO_CYCLE = "MaxImagesToCycle";
	private static final Object PLOT_VIEW_NAME = "PlotViewName";
	private int cycle = 0;

	@Override
	protected void processFile(ArrayList<String> filenames) {
		try {	
			// get the end of the list
			int listEnd = filenames.size()-1;
			// check to make sure the cyclepoint is valid
			if((listEnd-cycle < 0) || cycle > Integer.parseInt(getJobParameters().get(MAX_IMAGES_TO_CYCLE))) {
				// otherwise reset it to zero
				cycle = 0;
			}
			SDAPlotter.imagePlot(getJobParameters().get(PLOT_VIEW_NAME), filenames.get(listEnd-cycle));	
			cycle++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	


}
