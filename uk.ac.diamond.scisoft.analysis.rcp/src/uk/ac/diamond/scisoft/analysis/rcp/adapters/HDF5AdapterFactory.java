/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.python.pydev.shared_interactive_console.console.codegen.IScriptConsoleCodeGenerator;
import org.python.pydev.shared_interactive_console.console.codegen.PythonSnippetUtils;

@SuppressWarnings("rawtypes")
public class HDF5AdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IScriptConsoleCodeGenerator.class) {
			if (adaptableObject instanceof NodeLink) {
				final NodeLink nodeLink = (NodeLink) adaptableObject;
				final Tree tree = nodeLink.getTree();
				if (!(tree instanceof TreeFile))
					return null;

				return new IScriptConsoleCodeGenerator() {

					@Override
					public String getPyCode() {
						return "dnp.io.load("
								+ PythonSnippetUtils.getSingleQuotedString(((TreeFile) tree).getFilename()) + ")["
								+ PythonSnippetUtils.getSingleQuotedString(nodeLink.getFullName()) + "]";
					}

					@Override
					public boolean hasPyCode() {
						return true;
					}
				};
			} else if (adaptableObject instanceof Attribute) {
				final Attribute attribute = (Attribute) adaptableObject;
				final Tree tree = attribute.getTree();
				if (!(tree instanceof TreeFile))
					return null;

				return new IScriptConsoleCodeGenerator() {

					@Override
					public String getPyCode() {
						String filename = ((TreeFile) tree).getFilename();
						return "dnp.io.load(" + PythonSnippetUtils.getSingleQuotedString(filename) + ")["
								+ PythonSnippetUtils.getSingleQuotedString(attribute.getFullName()) + "]";
					}

					@Override
					public boolean hasPyCode() {
						return true;
					}
				};

			}
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IScriptConsoleCodeGenerator.class };
	}

}
