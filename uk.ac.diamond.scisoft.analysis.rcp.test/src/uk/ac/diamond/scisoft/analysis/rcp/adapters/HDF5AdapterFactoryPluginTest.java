/*-
 * Copyright 2012 Diamond Light Source Ltd.
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
