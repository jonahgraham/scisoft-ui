/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.srs;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * Provides a label and icon for objects of type {@link SRSTreeData}.
 */
public class SRSLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider, ILabelDecorator {

	@Override
	public Image getImage(Object element) {
		if (element instanceof SRSTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof SRSTreeData) {
			SRSTreeData data = (SRSTreeData) element;
			return data.getName();
		}
		return null;
	}

	@Override
	public String getDescription(Object anElement) {
		if (anElement instanceof SRSTreeData) {
			SRSTreeData data = (SRSTreeData) anElement;
			return "Property: " + data.getName(); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		if (element instanceof SRSTreeData)
			return image;
		return null;
	}

	@Override
	public String decorateText(String label, Object element) {
		if (element instanceof SRSTreeData)
			return label;
		return null;
	}

}
