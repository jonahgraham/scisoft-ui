/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.projects;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 */
public class DataFilesFilter implements FilenameFilter {


	private static final String LISTOFSUFFIX[] = {"png","jpg","tif{1,2}","mar","cbf","dat",
        "img","raw","mccd","cif","imgcif","nxs"};
	
	@Override
	public boolean accept(File dir, String name) {
		if (dir.isDirectory()) return true;
		for (int i = 0; i < LISTOFSUFFIX.length; i++)
		if (name.endsWith(LISTOFSUFFIX[i]))
			return true;
		return false;
	}

}
