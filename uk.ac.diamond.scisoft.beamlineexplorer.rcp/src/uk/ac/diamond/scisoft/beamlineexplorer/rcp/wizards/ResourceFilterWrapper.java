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

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.wizards;


import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IResourceFilterDescription;

/**
 * Class used to hold resource filter definitions See IContainer.createFilter for details of arguments
 */
public class ResourceFilterWrapper {
	public int type;
	public FileInfoMatcherDescription fileInfoMatcherDescription;

	public ResourceFilterWrapper(int type, FileInfoMatcherDescription fileInfoMatcherDescription) {
		super();
		this.type = type;
		this.fileInfoMatcherDescription = fileInfoMatcherDescription;
	}

	public static ResourceFilterWrapper createImportProjectAndFolderFilter(int type,
			FileInfoMatcherDescription fileInfoMatcherDescription) {
		return new ResourceFilterWrapper(type, fileInfoMatcherDescription);
	}

	public static ResourceFilterWrapper createRegexResourceFilterWrapper(int type, String argument) {
		return createImportProjectAndFolderFilter(type, new FileInfoMatcherDescription(
				"org.eclipse.core.resources.regexFilterMatcher", argument)); //$NON-NLS-1$
	}

	public static ResourceFilterWrapper createRegexFolderFilter(String argument, boolean folders, boolean exclude) {
		return createRegexResourceFilterWrapper((exclude ? IResourceFilterDescription.EXCLUDE_ALL
				: IResourceFilterDescription.INCLUDE_ONLY)
				| (folders ? IResourceFilterDescription.FOLDERS : IResourceFilterDescription.FILES)
				| IResourceFilterDescription.INHERITABLE, argument);
	}

}