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
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisOperation;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Class to create a 3D Stack plotting
 * TODO: correctly set axes name
 */
public class Plotting1DStackUI extends AbstractPlotUI {

	public final static String STATUSITEMID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.Plotting1DStackUI";
	private static final Logger logger = LoggerFactory.getLogger(Plotting1DStackUI.class);

	private IPlottingSystem plottingSystem;
	private List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());

	/**
	 * Color idx
	 */
	private int idx;

	/**
	 * Constructor of a plotting 1D 3D stack
	 * @param plottingSystem plotting system
	 */
	public Plotting1DStackUI(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
		idx = 0;
	}

	private static boolean isStringOK(String s) {
		return s != null && s.trim().length() > 0;
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
				Collection<ITrace> oldTraces = plottingSystem.getTraces();
				ILineStackTrace trace = oldTraces.size() != 0 ? (ILineStackTrace)oldTraces.iterator().next() : null;
				IDataset[] stackTraces = trace != null ? trace.getStack() : null;
				int traces = stackTraces != null ? stackTraces.length : 0;
				boolean useOldTraces = false;
				final int plots = plotData.size();
				if (GuiParameters.PLOTOP_NONE.equals(plotOperation) || GuiParameters.PLOTOP_UPDATE.equals(plotOperation)) {

					// check if same lines are being plotted
					if (plots <= traces) {
						useOldTraces = plots == traces;
					}

					if (!useOldTraces) {
						if (trace != null)
					//		plottingSystem.removeTrace(trace);
						traces = 0;
					}
				}

				if (useOldTraces && stackTraces != null && trace != null) {
					List<IDataset> unused = new ArrayList<IDataset>();
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					IDataset[] yDatasets = new IDataset[plotData.size()];
					for (IDataset data : stackTraces) {
						boolean used = false;
						int i = 0;
						for (DataSetWithAxisInformation d : plotData) {
							AbstractDataset ny = d.getData();
							yDatasets[i] = ny;
							AbstractDataset nx = dbPlot.getAxis(d.getAxisMap().getAxisID()[0]);
							xDatasets.add(nx);
							i++;
						}
						
						if (!used)
							unused.add(data);
					}
					trace.setData(xDatasets, yDatasets);
					logger.debug("Plot 1D 3D updated");
				} else {
					
					// increment the colour
					if (idx > ColorUtility.getSize())
						idx = 0;

					List<IAxis> axes = plottingSystem.getAxes();
					Map<String, AbstractDataset> axisData = dbPlot.getAxisData();
					int i = 0; // number of plots
					boolean against = true;
					IAxis firstAxis = null;
					ArrayList<IDataset> yl = new ArrayList<IDataset>();
					String id = plotData.get(0).getAxisMap().getAxisID()[0];
					AbstractDataset nx = axisData.get(id);
					List<IDataset> xDatasets = new ArrayList<IDataset>(1);
					xDatasets.add(nx);
					IDataset[] yDatasets = new IDataset[plotData.size()];
					for (DataSetWithAxisInformation d : plotData) {
						String[] names = d.getAxisMap().getAxisNames();
//						String id = d.getAxisMap().getAxisID()[0];
						String an;
						if (names == null) {
							an = AxisMapBean.XAXIS;
						} else {
							an = names[0];
							if (!isStringOK(an)) {
								an = AxisMapBean.XAXIS;
							}
						}
						
						String n = nx.getName();
						IAxis ax = findAxis(axes, an);
						if (ax == null || ax.isYAxis()) {
							if (isStringOK(n)) {
								an = n; // override axis name with dataset's name
								ax = findAxis(axes, an); // in case of overwrite by plotting system
							}
							if (ax == null || ax.isYAxis()) {
								// help!
								System.err.println("Haven't found x axis " + an);
								ax = plottingSystem.createAxis(an, false, AxisOperation.BOTTOM);
								axes.add(ax);
							}
						}
						plottingSystem.setSelectedXAxis(ax);
						if (!hasTitle) {
							if (firstAxis == null) {
								firstAxis = ax;
							} else if (ax != firstAxis) {
								against = false;
							}
						}

						if (names == null) {
							an = AxisMapBean.YAXIS;
						} else {
							an = names[1];
							if (!isStringOK(an)) {
								an = AxisMapBean.YAXIS;
							}
						}
						IAxis ay = findAxis(axes, an);
						if (ay == null || !ay.isYAxis()) {
							// help!
							System.err.println("Haven't found y axis " + an);
							ay = plottingSystem.createAxis(an, true, AxisOperation.LEFT);
							axes.add(ay);
						}
						plottingSystem.setSelectedYAxis(ay);

						AbstractDataset ny = d.getData();
						yl.add(ny);
						yDatasets[i] = ny;
						String nyn = ny.getName();
						// set a name to the data if none
						if (!isStringOK(nyn)) {
							nyn = "Plot " + i;
							ny.setName(nyn);
						}
						if (!hasTitle) {
							if (i == 0) {
								title = nyn;
							} else if (i < 3) {
								title += ", " + nyn;
							} else if (i == 3) {
								title += "...";
							}
						}
						i++;
					}
					plottingSystem.reset();
					plottingSystem.createPlot1D(nx, yl, null, plots, null);

					if (!hasTitle && isStringOK(title)) {
						title = "Plot of " + title + (against && firstAxis != null ? " against "  + firstAxis.getTitle() : "");
					}
					plottingSystem.setTitle(title);

					logger.debug("Plot 1D 3D created");
				}
			}
		});
	}

	private IAxis findAxis(List<IAxis> axes, String n) {
		if (n == null) {
			return null;
		}
		for (IAxis a : axes) {
			String t = a.getTitle();
			if (n.equals(t)) {
				return a;
			}
		}

		return null;
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
