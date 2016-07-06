/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import java.util.List;

import org.eclipse.january.dataset.ILazyDataset;

import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;

public class HDF5Selection extends DatasetSelection {
	private String node;

	public HDF5Selection(InspectorType type, String filename, String node, List<AxisSelection> axes, ILazyDataset... dataset) {
		super(type, filename, axes, dataset);
		this.node = node;
	}

	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof HDF5Selection) {
			HDF5Selection that = (HDF5Selection) other;
			if (filePath == null && that.filePath == null)
				return node.equals(that.node);
			if (filePath != null && filePath.equals(that.filePath))
				return node.equals(that.node);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 17 + node.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return node + " = " + super.toString();
	}

	public String getNode() {
		return node;
	}
}
