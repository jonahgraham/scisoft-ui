/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.python.pydev.shared_interactive_console.console.codegen.IScriptConsoleCodeGenerator;
import org.python.pydev.shared_interactive_console.console.codegen.PythonSnippetUtils;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;

/**
 * This test verifies that the adapter factory properly adapts HDF5 objects into {@link IScriptConsoleCodeGenerator}s
 * 
 * @see HDF5AdapterFactory
 */
public class HDF5AdapterFactoryPluginTest {

	private HDF5NodeLink createNodeLink() {
		HDF5File file = new HDF5File(123456, "/my/filename/here");
		HDF5Node node = new HDF5Node(234566);
		HDF5NodeLink hdf5NodeLink = new HDF5NodeLink(file, "/path", "/name", null, node);
		return hdf5NodeLink;
	}

	private void checkGenerator(IScriptConsoleCodeGenerator generator) {
		Assert.assertNotNull(generator);
		Assert.assertEquals(true, generator.hasPyCode());
		Assert.assertEquals("dnp.io.load('/my/filename/here')['/path/name']", generator.getPyCode());
	}

	@Test
	public void testHDF5NodeLinkAdaptsTo() {
		IScriptConsoleCodeGenerator generator = PythonSnippetUtils
				.getScriptConsoleCodeGeneratorAdapter(createNodeLink());
		checkGenerator(generator);
	}

	@Test
	public void testHDF5NodeLinkFactoryDirect() {
		IScriptConsoleCodeGenerator generator = (IScriptConsoleCodeGenerator) new HDF5AdapterFactory().getAdapter(
				createNodeLink(), IScriptConsoleCodeGenerator.class);
		checkGenerator(generator);
	}

}
