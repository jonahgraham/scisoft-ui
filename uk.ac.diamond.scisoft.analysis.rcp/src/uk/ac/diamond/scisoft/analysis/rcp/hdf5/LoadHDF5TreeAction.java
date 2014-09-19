/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.rcp.views.HDF5TreeView;

/**
 * Action for loading a HDF5 tree to viewer
 */
public class LoadHDF5TreeAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
		     final HDF5TreeView htv = (HDF5TreeView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(HDF5TreeView.ID);
		     htv.loadTreeUsingFileDialog();
		     return Boolean.TRUE;
		} catch (Exception ne) {
			return Boolean.FALSE;
		}
	}

}
