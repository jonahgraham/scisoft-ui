/*-
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.dawb.common.ui.util.ColorUtility;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Class to create a 1D plotting
 * 
 */
public class Plotting1DUI extends AbstractPlotUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting1DUI";
	private static final Logger logger = LoggerFactory.getLogger(Plotting1DUI.class);
	private static final int LEGEND_LIMIT = 5; // maximum number of lines for legend otherwise it is not shown

	private IPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Color idx
	 */
	private int idx;
	/**
	 * trace color
	 */
	private Color plotColor;

	/**
	 * Constructor of a plotting 1D 
	 * @param plottingSystem plotting system
	 */
	public Plotting1DUI(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
		idx = 0;
	}

	@Override
	public void processPlotUpdate(final DataBean dbPlot, boolean isUpdate) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<DataSetWithAxisInformation> plotData = dbPlot.getData();
				if (plotData == null)
					return;

				GuiBean gb = dbPlot.getGuiParameters();
				String title = gb == null ? null : (String) gb.get(GuiParameters.TITLE);
				boolean hasTitle = title != null;

				String plotOperation = gb == null ? null : (String) gb.get(GuiParameters.PLOTOPERATION);

				// If in ADD plot operation mode
				if (plotOperation != null && plotOperation.equals(GuiParameters.PLOTOP_ADD)) {
					// increment the color
					int index = plottingSystem.getTraces().size();
					if(idx > ColorUtility.getSize()) idx = 0;
					if (index > ColorUtility.getSize()) {
						plotColor = ColorUtility.getSwtColour(idx++);
					} else {
						plotColor = ColorUtility.getSwtColour(index);
					}

					List<IDataset> yDatasets = Collections.synchronizedList(new LinkedList<IDataset>());
					AbstractDataset nx = dbPlot.getAxis(AxisMapBean.XAXIS);
					int i = 0;
					for (DataSetWithAxisInformation d : plotData) {
						AbstractDataset ny = d.getData();
						String nyn = ny.getName();
						// set a name to the data if none
						if (nyn == null || nyn.equals("")) {
							ny.setName("Plot " + index);
						}
						yDatasets.add(ny);
						if (!hasTitle) {
							if (i > 0 && i <= 2) {
								title += ", " + nyn;
							} else if (i == 3) {
								title += "...";
							} else if (i == 0) {
								title = nyn;
							}
						}
						i++;
					}

					// Only blank primary axis title
					IAxis yAxis = plottingSystem.getSelectedYAxis();
					if (yAxis.isPrimaryAxis())
						yAxis.setTitle("");

					if (!hasTitle) { // TODO fix plot title by including previous plots
						if (title == null) {
							title = "";
						} else if (!title.equals("")) {
							String nxn = nx.getName();
							if (nxn != null && !nxn.equals(""))
								title = "Plot of " + title + " against " + nx.getName();
							else
								title = "Plot of " + title;
						}
					}

					Collection<ITrace> newTraces = plottingSystem.createPlot1D(nx, yDatasets, title, null);
					for (ITrace iTrace : newTraces) {
						final ILineTrace lineTrace = (ILineTrace)iTrace;
						lineTrace.setTraceType(TraceType.SOLID_LINE);
						if(plotColor != null)
							lineTrace.setTraceColor(plotColor);
					}

					logger.debug("Plot 1D created");
				} 
				// if in UPDATE or NONE mode
				else {

					// if more than one plot to show then do not show legend
					if (plotData.size() > LEGEND_LIMIT) {
						plottingSystem.setShowLegend(false);
					}

					// check if same lines are being plotted
					boolean useOldTraces = false;
					final int plots = plotData.size();
					Collection<ITrace> oldTraces = plottingSystem.getTraces();
					final int traces = oldTraces.size();
					if (plots <= traces) {
						int nt = 0;
						for (ITrace t : oldTraces) {
							if (t instanceof ILineTrace) {
								String oyn = t.getName();
								AbstractDataset ox = (AbstractDataset)((ILineTrace) t).getXData();
								String oxn = ox == null ? null : ox.getName();
								for (DataSetWithAxisInformation d : plotData) {
									String nyn = d.getData().getName();
									String nxn = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]).getName();
									if (oyn != null && oyn.equals(nyn)) {
										if (oxn != null && oxn.equals(nxn)) {
											nt++;
											break;
										}
									}
								}
							}
						}
						useOldTraces = nt == plots;
					}
					if (useOldTraces) {
						List<ITrace> unused = new ArrayList<ITrace>();
						for (ITrace t : oldTraces) {
							if (t instanceof ILineTrace) {
								boolean used = false;
								String oyn = t.getName();
								AbstractDataset x = (AbstractDataset)((ILineTrace) t).getXData();
								String oxn = x == null ? null : x.getName();
								for (DataSetWithAxisInformation d : plotData) {
									AbstractDataset ny = d.getData();
									String nyn = ny.getName();
									AbstractDataset nx = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]);
									String nxn = nx.getName();
									if (oyn != null && oyn.equals(nyn)) {
										if (oxn != null && oxn.equals(nxn)) {
											((ILineTrace) t).setData(nx, ny);
											((ILineTrace) t).repaint();
											used = true;
											break;
										}
									}
								}
								if (!used)
									unused.add(t);
							}
						}
						for (ITrace t : unused) {
							plottingSystem.removeTrace(t);
						}
						// if rescale axis option is checked in the x/y plot menu
						if (plottingSystem.isRescale())
							plottingSystem.autoscaleAxes();
						logger.debug("Plot 1D updated");
					} else {
						// increment the color
						int index = plottingSystem.getTraces().size();
						if(idx > ColorUtility.getSize()) idx = 0;
						if (index > ColorUtility.getSize()) {
							plotColor = ColorUtility.getSwtColour(idx);
						} else {
							plotColor = ColorUtility.getSwtColour(index);
						}
						idx++;
						
						List<IDataset> yDatasets = Collections.synchronizedList(new LinkedList<IDataset>());
						AbstractDataset nx = dbPlot.getAxis(AxisMapBean.XAXIS);
						int i = 0;
						for (DataSetWithAxisInformation d : plotData) {
							AbstractDataset ny = d.getData();
							String nyn = ny.getName();
							// set a name to the data if none
							if(nyn == null || nyn.equals("")){
								ny.setName("Plot "+i);
							}
							yDatasets.add(ny);
							if (!hasTitle) {
								if (i > 0 && i <= 2) {
									title += ", " + nyn;
								} else if (i == 3) {
									title += "...";
								} else if (i == 0) {
									title = nyn;
								}
							}
							i++;
						}
						plottingSystem.getSelectedYAxis().setTitle("");
						plottingSystem.clear();

						if (!hasTitle) {
							if (title == null) {
								title = "";
							} else if (!title.equals("")) {
								String nxn = nx.getName();
								if (nxn != null && !nxn.equals(""))
									title = "Plot of " + title + " against " + nx.getName();
								else
									title = "Plot of " + title;
							}
						}
						Collection<ITrace> newTraces = plottingSystem.createPlot1D(nx, yDatasets, title, null);

						for (ITrace iTrace : newTraces) {
							final ILineTrace lineTrace = (ILineTrace)iTrace;
							lineTrace.setTraceType(TraceType.SOLID_LINE);
						}
						logger.debug("Plot 1D created");
					}
				}
				
				
			}
		});
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.removeAll(observers);
	}
}
