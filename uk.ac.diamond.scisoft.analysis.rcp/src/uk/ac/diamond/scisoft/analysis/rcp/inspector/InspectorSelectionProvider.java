/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import javax.swing.tree.DefaultMutableTreeNode;

import ncsa.hdf.object.HObject;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.hdf5.editor.IH5DoubleClickSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5Selection;
import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5Utils;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView;

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
