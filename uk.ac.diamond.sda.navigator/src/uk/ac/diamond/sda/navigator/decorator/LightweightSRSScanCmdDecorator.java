/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.january.metadata.IExtendedMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class LightweightSRSScanCmdDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "uk.ac.diamond.sda.navigator.srsScancmdDecorator";

	private static final String SRS_EXT = "dat"; //$NON-NLS-1$
	private IExtendedMetadata metaData;
	private String decorator = "";
	private static final Logger logger = LoggerFactory.getLogger(LightweightSRSScanCmdDecorator.class);

	public LightweightSRSScanCmdDecorator() {
		super();
	}
	
	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		decorator = "";
		if (element instanceof IFile) {
			IFile modelFile = (IFile) element;
			if (SRS_EXT.equals(modelFile.getFileExtension())) {
				IFile ifile = (IFile) element;
				
				if (!ifile.exists())           return;
				if (ifile.getLocation()==null) return;
				srsMetaDataLoader(ifile.getLocation().toString());

				try {
					if(metaData!=null){
						decorator = metaData.getScanCommand();
						if(decorator==null){
							decorator=" * Scan Command: N/A";
							decoration.addSuffix(decorator);
						}else{
							if (decorator.length() > 100) // restrict to 100 characters
								decorator = decorator.substring(0, 100) + "...";
							decorator = " * " + decorator;
							decoration.addSuffix(decorator);
						}
					} else {
						decorator=" * Scan Command: N/A";
						decoration.addSuffix(decorator);
						logger.warn("Could not read metadata from file {}",ifile.getFullPath());
					}
				}catch (Exception e) {
					logger.error("Could not read metadata from {}: ", e);
				}
			}
		}
	}
	
	public IExtendedMetadata srsMyMetaDataLoader(String fullpath){
		srsMetaDataLoader(fullpath);
		return metaData;
	}
	
	private void srsMetaDataLoader(String fullpath) {
		
		try {
			IMetadata metaDataTest=LoaderFactory.getMetadata(fullpath, null);
			if(metaDataTest instanceof IExtendedMetadata)
				metaData = (IExtendedMetadata)LoaderFactory.getMetadata(fullpath, null);
			else{
				decorator=" * Scan Command: N/A";
				logger.warn("Cannot decorate SRS decorator");
			}
		} catch (Exception ne) {
			logger.error("Cannot open dat file", ne);
		}
	}
}
