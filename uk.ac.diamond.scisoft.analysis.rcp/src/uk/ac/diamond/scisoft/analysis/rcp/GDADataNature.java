/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class GDADataNature implements IProjectNature {
	
	protected IProject project;
	
	public final static String ID = "uk.ac.diamond.scisoft.analysis.rcp.GDADataNature";

	@Override
	public void configure() throws CoreException {
		
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	/**
	 * @return Returns the project.
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/**
	 * @param project The project to set.
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
