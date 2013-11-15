/*
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Replaced by MockConfigElem
 */
@Deprecated
class MockViewDescriptor implements IViewDescriptor {
	public static final String UK_AC_DIAMOND_TEST_VIEW = "uk.ac.diamond.test.view.";
	private final String label;

	public MockViewDescriptor(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getId() {
		return UK_AC_DIAMOND_TEST_VIEW + label;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public IViewPart createView() throws CoreException {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public String[] getCategoryPath() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public String getDescription() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public float getFastViewWidthRatio() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public boolean getAllowMultiple() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

	@Override
	public boolean isRestorable() {
		throw new AssertionFailedError("Methods in MockViewDescriptor should not be called");
	}

}
