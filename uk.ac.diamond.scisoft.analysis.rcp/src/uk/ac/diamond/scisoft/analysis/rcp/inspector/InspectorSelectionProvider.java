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
import javax.swing.tree.TreeNode;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.hdf5.editor.IH5DoubleClickSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
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
				? ((IStructuredSelection) selection).getFirstElement() : null;

		if (node == null)
			return null;
		if (!(node instanceof DefaultMutableTreeNode))
			return null;

		final DefaultMutableTreeNode dNode = (DefaultMutableTreeNode) node;
		TreeNode[] path = dNode.getPath();
		int level = dNode.getLevel();
		String nodePath = getPath(path, level);

		IDataHolder holder = LoaderFactory.getData(filePath);
		Tree tree = holder.getTree();
		NodeLink link = tree.findNodeLink(nodePath);
		return HDF5Utils.createDatasetSelection(filePath, nodePath, link);
	}

	/**
	 * Returns the path of the node (without the root) given a treenode and the level of the node
	 * 
	 * @param path
	 * @param level
	 * @return path
	 */
	private String getPath(TreeNode[] path, int level) {
		String nodepath = "/";
		for (int i = 1; i <= level; i++) {
			nodepath = nodepath + path[i].toString() + "/";
		}
		nodepath = nodepath.substring(0, nodepath.length() - 1);
		return nodepath;
	}
}
