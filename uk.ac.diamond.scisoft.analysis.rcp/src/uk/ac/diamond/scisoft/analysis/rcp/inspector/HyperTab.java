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

import java.util.List;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
import uk.ac.diamond.scisoft.analysis.rcp.views.HyperView;

public class HyperTab extends PlotTab {

	public HyperTab(IWorkbenchPartSite partSite, InspectorType type, String title, String[] axisNames) {
		super(partSite, type, title, axisNames);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void pushToView(IMonitor monitor, List<SliceProperty> sliceProperties) {
		if (dataset == null)
			return;
		
		if (dataset.getRank() != 3) return;

//		Slice[] slices = new Slice[sliceProperties.size()];
//		boolean[] average = new boolean[sliceProperties.size()];
//		for (int i = 0; i < slices.length; i++) {
//			slices[i] = sliceProperties.get(i).getValue();
//			average[i] = sliceProperties.get(i).isAverage();
//		}
//
//		int[] order = getOrder(daxes.size());
//		final List<AbstractDataset> slicedAxes = sliceAxes(getChosenAxes(), slices, average, order);
//
//
//		if (itype == InspectorType.DATA2D) {
//			swapFirstTwoInOrder(order);
//		}
//
//		final AbstractDataset reorderedData = slicedAndReorderData(monitor, slices, average, order, null);
//		if (reorderedData == null) return;
//		
//		reorderedData.setName(dataset.getName());
//		reorderedData.squeeze();
//		if (reorderedData.getSize() < 1)
//			return;

		switch (itype) {
		case HYPER:
			composite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					HyperView tableView = getHyperView();
					if (tableView == null)
						return;
					
					List<AxisChoice> axisChoices = getChosenAxes();
					//TODO find dataset dim from paxes
					PlotAxis pa = paxes.get(0).getValue();
					
					int chosenDim = 0;
					
					for (int i = 0; i < axisChoices.size(); i++) {
						if (axisChoices.get(i).getName() == pa.getName()) {
							chosenDim = i;
						}
					}
					
					tableView.setData(dataset, axisChoices, chosenDim);
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
		if (rank == 3)
			isCompatible = true;

		if (composite != null)
			composite.setEnabled(isCompatible);
		return isCompatible;
	}

}
