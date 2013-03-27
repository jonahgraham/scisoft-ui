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

package uk.ac.diamond.scisoft.analysis.rcp.util;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;

public class PlotUtils {

	private static final Logger logger = LoggerFactory.getLogger(PlotUtils.class);
	
	
	/**
	 * Thread safe
	 * @param plotWindow 
	 * @param monitor 
	 * @param plotMode 
	 * @param xDataSet 
	 * @param yDataSets 
	 */
	public static void create1DPlot(final AbstractDataset         xDataSet, 
			                        final List<AbstractDataset>   yDataSets, 
			                        final PlotMode         plotMode, 
			                        final PlotWindow       plotWindow, 
			                        final IProgressMonitor monitor) {
		
		if (xDataSet.getRank() != 1) return;

		// We allow yDataSets to be null if they like.
		final AbstractDataset x;
		final List<AbstractDataset> ys;
		if (yDataSets==null) {
			ys = new ArrayList<AbstractDataset>(1);
			ys.add(xDataSet);
			x = DoubleDataset.arange(ys.get(0).getSize());
		} else {
			x  = xDataSet;
			ys = yDataSets;
		}
		
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display.isDisposed()) return;
		
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					plotWindow.updatePlotMode(plotMode.getGuiPlotMode(), false);

					if (monitor!=null&&monitor.isCanceled()) return;
					
					// generate the bean to send the Gui information
					GuiBean guiBean = new GuiBean();
					guiBean.put(GuiParameters.PLOTMODE, plotMode.getGuiPlotMode());
					guiBean.put(GuiParameters.TITLE,    getTitle(x, ys, true));
					plotWindow.processGUIUpdate(guiBean);
					if (monitor!=null&&monitor.isCanceled()) return;

					DataBean dataBean = new DataBean(plotMode.getGuiPlotMode());
					dataBean.addAxis(AxisMapBean.XAXIS, x);
					
					// TODO use PM3D for z, currently hard codes something, in process of fixing.
					if (PlotMode.PM3D==plotMode) {
						final AbstractDataset z = new DoubleDataset(new double[]{-15,1,200});
						dataBean.addAxis(AxisMapBean.ZAXIS, z);
					}

					for (int i = 0; i < ys.size(); i++) {
						// now add it to the plot data
						try {
							dataBean.addData(DataSetWithAxisInformation.createAxisDataSet(ys.get(i)));
							if (monitor!=null&&monitor.isCanceled()) return;
						} catch (DataBeanException e) {
							logger.error("Problem adding data to bean as axis key does not exist", e);
						}
					}

					if (monitor!=null&&monitor.isCanceled()) return;
					plotWindow.processPlotUpdate(dataBean);
					
				} catch (Exception ne) {
					logger.error("Cannot create plot required.", ne);
				}
			}
			
		});
	}
	
	/**
	 * Attempts to create a plot with the data passed in.
	 * 
	 * Thread safe.
	 * 
	 * @param data
	 * @param axes
	 * @param mode
	 * @param plotWindow
	 * @param monitor
	 */
	public static void createPlot(final AbstractDataset       data,
			                      final List<AbstractDataset> axes,
			                      final GuiPlotMode           mode, 
			                      final PlotWindow            plotWindow, 
			                      final IProgressMonitor monitor) {
		
		
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display.isDisposed()) return;
		
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					plotWindow.updatePlotMode(mode, false);

					if (monitor!=null&&monitor.isCanceled()) return;
					
					// generate the bean to send the Gui information
					GuiBean guiBean = new GuiBean();
					guiBean.put(GuiParameters.PLOTMODE, mode);
					guiBean.put(GuiParameters.TITLE,    data.getName());
					plotWindow.processGUIUpdate(guiBean);
					if (monitor!=null&&monitor.isCanceled()) return;

					DataBean dataBean = new DataBean(mode);
					DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
					AxisMapBean axisMapBean = new AxisMapBean(AxisMapBean.DIRECT);
					
					dataBean.addAxis(AxisMapBean.XAXIS, axes.get(0));
					dataBean.addAxis(AxisMapBean.YAXIS, axes.get(1));
					// note that the DataSet plotter's 2D mode is row-major
					axisData.setData(DatasetUtils.transpose(data, new int[] {1, 0}));
					axisData.setAxisMap(axisMapBean);

					dataBean.addData(axisData);

					if (monitor!=null&&monitor.isCanceled()) return;
					plotWindow.processPlotUpdate(dataBean);
					
				} catch (Exception ne) {
					logger.error("Cannot create plot required.", ne);
				}
			}
			
		});
	}


	private static Serializable getTitle(AbstractDataset x, List<AbstractDataset> ys, final boolean isFileName) {
		
		final StringBuilder buf = new StringBuilder();
		buf.append("Plot of");
		final Set<String> used = new HashSet<String>(7);
		for (IDataset dataSet : ys) {
			String name = dataSet.getName();
			
			if (isFileName) {
			    // Strip off file name
				final Matcher matcher = Pattern.compile("(.*) \\(.*\\)").matcher(name);
				if (matcher.matches()) name = matcher.group(1);
			}
			
			if (used.contains(name)) continue;
			used.add(name);
			buf.append(" ");
			buf.append(name);
			buf.append(",");
		}
		final int index = buf.length()-1;
		buf.delete(index, index+1);
		buf.append(" against ");
		buf.append(x.getName());
		return buf.toString();
	}



}
