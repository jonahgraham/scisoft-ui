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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import gda.util.TestUtils;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.io.ADSCImageLoader;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
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
		PlotServer plotServer = PlotServerProvider.getPlotServer();
		GuiBean guiState = plotServer.getGuiState("Plot 1");
		if (guiState == null) {
			guiState = new GuiBean();
		}
		
		DataBean datab = new DataBean();
		
		DataSetWithAxisInformation dswai = new DataSetWithAxisInformation();
		AxisMapBean amb = new AxisMapBean();
		dswai.setAxisMap(amb);
		dswai.setData(data);
		datab.addData(dswai);
		guiState.put(GuiParameters.PLOTMODE, GuiPlotMode.TWOD);
		plotView.processGUIUpdate(guiState);
    	plotView.processPlotUpdate(datab);
		PluginTestHelpers.delay(300000); // time to 'play with the graph if wanted
	}

}
