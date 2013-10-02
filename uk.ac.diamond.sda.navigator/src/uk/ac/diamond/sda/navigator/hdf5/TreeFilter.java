/*
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.sda.navigator.hdf5;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class to act as a filter for nodes of tree
 */
public class TreeFilter {
	Collection<String> unwantedNodeNames;

	/**
	 * Constructor that needs an array of the names of unwanted nodes
	 *
	 * @param names
	 */
	public TreeFilter(String[] names) {
		unwantedNodeNames = new HashSet<String>();

		for (String n: names)
			unwantedNodeNames.add(n);
	}

	/**
	 * @param node
	 * @return true if node is not of those unwanted
	 */
	public boolean select(String node) {
		return !unwantedNodeNames.contains(node);
	}
}