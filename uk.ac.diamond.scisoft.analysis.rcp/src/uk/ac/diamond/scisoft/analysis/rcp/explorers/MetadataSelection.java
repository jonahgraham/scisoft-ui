/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.explorers;

import org.eclipse.jface.viewers.ISelection;

/**
 * Metadata item selection given by key/name/path
 */
public class MetadataSelection implements ISelection {

	private String pathname;

	public MetadataSelection(String name) {
		pathname = name;
	}

	@Override
	public boolean isEmpty() {
		return pathname == null || pathname.length() == 0;
	}
	
	public String getPathname() {
		return pathname;
	}
}
