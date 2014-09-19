/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.decorator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ExtendedSRSLoader;
import uk.ac.diamond.scisoft.analysis.io.SRSLoader;
import uk.ac.diamond.sda.navigator.srs.SRSTreeData;

/**
 * Class used to decorate each of the sub-element of a DAT file with the corresponding data value (max, min, class...)
 * Not yet implemented
 */
public class LightweightSRSDecorator extends LabelProvider implements ILightweightLabelDecorator {

	private String decorator;
	private String fileName;
	private static final Logger logger = LoggerFactory.getLogger(LightweightSRSDecorator.class);
	private SRSTreeData srsData;
	private DataHolder data;

	@Override
	public void decorate(Object element, IDecoration decoration) {
		setDecorator("");
		if (element instanceof SRSTreeData) {
			srsData = (SRSTreeData) element;
			IFile ifile = srsData.getFile();

			List<SRSTreeData> properties = new ArrayList<SRSTreeData>();
			String[] names = data.getNames();

			for (int i = 0; i < data.size(); i++) {
				ILazyDataset lazyData = data.getLazyDataset(i);
				if (lazyData instanceof Dataset)
					properties.add(new SRSTreeData(names[i], data.getDataset(i).min().toString(), 
							data.getDataset(i).max().toString(),
							data.getDataset(i).elementClass().toString(), ifile));
				else {
					properties.add(new SRSTreeData(names[i], "Not available", "Not available", "Not available", ifile));
				}
			}
			//SRSTreeData[] srsTreeData = (SRSTreeData[]) properties.toArray(new SRSTreeData[properties.size()]);

		}
	}

	/**
	 * Method that calls the SRSLoader class to load a .dat file
	 * 
	 * @param file
	 *            The .dat file to open
	 */
	public void srsFileLoader(IFile file) {
		fileName = file.getLocation().toString();
		try {
			SRSLoader dataLoader = new ExtendedSRSLoader(fileName);
			data = dataLoader.loadFile();
		} catch (ScanFileHolderException e) {
			data = new DataHolder();
			data.addDataset("Failed to load File", new DoubleDataset(1));
			logger.warn("Failed to load srs file");
		}
	}

	public String getDecorator() {
		return decorator;
	}

	public void setDecorator(String decorator) {
		this.decorator = decorator;
	}

}
