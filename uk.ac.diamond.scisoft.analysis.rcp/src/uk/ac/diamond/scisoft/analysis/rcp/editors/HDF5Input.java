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

package uk.ac.diamond.scisoft.analysis.rcp.editors;

import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class HDF5Input implements IEditorInput {
	
	private IEditorInput input;
	private Tree tree;

	public HDF5Input(IEditorInput in, Tree t) {
		input = in;
		tree = t;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return input.getAdapter(adapter);
	}

	@Override
	public boolean exists() {
		return input.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return input.getImageDescriptor();
	}

	@Override
	public String getName() {
		return input.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return input.getPersistable();
	}

	@Override
	public String getToolTipText() {
		return input.getToolTipText();
	}

	public Tree getTree() {
		return tree;
	}

	public IEditorInput getInput() {
		return input;
	}
}
