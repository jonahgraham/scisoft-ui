/*
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.Logger;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.gda.common.rcp.util.GridUtils;

/**
 * A class with a left, right and status. The right contains a plot above the 
 * status:
 *      |	(Left)	|(Right)	Toolbars			|
 *      |			|								|
 *      |			|			Plot				|
 *      |			|								|
 *      |			|_______________________________|
 *      |			|		Status					|
 */
public class SashFormPlotComposite implements PlotView{

	protected final IWorkbenchPart part;
	protected final ScrolledComposite leftScroll, rightScroll;
	protected final Composite left, right;
	protected IPlottingSystem plottingsystem;
	protected final SashForm sashForm;
	protected AbstractDataset[] dataSets;
	protected String xAxisLabel, yAxisLabel;
	private SashForm rightSash;
	private Text statusLabel;
	private IRegion regionOnDisplay;
	private IROIListener regionListener;

	public SashFormPlotComposite(Composite parent, final IWorkbenchPart part, IROIListener regionListener, final IAction... actions) throws Exception {

		this.part = part;
		this.regionListener = regionListener;
		
		this.sashForm = new SashForm(parent, SWT.HORIZONTAL);

		this.leftScroll = new ScrolledComposite(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
		leftScroll.setExpandHorizontal(true);
		leftScroll.setExpandVertical(true);

		this.left = new Composite(leftScroll, SWT.NONE);
		left.setLayout(new GridLayout());

		this.rightScroll = new ScrolledComposite(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
		rightScroll.setExpandHorizontal(true);
		rightScroll.setExpandVertical(true);

		this.rightSash = new SashForm(rightScroll, SWT.VERTICAL);
		
		this.right = new Composite(rightSash, SWT.NONE);
		GridLayout gl_right = new GridLayout(1, false);
		gl_right.marginHeight = 1;
		gl_right.verticalSpacing = 0;
		right.setLayout(gl_right);
		
		
		ActionBarWrapper wrapper = ActionBarWrapper.createActionBars(right,null);

		plottingsystem = PlottingFactory.createPlottingSystem();
		plottingsystem.createPlotPart(right, part.getTitle(), null, PlotType.XY, part);
		plottingsystem.setRescale(true);
		plottingsystem.getPlotActionSystem().fillZoomActions(wrapper.getToolBarManager());
		plottingsystem.getPlotActionSystem().fillPrintActions(wrapper.getToolBarManager());
		plottingsystem.getPlotActionSystem().fillToolActions(wrapper.getToolBarManager(),ToolPageRole.ROLE_1D);
		plottingsystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRegionToDisplay();

		if (actions != null) {
			for (int i = 0; i < actions.length; i++) {
				wrapper.getToolBarManager().add(actions[i]);
			}
		}
		
		wrapper.update(true);
		
		this.statusLabel = new Text(rightSash, SWT.WRAP|SWT.V_SCROLL);
		statusLabel.setEditable(false);
		rightSash.setWeights(new int[]{100,10});
	}
	
	private void createRegionToDisplay() throws Exception {
		this.regionOnDisplay = getPlottingSystem().createRegion("ROI", RegionType.XAXIS);
		regionOnDisplay.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		getPlottingSystem().addRegion(regionOnDisplay);
		regionOnDisplay.setMobile(true);
		regionOnDisplay.addROIListener(regionListener);
	}
		
	public IPlottingSystem getPlottingSystem() {
		return plottingsystem;
	}
	
	public IRegion getRegionOnDisplay() {
		return regionOnDisplay;
	}

	@Override
	public PlotBean getPlotBean() {
		final PlotBean ret = new PlotBean();

		final Map<String, AbstractDataset> d = new HashMap<String, AbstractDataset>(1);
		if (dataSets != null) {
			for (int i = 0; i < dataSets.length; i++) {
				String name = "Plot " + i;
				if (dataSets[i].getName() != null)
					name = dataSets[i].getName();
				d.put(name, dataSets[i]);
			}
		}
		ret.setDataSets(d);
		ret.setCurrentPlotName("Plot");

		ret.setXAxisMode(1);
		ret.setYAxisMode(1);

		ret.setXAxis(getXAxisLabel());
		ret.setYAxis(getYAxisLabel());

		return ret;
	}
	
	/**
	 * Call once all ui has been added
	 */
	public void computeSizes() {
		leftScroll.setContent(left);
		leftScroll.setMinSize(left.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		rightScroll.setContent(rightSash);
		rightScroll.setMinSize(rightSash.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public String getPartName() {
		return part.getTitle();
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return part.getSite();
	}

	public ScrolledComposite getLeftScroll() {
		return leftScroll;
	}

	public ScrolledComposite getRightScroll() {
		return rightScroll;
	}

	public Composite getLeft() {
		return left;
	}

	public Composite getRight() {
		return right;
	}

	public void setWeights(int[] is) {
		this.sashForm.setWeights(is);
	}

	public void setXAxisLabel(String label) {
		this.xAxisLabel = label;
	}

	public void setYAxisLabel(String label) {
		this.yAxisLabel = label;
	}

	public void setDataSets(AbstractDataset... dataSets) {
		this.dataSets = dataSets;
	}

	public String getXAxisLabel() {
		return xAxisLabel;
	}

	public String getYAxisLabel() {
		return yAxisLabel;
	}

	public SashForm getSashForm() {
		return sashForm;
	}
	
	public void dispose() {
		if (plottingsystem != null){
			plottingsystem.dispose();
		}
	}

	/**
	 * Adds status to the status field (scrolling history).
	 * 
	 * SWT thread safe
	 * 
	 * @param text
	 */
	public void appendStatus(final String text, Logger logger) {
		if (logger!=null) logger.info(text);
		
		if (getSite().getShell()==null) return;
		if (getSite().getShell().isDisposed()) return;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				statusLabel.append(DateFormat.getDateTimeInstance().format(new Date()));
				statusLabel.append(" ");
				statusLabel.append(text);
				statusLabel.append("\n");
			}
		});
	}

	public void layout() {
		GridUtils.startMultiLayout(sashForm);
		try {
			GridUtils.layoutFull(sashForm);
			GridUtils.layoutFull(left);
			leftScroll.setMinSize(left.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	/**
	 * Plots the data given by setDatasets
	 */
	public void plotData() {
		plottingsystem.clear();
		List<IDataset> ys = new ArrayList<IDataset>();
		for (AbstractDataset ds : dataSets){
			ys.add(ds);
		}
		plottingsystem.createPlot1D(null, ys, null);
//		plottingsystem.setTitle(title);
	}
}
