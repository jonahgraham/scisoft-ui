/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class BeamlineRecursiveControlledNature implements IProjectNature {

	public static final String NATURE_ID = "uk.ac.diamond.scisoft.beamlineexplorer.rcp.recursivecontrolled"; //$NON-NLS-1$
	
    private IProject project;
	
    @Override
    public void configure() throws CoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deconfigure() throws CoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;

    }

}
