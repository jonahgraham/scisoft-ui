/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.customprojects.rcp.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.customprojects.rcp.natures.SingleLevelProjectNature;


public class CustomProjectSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomProjectSupport.class);

	
    public static IProject createProject(String projectName, URI location) {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0 );
        
        IProject project = createBaseProject(projectName, location);
        try {
            logger.debug("adding nature");
        	addNature(project);
            
            //String[] paths = { "parent/child1-1/child2", "parent/child1-2/child2/child3" }; //$NON-NLS-1$ //$NON-NLS-2$
            //addToProjectStructure(project, paths);        	
        	
        } catch (CoreException e) {
            e.printStackTrace();
            project = null;
        }

        return project;
    }


    private static IProject createBaseProject(String projectName, URI location) {
    	logger.debug("projectName= " + projectName + "  location= " + location);
        
   	
    	// it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

        if (!newProject.exists()) {
            URI projectLocation = location;

            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}
                       
            if(projectLocation != null){
            desc.setLocationURI(projectLocation);
            }
            
            logger.debug("projectLocation: {}", projectLocation == null ? null : projectLocation.getPath());
            try {
                newProject.create(desc, null);
                
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        
        return newProject;
    }



    private static void addNature(IProject project) throws CoreException {
        if (!project.hasNature(SingleLevelProjectNature.NATURE_ID)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = SingleLevelProjectNature.NATURE_ID;
            description.setNatureIds(newNatures);

            IProgressMonitor monitor = null;
            project.setDescription(description, monitor);
        }
    }

}
