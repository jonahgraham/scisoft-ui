/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import javax.swing.tree.DefaultMutableTreeNode;

import ncsa.hdf.object.HObject;

import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.hdf5.editor.IH5DoubleClickSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5Utils;

/**
 * Provides a link from the HDF5Tree, which is HDFView based and is fast, to the
 * DatasetInspector for the DExplore perspective.
 * 
 * We need to support this in order to have a backup HDF5Editor which works
 * with really large files and does not use the low level API for emergencies.
 * 
 */
public class InspectorSelectionProvider implements IH5DoubleClickSelectionProvider {

	@Override
	public ISelection getSelection(ISelection selection, String filePath) throws Exception {
		
    
		
		final Object node = selection instanceof IStructuredSelection
				          ? ((IStructuredSelection)selection).getFirstElement()
				          : null;
				          
	    if (node==null) return null;
	    if (!(node instanceof DefaultMutableTreeNode)) return null;
	    
	    final DefaultMutableTreeNode dNode = (DefaultMutableTreeNode)node;
	    if (!(dNode.getUserObject() instanceof HObject)) return null;
	    final HObject linkPath = (HObject)dNode.getUserObject();
	    
	    HDF5Loader   loader = new HDF5Loader(filePath);
	    HDF5File     tree   = loader.loadTree(new IMonitor.Stub());	    
	    HDF5NodeLink link   = tree.findNodeLink(linkPath.getFullName());
		return HDF5Utils.createDatasetSelection(link, true);
	}

}
