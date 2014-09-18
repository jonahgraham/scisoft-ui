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

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

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

		public TwoDData(ILazyDataset ds, String dim1, String dim2) {
			super(ds, dim1);
			this.dim2 = dim2;
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

		public ThreeDData(ILazyDataset ds, String dim1, String dim2, String dim3) {
			super(ds, dim1, dim2);
			this.dim3 = dim3;

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
		switch (datasel.getFirstElement().getShape().length) {
		case 1:
			return new OneDData(datasel.getFirstElement(), datasel.getAxes().get(0).getSelectedName());
		case 2:
			return new TwoDData(datasel.getFirstElement(), datasel.getAxes().get(0).getSelectedName(), datasel
					.getAxes().get(1).getSelectedName());
		case 3:
			return new ThreeDData(datasel.getFirstElement(), datasel.getAxes().get(0).getSelectedName(), datasel
					.getAxes().get(1).getSelectedName(), datasel.getAxes().get(2).getSelectedName());
		}
		return null;
	}
}