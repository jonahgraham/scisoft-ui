/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.util;

import java.io.File;
import java.util.Comparator;

/**
 *
 */
public class FileComparator implements Comparator<File> {

	private FileCompareMode compareMode;
	
	public FileComparator(FileCompareMode compareMode) {
		this.compareMode = compareMode;
	}
	
	@Override
	public int compare(File o1, File o2) {
		switch(compareMode) {
		case datetime:
			if (o1.lastModified() == o2.lastModified())
					return 0;
			if (o1.lastModified() < o2.lastModified())
				return -1;
			return 1;
		case name:
			return o1.compareTo(o2);		
		}
		return 0;
	}

}
