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
import org.eclipse.core.runtime.IPath;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class LightweightNXSScanCmdDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String ID = "uk.ac.diamond.sda.navigator.nxsScancmdDecorator";

	private static final String NXS_EXT = "nxs"; //$NON-NLS-1$
	private String decorator = "";

	public LightweightNXSScanCmdDecorator() {
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
			IPath modelFilePath = modelFile.getLocation();
			if (modelFilePath != null && modelFilePath.toFile().canRead() && NXS_EXT.equals(modelFile.getFileExtension())) {
				IFile ifile = (IFile) element;

				String[][] listTitlesAndScanCmd = NavigatorUtils.getHDF5TitlesAndScanCmds(ifile.getLocation().toString());
				for (int i = 0; i < listTitlesAndScanCmd[0].length; i++) {
					decorator = listTitlesAndScanCmd[0][i] + listTitlesAndScanCmd[1][i];
					decoration.addSuffix(decorator);
				}
			}
		}		
	}

}
