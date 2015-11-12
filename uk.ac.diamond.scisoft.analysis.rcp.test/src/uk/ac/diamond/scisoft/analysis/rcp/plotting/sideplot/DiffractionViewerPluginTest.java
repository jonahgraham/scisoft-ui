/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;


import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.TestUtils;
import uk.ac.diamond.scisoft.analysis.io.ADSCImageLoader;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.utils.PluginTestHelpers;

/**
 *
 */
public class DiffractionViewerPluginTest {

	@Test
	public final void testShowView() throws Exception {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		String TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		Dataset data = new ADSCImageLoader(TestFileFolder + "ADSCImageTest/F6_1_001.img").loadFile()
				.getDataset(0);
		
		
		PlotView plotView = (PlotView) window.getActivePage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");
		
		DataBean datab = new DataBean(GuiPlotMode.TWOD);
		DatasetWithAxisInformation dswai = new DatasetWithAxisInformation();
		AxisMapBean amb = new AxisMapBean();
		dswai.setAxisMap(amb);
		dswai.setData(data);
		datab.addData(dswai);
    	plotView.processPlotUpdate(datab);
		PluginTestHelpers.delay(300000); // time to 'play with the graph if wanted
	}

}
