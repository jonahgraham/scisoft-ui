/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.hdf5;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.IDescriptionProvider;

import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;

/**
 * Provides a label and icon for objects of type {@HDF5NodeLink}.
 */
public class HDF5LabelProvider extends LabelProvider implements ILabelProvider, ILabelDecorator, IDescriptionProvider {
	
	public static final String ID = "uk.ac.diamond.sda.navigator.hdf5Decorator";
	
	@Override
	public Image getImage(Object element) {	
		if (element instanceof HDF5Attribute) {
			return  new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/hdf5/dataset.gif"));
		}		
		HDF5NodeLink link = (HDF5NodeLink) element;
		HDF5Node node = link.getDestination();
		if (node instanceof HDF5Dataset) {
			HDF5Dataset dataset = (HDF5Dataset) node;
			if (dataset.isString()) {
				return new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/hdf5/text.gif"));
			}
			//ILazyDataset data = dataset.getDataset();
			// data
			//if (data instanceof Dataset) {
			return  new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/hdf5/dataset.gif"));
			//} else {
			//	return new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/hdf5/dataset.gif"));
			//}				
		}
		return  new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/hdf5/folderopen.gif"));
	}

	@Override
	public String getText(Object element) {
		return (element instanceof HDF5Attribute)==true ? ((HDF5Attribute) element).getName()+" ":((HDF5NodeLink) element).getName()+" ";
	}

	@Override
	public String getDescription(Object element) {
		String msg = "";
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			return file.getName() + " " + file.getFullPath();
		}
		if (element instanceof HDF5Attribute) {
			HDF5Attribute attr = (HDF5Attribute) element;
			// name
			msg = attr.getName();
			return msg;
		}
		assert element instanceof HDF5NodeLink : "Not an attribute or a link";
		HDF5NodeLink link = (HDF5NodeLink) element;
		HDF5Node node = link.getDestination();
		msg = link.getName();
		if (node instanceof HDF5Dataset) {
			HDF5Dataset dataset = (HDF5Dataset) node;
			if (!dataset.isSupported()) {
				return "Not supported";
			}
		}
		return msg;
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		return image;
	}

	@Override
	public String decorateText(String label, Object element) {

		if (element instanceof HDF5Attribute) {
			HDF5Attribute attr = (HDF5Attribute) element;
			// class
			label += ("Attr ");
			// dimensions
			if (attr.getSize() > 1) {
				for (int i : attr.getShape()) {
					label += i + ", ";
				}
				if (label.length() > 2)
					label += label.substring(0, label.length() - 2)+" ";
			}
			// type
			label += attr.getTypeName()+" ";
			// data
			label += attr.getSize() == 1 ? attr.getFirstElement() +" " : attr.toString()+" ";
			return label;
		}
		assert element instanceof HDF5NodeLink : "Not an attribute or a link";
		HDF5NodeLink link = (HDF5NodeLink) element;
		HDF5Node node = link.getDestination();
		// class
		HDF5Attribute attr = node.getAttribute(HDF5File.NXCLASS);
		if(attr!=null)
			label += attr.getFirstElement()+" ";
		//label += attr.getFirstElement()+" ";
		if (node instanceof HDF5Dataset) {
			HDF5Dataset dataset = (HDF5Dataset) node;
			// class
			label+="SDS ";
			if (dataset.isString()) {
				label += dataset.getTypeName()+" ";
				label += dataset.getString()+" ";
				if (label.length() > 100) // restrict to 100 characters
					label = label.substring(0, 100) + "...";
				return label;
			}
			if (!dataset.isSupported()) {
				return label+"Not supported";
			}
			ILazyDataset data = dataset.getDataset();
			int[] shape = data.getShape();
			// dimensions
			for (int i : shape) {
				label += i + ", ";
			}
			if (label.length() > 2)
				label += label.substring(0, label.length()-2)+" ";
			// type
			label += dataset.getTypeName()+" ";
			// data
			if (data instanceof Dataset) {
				// show a single value
				label += ((Dataset) data).getString(0)+" ";
			} else {
				label += "Select to view";
			}
		}
		return label;
	}
}
