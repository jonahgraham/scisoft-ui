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
package uk.ac.diamond.scisoft.mappingexplorer.views.oned;

import org.eclipse.jface.viewers.ISelection;

import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;

/**
 * One D selection interface. This selection is used to propagate the data selected in the OneD viewer. Expects the data
 * set in the OneD viewer to be of two dimension.
 * 
 * @author rsr31645
 */
public interface IOneDSelection extends ISelection {

	/**
	 * @return {@link AxisSelection} of the first dimension in the data set (data at shape[0])
	 */
	AxisSelection getDimension1Selection();

	/**
	 * @return @return {@link AxisSelection} of the first dimension in the data set (data at shape[1])
	 */
	AxisSelection getDimension2Selection();

	/**
	 * @return the secondary view id of the OneD view. This is used to filter whether the selection needs to be applied
	 *         by the selection listener.
	 */
	String getSecondaryViewId();

	/**
	 * Default implementation class for the {@link IOneDSelection}
	 */
	public static class OneDSelection implements IOneDSelection {

		private final String secondaryViewId;
		private final AxisSelection dimension1Selection;
		private final AxisSelection dimension2Selection;

		public OneDSelection(String secondaryViewId, AxisSelection dimension1Selection,
				AxisSelection dimension2Selection) {
			this.secondaryViewId = secondaryViewId;
			this.dimension1Selection = dimension1Selection;
			this.dimension2Selection = dimension2Selection;

		}

		@Override
		public String getSecondaryViewId() {
			return secondaryViewId;
		}

		@Override
		public AxisSelection getDimension1Selection() {
			return dimension1Selection;
		}

		@Override
		public AxisSelection getDimension2Selection() {
			return dimension2Selection;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return false;
		}
	}
}
