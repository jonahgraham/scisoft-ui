/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.navigator.util;

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
		if (roots==null) roots = createList(FileSystems.getDefault().getRootDirectories());
		return roots;
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
