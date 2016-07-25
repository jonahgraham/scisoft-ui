/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.scisoft.mappingexplorer.views;

import java.util.List;

import org.eclipse.january.dataset.ILazyDataset;

import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;

/**
 * @author rsr31645
 */
public class HDF5MappingViewData {

	public static class OneDData implements IMappingViewData {
		private final ILazyDataset ds;
		private final String dim1;

		public OneDData(ILazyDataset ds, String dim1) {
			this.ds = ds;
			this.dim1 = dim1;
		}

		@Override
		public ILazyDataset getDataSet() {
			return ds;
		}

		@Override
		public String getDimension1Label() {
			return dim1;
		}

		@Override
		public double[] getDimension1Values() {
			return null;
		}
	}

	public static class TwoDData extends OneDData implements IMappingView2dData {

		private final String dim2;

		public TwoDData(ILazyDataset ds, String... dims) {
			super(ds, dims[0]);
			this.dim2 = dims[1];
		}

		@Override
		public String getDimension2Label() {
			return dim2;
		}

		@Override
		public double[] getDimension2Values() {
			return null;
		}
	}

	public static class ThreeDData extends TwoDData implements IMappingView3dData {
		private final String dim3;

		public ThreeDData(ILazyDataset ds, String... dims) {
			super(ds, dims[0], dims[1]);
			this.dim3 = dims[2];

		}

		@Override
		public String getDimension3Label() {
			return dim3;
		}

		@Override
		public double[] getDimension3Values() {
			return null;
		}
	}

	public static IMappingViewData getMappingViewData(DatasetSelection datasel) {
		List<AxisSelection> axes = datasel.getAxes();
		int rank = datasel.getFirstElement().getRank();
		String[] names = new String[rank];
		for (int i = 0; i < rank; i++) {
			if (axes == null || axes.size() < i) {
				names[i] = "Dim " + (i+1);
			} else {
				names[i] = axes.get(i).getSelectedName();
			}
		}
		switch (rank) {
		case 1:
			return new OneDData(datasel.getFirstElement(), names[0]);
		case 2:
			return new TwoDData(datasel.getFirstElement(), names);
		case 3:
			return new ThreeDData(datasel.getFirstElement(), names);
		}
		return null;
	}
}
