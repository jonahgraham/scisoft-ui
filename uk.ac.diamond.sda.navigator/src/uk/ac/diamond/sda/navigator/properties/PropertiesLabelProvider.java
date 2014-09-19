/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * Provides a label and icon for objects of type {@link PropertiesTreeData}.
 * 
 */
public class PropertiesLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof PropertiesTreeData)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof PropertiesTreeData) {
			PropertiesTreeData data = (PropertiesTreeData) element;
			return data.getName() + "= " + data.getValue(); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public String getDescription(Object anElement) {
		if (anElement instanceof PropertiesTreeData) {
			PropertiesTreeData data = (PropertiesTreeData) anElement;
			return "Property: " + data.getName(); //$NON-NLS-1$
		}
		return null;
	}

}
