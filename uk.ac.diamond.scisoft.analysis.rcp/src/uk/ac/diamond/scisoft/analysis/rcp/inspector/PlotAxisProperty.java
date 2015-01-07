/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;

/**
 * Represent plot axis used in GUI and model
 */
public class PlotAxisProperty extends InspectorProperty {
	private final static String propName = "plotaxis";

	public final static String plotUpdate = "plotupdate";

	protected PlotAxis plotAxis;

	@Override
	public PlotAxisProperty clone() {
		PlotAxisProperty n = new PlotAxisProperty();
		n.plotAxis = plotAxis == null ? null : plotAxis.clone();
		return n;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(plotAxis != null ? plotAxis.getName() : null);
		s.append(": ");
		s.append(pcl);
		return s.toString();
	}

	public PlotAxis getValue() {
		return plotAxis;
	}

	public void setValue(PlotAxis plotAxis) {
		PlotAxis oldValue = this.plotAxis;
		this.plotAxis = plotAxis;

		fire(new PropertyChangeEvent(this, propName, oldValue, plotAxis));
	}

	public boolean isInSet() {
		if (plotAxis == null)
			return false;
		return plotAxis.isInSet();
	}

	public String getName() {
		return plotAxis != null ? plotAxis.getName() : null; 
	}

	public LinkedList<String> getNames() {
		return plotAxis != null ? plotAxis.getNames() : null; 
	}

	public void setName(String name) {
		setName(name, true);
	}

	public void clear() {
		if (plotAxis != null)
			plotAxis.clear();
	}

	public boolean containsName(String name) {
		if (plotAxis != null)
			return plotAxis.containsName(name);
		return false;
	}

	public void put(int dimension, String name) {
		if (plotAxis == null)
			plotAxis = new PlotAxis();

		plotAxis.putParameter(dimension, name);
	}

	public int getDimension() {
		return plotAxis != null ? plotAxis.getDimension() : -1;
	}

	public void setName(String name, boolean fire) {
		String oldName = null;
		if (plotAxis == null)
			plotAxis = new PlotAxis();
		else
			oldName = plotAxis.getName();

		plotAxis.setName(name);

		if (fire)
			fire(new PropertyChangeEvent(this, propName, oldName, name));
	}

	public void setInSet(boolean inSet) {
		if (plotAxis != null)
			plotAxis.setInSet(inSet);
	}
}
