/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import uk.ac.diamond.scisoft.analysis.rcp.views.HDF5TreeView;

public class CollapseAllHDF5TreeAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final HDF5TreeView hdf5TreeView = (HDF5TreeView) EclipseUtils.getActivePage().getActivePart();
		hdf5TreeView.collapseAll();
		return Boolean.TRUE;
	}

}
