/*-
 * Copyright 2015 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeAdaptable;

/**
 * Reference to a node or attribute in a HDF5 file for an adapter factory
 */
public class HDF5Adaptable implements TreeAdaptable {
	private String file;
	private String node;
	private Object obj;

	public HDF5Adaptable(String filePath, String nodePath, Object object) {
		file = filePath;
		node = nodePath;
		obj  = object;
	}

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public String getNode() {
		return node;
	}

	@Override
	public NodeLink getNodeLink() {
		return (NodeLink) (obj instanceof NodeLink ? obj : null);
	}

	@Override
	public Attribute getAttribute() {
		return (Attribute) (obj instanceof Attribute ? obj : null);
	}
}
