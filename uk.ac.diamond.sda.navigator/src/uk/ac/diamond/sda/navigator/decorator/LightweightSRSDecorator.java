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

package uk.ac.diamond.sda.navigator.decorator;

import java.util.ArrayList;
import java.util.List;

import gda.analysis.io.ScanFileHolderException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
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
				if (lazyData instanceof AbstractDataset)
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
