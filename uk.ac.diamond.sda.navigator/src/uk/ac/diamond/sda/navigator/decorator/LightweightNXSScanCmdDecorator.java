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

package uk.ac.diamond.sda.navigator.decorator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
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
