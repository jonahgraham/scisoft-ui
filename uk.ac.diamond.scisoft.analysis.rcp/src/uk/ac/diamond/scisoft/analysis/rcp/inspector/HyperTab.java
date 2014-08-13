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

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.dawnsci.slicing.tools.hyper.HyperType;
import org.dawnsci.slicing.tools.hyper.HyperView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;

public class HyperTab extends PlotTab {
		
	private HyperType hyperType;

	public HyperTab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
		super(partSite, type, title, axisNames);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Composite createTabComposite(Composite parent) {
		ScrolledComposite sComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite holder = new Composite(sComposite, SWT.NONE);
		holder.setLayout(new GridLayout(2, false));

		axisLabels = new ArrayList<Label>();
		combos = new ArrayList<Combo>();

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				int i = combos.indexOf(c);
				if (i >= 0 && paxes != null) {
					PlotAxisProperty p = paxes.get(i);
					String item = c.getItem(c.getSelectionIndex()); 
					if (item.equals(p.getName()))
						return;
					p.setName(item, false);
					repopulateCombos(null, null);
				}
			}
		};
		createCombos(holder, listener);

		if (daxes != null)
			populateCombos();
		
		final Button b1 = new Button(holder, SWT.RADIO);
		b1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		b1.setText("Box and Axis Region");
		b1.setSelection(true);
		hyperType = HyperType.Box_Axis;
		b1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hyperType = HyperType.Box_Axis;
				fireUpdate();
			}
		});
		
		final Button b2 = new Button(holder, SWT.RADIO);
		b2.setText("Line and Axis Line");
		b2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hyperType = HyperType.Line_Line;
				fireUpdate();
			}
		});
		
		final Button b3 = new Button(holder, SWT.RADIO);
		b3.setText("Line and Axis Region");
		b3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		b3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hyperType = HyperType.Line_Axis;
				fireUpdate();
			}
		});

		holder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sComposite.setContent(holder);
		holder.setSize(holder.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		composite = sComposite;
		return composite;
	}
	
	private void fireUpdate() {
		if (paxes != null) { // signal a replot without a slice reset
			PlotAxisProperty p = paxes.get(0);
			p.fire(new PropertyChangeEvent(p, PlotAxisProperty.plotUpdate, p.getName(), p.getName()));
		}
	}
	
	
	@Override
	public void pushToView(IMonitor monitor, List<SliceProperty> sliceProperties) {
		if (dataset == null)
			return;
		
		if (dataset.getRank() < 3) return;
		
		final Slice[] slices = new Slice[sliceProperties.size()];
		boolean[] average = new boolean[sliceProperties.size()];
		for (int i = 0; i < slices.length; i++) {
			slices[i] = sliceProperties.get(i).getValue();
			average[i] =  sliceProperties.get(i).isAverage();
		}

		final int[] order = getOrder(daxes.size());
		// FIXME: Image, surface and volume plots can't work with multidimensional axis data
		final List<Dataset> slicedAxes = sliceAxes(getChosenAxes(), slices, average, order);
		
		///
		// --------- final Dataset reorderedData = slicedAndReorderData(monitor, slices, average, order, null);

		switch (itype) {
		case HYPER:
			composite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					HyperView tableView = getHyperView();
					List<IDataset> sAxes = new ArrayList<IDataset>();
					sAxes.addAll(slicedAxes);
					if (tableView == null)
						return;

					tableView.setData(dataset, sAxes,slices, order, hyperType);
				}
			});
			break;
		case DATA1D:
		case DATA2D:
		case EMPTY:
		case IMAGE:
		case LINE:
		case LINESTACK:
		case IMAGEXP:
		case MULTIIMAGES:
		case POINTS1D:
		case POINTS2D:
		case POINTS3D:
		case SURFACE:
		case VOLUME:
			break;
		}
	}

	private HyperView getHyperView() {
		HyperView view = null;

		// check if Dataset Table View is open
		try {
			view = (HyperView) site.getPage().showView(HyperView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			logger.error("All over now! Cannot find hyper view: {} ", e);
		}
		return view;
	}
	
	@Override
	public boolean checkCompatible(ILazyDataset data) {
		boolean isCompatible = false;
		int rank = data.getRank();
		if (rank > 2)
			isCompatible = true;

		if (composite != null)
			composite.setEnabled(isCompatible);
		return isCompatible;
	}
	
	
	private static final String IMAGE_EXP_AXIS_LABEL = "images";
	
	@Override
	protected void populateCombos() {
		int cSize = combos.size() - comboOffset;
		HashMap<Integer, String> sAxes = getSelectedComboAxisNames();

		for (int i = 0; i < cSize; i++) {
			Combo c = combos.get(i + comboOffset);
			c.removeAll();

			PlotAxisProperty p = paxes.get(i + comboOffset);
			p.clear();

			Label l = axisLabels.get(i + comboOffset);
			if (sAxes.size() == 0) {
				p.setInSet(false);
				c.setEnabled(false);
				c.setVisible(false);
				l.setVisible(false);
				if (itype == InspectorType.IMAGEXP) { // hack to change labels
					l = axisLabels.get(i + comboOffset - 1);
					l.setText(IMAGE_EXP_AXIS_LABEL);
					l.getParent().layout();
				}
				break;
			}
			c.setEnabled(true);
			c.setVisible(true);
			l.setVisible(true);
			if (itype == InspectorType.IMAGEXP && l.getText().equals(IMAGE_EXP_AXIS_LABEL)) {
				l.setText(axes[i+comboOffset]); // reset label
				l.getParent().layout();
			}
			ArrayList<Integer> keyList = new ArrayList<Integer>(sAxes.keySet());
			Collections.sort(keyList);
			Collections.reverse(keyList);
			Integer lastKey = keyList.get(keyList.size() - 1);
			String a = sAxes.get(lastKey); // reverse order

			if (axes.length == 1) { // for 1D plots and 1D dataset table, remove single point axes
				int[] shape = dataset.getShape();
				while (shape[lastKey] == 1) {
					lastKey--;
				}
				a = sAxes.get(lastKey); // reverse order
				for (int j : keyList) {
					String n = sAxes.get(j);
					p.put(j, n);
					if (shape[j] != 1)
						c.add(n);
				}
			} else {
				for (int j : keyList) {
					String n = sAxes.get(j);
					p.put(j, n);
					c.add(n);
				}
			}
			c.setText(a);
			sAxes.remove(lastKey);
			p.setName(a, false);
			p.setInSet(true);
		}
	}

}
