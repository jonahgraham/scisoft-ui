/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
