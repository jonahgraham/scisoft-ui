/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.diamond.sda.navigator.srs;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IDescriptionProvider;

import uk.ac.diamond.scisoft.analysis.rcp.navigator.srs.SRSTreeData;

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
		System.out.println("element: "+element.toString());
		if (element instanceof SRSTreeData) {
			SRSTreeData data = (SRSTreeData) element;
			System.out.println("d: "+data.getName());
			return data.getName(); //+ ": min=" + data.getMinValue() //$NON-NLS-1$
					//+ ", max=" + data.getMaxValue(); //$NON-NLS-1$
			// + ", Class=" + data.getClassValue();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String decorateText(String label, Object element) {
		if (element instanceof SRSTreeData)
			return label+"";
		return null;
	}

}