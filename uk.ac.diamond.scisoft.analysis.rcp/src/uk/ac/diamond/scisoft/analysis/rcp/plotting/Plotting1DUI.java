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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
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

	private AbstractPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Constructor of a plotting 1D 
	 * @param plottingSystem plotting system
	 */
	public Plotting1DUI(AbstractPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
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
							AbstractDataset ox = ((ILineTrace) t).getXData();
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
					for (ITrace t : oldTraces) {
						if (t instanceof ILineTrace) {
							String oyn = t.getName();
							AbstractDataset x = ((ILineTrace) t).getXData();
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
									}
								}
							}
						}
					}
					// if rescale axis option is checked in the x/y plot menu
					if (plottingSystem.isRescale())
						plottingSystem.autoscaleAxes();
					logger.debug("Plot 1D updated");
				} else {
					List<AbstractDataset> yDatasets = Collections.synchronizedList(new LinkedList<AbstractDataset>());
					AbstractDataset nx = dbPlot.getAxis(AxisMapBean.XAXIS);
					int i = 0;
					for (DataSetWithAxisInformation d : plotData) {
						AbstractDataset ny = d.getData();
						String nyn = ny.getName();
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
					
					Collection<ITrace> newTraces = plottingSystem.createPlot1D(nx, yDatasets, hasTitle ? title : "Plot of " + title + " against " + nx.getName(), null);

					for (ITrace iTrace : newTraces) {
						final ILineTrace lineTrace = (ILineTrace)iTrace;
						lineTrace.setTraceType(TraceType.SOLID_LINE);
					}
					logger.debug("Plot 1D created");
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
