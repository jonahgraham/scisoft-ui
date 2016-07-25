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

import org.eclipse.january.dataset.Slice;

/**
 * Represent slice used in GUI and model
 */
public class SliceProperty extends InspectorProperty {
	private final static String propName = "slice";

	public final static String sliceUpdate = "sliceupdate";

	protected Slice slice;
	protected int max = -1; // maximum size
	protected boolean average = false; // average data within the slice

	@Override
	public SliceProperty clone() {
		SliceProperty n = new SliceProperty();
		n.slice = slice.clone();
		n.max = max;
		n.average = average;
		return n;
	}

	@Override
	public String toString() {
		String str = slice != null ? slice.toString() : ":";
		if (average)
			str = "~" + str;
		return str;
	}

	public Slice getValue() {
		return slice;
	}

	public void setValue(Slice slice) {
		Slice oldValue = this.slice;
		this.slice = slice;

		fire(new PropertyChangeEvent(this, propName, oldValue, slice));
	}

	public void setStart(int start) {
		Integer oldStart = null;
		if (slice == null)
			slice = new Slice(start, null);
		else {
			oldStart = slice.getStart();
			slice.setStart(start);
		}

		fire(new PropertyChangeEvent(this, propName, oldStart, start));
	}

	public void setStop(int stop) {
		setStop(stop, false);
	}

	public void setStop(int stop, boolean triggerSlicerUpdate) {
		Integer oldStop = null;
		if (slice == null)
			slice = new Slice(stop);
		else {
			oldStop = slice.getStop();
			slice.setStop(stop);
		}

		fire(new PropertyChangeEvent(this, triggerSlicerUpdate ? sliceUpdate : propName, oldStop, stop));
	}

	public void setLength(int length) {
		max = length;
		if (slice == null)
			slice = new Slice();
		slice.setLength(length);
	}

	public void setStep(int step) {
		int oldStep = 1;
		if (slice == null)
			slice = new Slice(null, null, step);
		else {
			oldStep = slice.getStep();
			slice.setStep(step);
		}

		fire(new PropertyChangeEvent(this, propName, oldStep, step));
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMax() {
		return max;
	}
	
	public void setAverage(boolean average) {
		this.average = average;
	}

	public boolean isAverage() {
		return average;
	}
	
	public boolean isSlice() {
		return slice!=null && !slice.isSliceComplete();
	}
}
