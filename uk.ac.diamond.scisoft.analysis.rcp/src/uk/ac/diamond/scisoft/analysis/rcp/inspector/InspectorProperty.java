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
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

public abstract class InspectorProperty {
	protected Set<PropertyChangeListener> pcl = new HashSet<PropertyChangeListener>();

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if( listener != null)
			pcl.add(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if( listener != null)
			pcl.remove(listener);
	}

	protected void fire(PropertyChangeEvent event) {
		// allow concurrent modifications by make copy
		HashSet<PropertyChangeListener> ls = new HashSet<PropertyChangeListener>(pcl);
		for (PropertyChangeListener l : ls) {
			if (l != null)
				l.propertyChange(event);
		}
	}
}
