/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.util;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NIOUtils {

	private static List<Path> roots;
	
	public static final List<Path> getRoots(boolean clearCache) {
		if (clearCache) roots=null;
		return getRoots();
	}

	public static final List<Path> getRoots() {
		if (roots==null) {
			roots = createList(FileSystems.getDefault().getRootDirectories());
			
			if (System.getProperty("os.name").indexOf("Windows") == 0 && Boolean.getBoolean("uk.ac.diamond.sda.navigator.util.showBeamlines")) {
				List<Path> beamlines = getBeamlineRoots();
				if (beamlines!=null) roots.addAll(beamlines);
			}
		}
		return roots;
	}
	
	/**
	 * We search for beamline drives on \\Data.diamond.ac.uk\
	 * @return list of paths, or null
	 */
	private static List<Path> getBeamlineRoots() {
		
		// TODO FIXME - This is horrible.
		final List<Path> paths = new ArrayList<Path>(7);
		
		String base = "\\\\Data.diamond.ac.uk\\";
		for (int i = 1; i<=50; ++i) {
			
			String is = i<10 ? "0"+i : ""+i;
			File file = new File(base+"b"+is);
			if (file.exists()) paths.add(file.toPath());
			
			file = new File(base+"i"+is);
			if (file.exists()) paths.add(file.toPath());
			
			for (int j = 1; j < 10; j++) {
				file = new File(base+"b"+is+"-"+j);
				if (file.exists()) paths.add(file.toPath());
				
				file = new File(base+"i"+is+"-"+j);
				if (file.exists()) paths.add(file.toPath());
			}
		}
		if (paths.isEmpty()) return null;
		return paths;
	}

	private static final List<Path> createList(Iterable<Path> dirs) {
		List<Path> ret = new ArrayList<Path>(3);
		for (Path path : dirs) {
			if (!Files.isReadable(path)) continue;
			ret.add(path);
		}
		return ret;
	}

}
