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

import org.eclipse.core.runtime.IAdapterFactory;
import org.python.pydev.shared_interactive_console.console.codegen.IScriptConsoleCodeGenerator;
import org.python.pydev.shared_interactive_console.console.codegen.PythonSnippetUtils;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;

@SuppressWarnings("rawtypes")
public class HDF5AdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IScriptConsoleCodeGenerator.class) {
			if (adaptableObject instanceof HDF5NodeLink) {
				final HDF5NodeLink hdf5NodeLink = (HDF5NodeLink) adaptableObject;
				return new IScriptConsoleCodeGenerator() {

					@Override
					public String getPyCode() {
						return "dnp.io.load("
								+ PythonSnippetUtils.getSingleQuotedString(hdf5NodeLink.getFile().getFilename()) + ")["
								+ PythonSnippetUtils.getSingleQuotedString(hdf5NodeLink.getFullName()) + "]";
					}

					@Override
					public boolean hasPyCode() {
						return true;
					}
				};
			} else if (adaptableObject instanceof HDF5Attribute) {
				final HDF5Attribute hdf5Attribute = (HDF5Attribute) adaptableObject;
				return new IScriptConsoleCodeGenerator() {

					@Override
					public String getPyCode() {
						String filename = hdf5Attribute.getHDF5File().getFilename();
						return "dnp.io.load(" + PythonSnippetUtils.getSingleQuotedString(filename) + ")["
								+ PythonSnippetUtils.getSingleQuotedString(hdf5Attribute.getFullName()) + "]";
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
