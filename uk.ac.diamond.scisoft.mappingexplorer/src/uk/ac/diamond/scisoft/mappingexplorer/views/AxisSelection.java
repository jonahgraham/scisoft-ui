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

import org.eclipse.jface.viewers.ISelection;

/**
 * @author rsr31645
 * 
 */
public class AxisSelection implements ISelection {
	private String dimLabel;
	private int dimension;

	private int dimensionValue;

	public enum DimensionChanged {
		INDEX, VALUE;
	}

	private DimensionChanged changeAffected = DimensionChanged.INDEX;

	public AxisSelection(String dimLabel, int dimension) {
		this.dimLabel = dimLabel;
		this.dimension = dimension;
	}

	public AxisSelection(String dimLabel, int dimension, int dimensionVal) {
		this(dimLabel, dimension);
		this.dimensionValue = dimensionVal;
	}

	public String getLabel() {
		return dimLabel;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimensionValue(int dimensionValue) {
		this.dimensionValue = dimensionValue;
	}

	public int getDimensionValue() {
		return dimensionValue;
	}

	public void setChangeAffected(DimensionChanged changeAffected) {
		this.changeAffected = changeAffected;
	}

	public DimensionChanged getChangeAffected() {
		return changeAffected;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}