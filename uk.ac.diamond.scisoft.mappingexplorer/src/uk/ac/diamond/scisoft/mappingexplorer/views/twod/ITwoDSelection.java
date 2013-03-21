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
package uk.ac.diamond.scisoft.mappingexplorer.views.twod;

import org.eclipse.jface.viewers.ISelection;

import uk.ac.diamond.scisoft.mappingexplorer.views.AxisSelection;

/**
 * @author rsr31645
 */
public interface ITwoDSelection extends ISelection {

	String getSecondaryViewId();

	IPixelSelection getPixelSelection();

	AxisSelection getAxisDimensionSelection();

	IAreaSelection getAreaSelection();

	boolean isFlipped();

	public interface IPixelSelection {
		int[] getSelectedPixel();
	}

	public static class PixelSelection implements IPixelSelection {
		private final int[] selectedPixel;

		public PixelSelection(int[] selectedPixel) {
			this.selectedPixel = selectedPixel;
		}

		@Override
		public int[] getSelectedPixel() {
			return selectedPixel;
		}
	}

	public interface IAreaSelection {
		int[] getStartPosition();

		int[] getEndPosition();
	}

	public static class AreaSelection implements IAreaSelection {

		private final int[] startPosition;
		private final int[] endPostion;

		public AreaSelection(int[] startPosition, int[] endPostion) {
			this.startPosition = startPosition;
			this.endPostion = endPostion;
		}

		@Override
		public int[] getEndPosition() {
			return endPostion;
		}

		@Override
		public int[] getStartPosition() {
			return startPosition;
		}
	}

	public static class TwoDSelection implements ITwoDSelection {

		private final String secondaryViewId;
		private boolean flipped;
		private AxisSelection dimensionSelection;
		private IPixelSelection pixelSelection;
		private IAreaSelection areaSelection;


		public TwoDSelection(String secondaryViewId, AxisSelection selectedDimension, PixelSelection pixelSelection, boolean isFlipped) {
			this.secondaryViewId = secondaryViewId;
			dimensionSelection = selectedDimension;
			this.pixelSelection = pixelSelection;
			this.flipped = isFlipped;
		}

		public void setFlipped(boolean flipped) {
			this.flipped = flipped;
		}

		@Override
		public String getSecondaryViewId() {
			return secondaryViewId;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean isFlipped() {
			return flipped;
		}

		@Override
		public AxisSelection getAxisDimensionSelection() {
			return dimensionSelection;
		}

		public void setPixelSelection(IPixelSelection pixelSelection) {
			this.pixelSelection = pixelSelection;
		}

		@Override
		public IPixelSelection getPixelSelection() {
			return pixelSelection;
		}

		public void setAreaSelection(IAreaSelection areaSelection) {
			this.areaSelection = areaSelection;
		}

		@Override
		public IAreaSelection getAreaSelection() {
			return areaSelection;
		}

	}

}
