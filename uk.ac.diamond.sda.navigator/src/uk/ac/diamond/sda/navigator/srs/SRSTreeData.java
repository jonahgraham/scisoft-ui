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

package uk.ac.diamond.sda.navigator.srs;

import org.eclipse.core.resources.IFile;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

/**
 * Provides an SRS model of a name,min,max and class set from a *.dat file.
 */
public class SRSTreeData {

	private IFile container;
	private String name;
	private String minValue;
	private String maxValue;
	private String classValue;

	protected DataHolder data;

	/**
	 * Create a property with the given name and value contained by the given file.
	 * 
	 * @param name
	 *            The name of the property.
	 * @param minValue
	 *            The minimum value of the property.
	 * @param maxValue
	 *            The maximum value of the property.
	 * @param classValue
	 *            The class value of the property.
	 * @param file
	 *            The file that defines this property.
	 */
	public SRSTreeData(String name, String minValue, String maxValue, String classValue, IFile file) {
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.classValue = classValue;
		this.container = file;
	}

	/**
	 * The name of this property.
	 * 
	 * @return The name of this property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the minimum value of the property in the file.
	 * 
	 * @return The minimum value of the property in the file.
	 */
	public String getMinValue() {
		return minValue;
	}

	/**
	 * Return the maximum value of the property in the file.
	 * 
	 * @return The maximum value of the property in the file.
	 */
	public String getMaxValue() {
		return maxValue;
	}

	/**
	 * Return the class value of the property in the file.
	 * 
	 * @return The class value of the property in the file.
	 */
	public String getClassValue() {
		return classValue;
	}

	/**
	 * The IFile that defines this property.
	 * 
	 * @return The IFile that defines this property.
	 */
	public IFile getFile() {
		return container;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SRSTreeData && ((SRSTreeData) obj).getName().equals(name);
	}

	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer(getName()).append(":").append(getMinValue()) //$NON-NLS-1$
				.append(":").append(getMaxValue()) //$NON-NLS-1$
				.append(":").append(getClassValue()); //$NON-NLS-1$
		return toString.toString();
	}
}