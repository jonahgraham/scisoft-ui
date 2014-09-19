/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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