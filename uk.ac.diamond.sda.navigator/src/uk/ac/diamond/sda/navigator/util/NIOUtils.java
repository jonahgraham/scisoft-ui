/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.sda.navigator.util;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NIOUtils {

	private static List<Path> roots;
	
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
