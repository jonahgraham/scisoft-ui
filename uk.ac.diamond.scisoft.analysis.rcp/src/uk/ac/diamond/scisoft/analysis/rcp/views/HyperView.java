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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.hyper.ArpesMainImageReducer;
import org.dawb.common.ui.hyper.ArpesSideImageReducer;
import org.dawb.common.ui.hyper.HyperWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;

/**
 * Display a 3D dataset across two plots with ROI slicing
 */
public class HyperView extends ViewPart {
	
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView";
	private HyperWindow hyperWindow;
	
	@Override
	public void createPartControl(Composite parent) {
		
		hyperWindow = new HyperWindow();
		hyperWindow.createControl(parent);
		
	}
	
	public void setData(ILazyDataset lazy, List<AxisChoice> daxes, int traceDim, boolean asImages) {
		
		List<ILazyDataset> axes = new ArrayList<ILazyDataset>(daxes.size());
		
		for (AxisChoice ax : daxes) {
			axes.add(ax.getValues());
		}
		
		if (!asImages) hyperWindow.setData(lazy, axes, traceDim);
		else {
			hyperWindow.setData(lazy, axes, traceDim,new ArpesMainImageReducer(),new ArpesSideImageReducer());
		}
	}
	
	@Override
	public void setFocus() {
		hyperWindow.setFocus();
		
	}
}
