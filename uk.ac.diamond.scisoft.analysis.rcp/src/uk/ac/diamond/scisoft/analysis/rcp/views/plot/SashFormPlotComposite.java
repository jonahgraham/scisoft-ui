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

package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.plotting.jreality.tool.PlotActionComplexEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.Logger;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotDataTableDialog;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
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

	protected final IWorkbenchPart    part;
	protected final ScrolledComposite leftScroll,rightScroll;
	protected final Composite         left,right;
	protected final DataSetPlotter    plotter;
	protected final SashForm          sashForm;
	protected       AbstractDataset[] dataSets;
	protected       String            xAxisLabel,yAxisLabel;
	private ToolBarManager            rightHandToolbarManager;
	private Label                     rightHandToolbarManagerLabel;
	private SashForm                  rightSash;
	private Text                      statusLabel;
	private Plot1DUIAdapter           plotUI;

	/**
	 * 
	 * @param parent
	 * @param part 
	 */
	public SashFormPlotComposite(Composite parent, final IWorkbenchPart part, final IAction... actions) {

		this.part = part;
		
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
		
		final Composite top = new Composite(right, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gl_top = new GridLayout(3, false);
		gl_top.verticalSpacing = 0;
		gl_top.marginWidth = 0;
		gl_top.marginHeight = 0;
		gl_top.horizontalSpacing = 0;
		top.setLayout(gl_top);
		
		final ToolBar bar1 = new ToolBar(top, SWT.FLAT);
		bar1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		this.rightHandToolbarManagerLabel = new Label(top, SWT.NONE);
		rightHandToolbarManagerLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final ToolBar bar2 = new ToolBar(top, SWT.FLAT);
		bar2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		this.rightHandToolbarManager = new ToolBarManager(bar2);
	       
		this.plotter = new DataSetPlotter(PlottingMode.ONED, right,false);
		final Composite graphComp    = plotter.getComposite();
        graphComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        plotter.setPlotActionEnabled(true);
        plotter.setPlotRightClickActionEnabled(false);

        final ToolBarManager   toolBar = new ToolBarManager(bar1);
		this.plotUI  = new Plot1DUIAdapter(toolBar, plotter, right,getPartName()){
			@Override
			public void buildToolActions(IToolBarManager manager) {
				manager.add(StaticScanPlotView.getOpenStaticPlotAction(SashFormPlotComposite.this));
				manager.add(addToHistory);
				manager.add(removeFromHistory);
				super.buildToolActions(manager);
				manager.add(createShowLegend());
				manager.add(rightClickOnGraphAction);
				
				
				if (actions!=null) for (int i = 0; i < actions.length; i++) {
					manager.add(actions[i]);
				}
			}
			@Override
			public void plotActionPerformed(final PlotActionEvent event) {		
				if (event instanceof PlotActionComplexEvent) {
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run()
						{
							PlotDataTableDialog dataDialog = 
								new PlotDataTableDialog(parent.getShell(),(PlotActionComplexEvent)event);
							dataDialog.open();								
						}
					});
				} else {
					parent.getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							double x = event.getPosition()[0];
							double y = event.getPosition()[1];
							String text = String.format("X:%g Y:%g", x, y);
							rightHandToolbarManagerLabel.setText(text);

						}
					});
				}
			}
		};
		plotter.registerUI(plotUI);
		toolBar.update(true);

		this.statusLabel = new Text(rightSash, SWT.WRAP|SWT.V_SCROLL);
		statusLabel.setEditable(false);
		rightSash.setWeights(new int[]{100,10});
	}
	
	public void addZoomListener(final IPropertyChangeListener ifZoom) {
		plotUI.addZoomListener(ifZoom);
	}
	@Override
	public PlotBean getPlotBean() {
		final PlotBean ret = new PlotBean();
		
		final Map<String,AbstractDataset> d = new HashMap<String,AbstractDataset>(1);
		if (dataSets!=null) {
			for (int i = 0; i < dataSets.length; i++) {
				String name = "Plot "+i;
				if (dataSets[i].getName()!=null) name = dataSets[i].getName();
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

	/**
	 * @return Returns the leftScroll.
	 */
	public ScrolledComposite getLeftScroll() {
		return leftScroll;
	}

	/**
	 * @return Returns the rightScroll.
	 */
	public ScrolledComposite getRightScroll() {
		return rightScroll;
	}

	/**
	 * @return Returns the left.
	 */
	public Composite getLeft() {
		return left;
	}

	/**
	 * @return Returns the right.
	 */
	public Composite getRight() {
		return right;
	}

	/**
	 * @return Returns the plotter.
	 */
	public DataSetPlotter getPlotter() {
		return plotter;
	}

	/**
	 * 
	 * @param is
	 */
	public void setWeights(int[] is) {
		this.sashForm.setWeights(is);
	}

	/**
	 * 
	 * @param label
	 */
	public void setXAxisLabel(String label) {
		this.xAxisLabel = label;
		plotter.setXAxisLabel(label);
	}
	
	/**
	 * 
	 * @param label
	 */
	public void setYAxisLabel(String label) {
		this.yAxisLabel = label;
		plotter.setYAxisLabel(label);
	}

	/**
	 * @param dataSets The dataSet to set.
	 */
	public void setDataSets(AbstractDataset... dataSets) {
		this.dataSets = dataSets;
	}

	/**
	 * @return Returns the xAxisLabel.
	 */
	public String getXAxisLabel() {
		return xAxisLabel;
	}

	/**
	 * @return Returns the yAxisLabel.
	 */
	public String getYAxisLabel() {
		return yAxisLabel;
	}

	/**
	 * @return Returns the sashForm.
	 */
	public SashForm getSashForm() {
		return sashForm;
	}
	

	/**
	 * Called when SashFormPlotComposite should dispose.
	 */
	public void dispose() {
		if (plotter!=null) plotter.cleanUp();
	}

	/**
	 * 
	 * @param label
	 */
	public void setRightHandToolBarLabel(final String label) {
		this.rightHandToolbarManagerLabel.setText(label);
	}
	
	/**
	 * 
	 * @param action
	 */
	public void add(IAction action) {
		this.rightHandToolbarManager.add(action);
		this.rightHandToolbarManager.update(true);
	}

	/**
	 * 
	 * @param item
	 */
	public void add(IContributionItem item) {
		this.rightHandToolbarManager.add(item);
		this.rightHandToolbarManager.update(true);
	}

	/**
	 * 
	 * @param b
	 */
	public void setRightHandToolBarLabelEnabled(boolean b) {
		this.rightHandToolbarManagerLabel.setEnabled(b);
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


}
