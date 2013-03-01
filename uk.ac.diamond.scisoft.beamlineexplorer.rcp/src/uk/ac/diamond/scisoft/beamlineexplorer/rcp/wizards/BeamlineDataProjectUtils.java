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

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.beamlineexplorer.rcp.internal.BeamlineExplorerProjectActivator;


public class BeamlineDataProjectUtils {
	
	private static Logger logger = LoggerFactory.getLogger(BeamlineDataProjectUtils.class);

	public static IProject createImportProjectAndFolder(final String projectName, final String folderName,
			final String importFolder, final String natureId, final List<ResourceFilterWrapper> resourceFilterWrappers,
			IProgressMonitor monitor) throws CoreException {

		File file = new File(importFolder);
		if(!file.exists())
			throw new CoreException(new Status(IStatus.ERROR, BeamlineExplorerProjectActivator.PLUGIN_ID, 
					"Unable to create project folder " + projectName + "." + folderName + " as folder " + importFolder + " does not exist "));
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();

				IProject project = root.getProject(projectName);
				if (!project.exists()) {
					monitor.subTask("Creating project :" + projectName);
					project.create(monitor);
					if (natureId != null) {
						project.open(monitor);
						IProjectDescription description = project.getDescription();
						description.setNatureIds(new String[] { natureId });
						project.setDescription(description, monitor);
					}
				}else{
					logger.error("Project exists already: " + project.getName());
				}

				project.open(monitor);
				
				if (project.findMember(folderName) == null) {
					final IFolder src = project.getFolder(folderName);
					src.createLink(new Path(importFolder), IResource.BACKGROUND_REFRESH,
							monitor);

					if (resourceFilterWrappers != null) {
						for (ResourceFilterWrapper wrapper : resourceFilterWrappers) {
							src.createFilter(wrapper.type, wrapper.fileInfoMatcherDescription,
									IResource.BACKGROUND_REFRESH, monitor);
						}
					}
				}

			}
		};
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		workspace.run(runnable, workspace.getRuleFactory().modifyRule(root), IResource.NONE, monitor);
		
		return root.getProject(projectName);
	}

	public static void addRemoveNature(IProject project, IProgressMonitor monitor, boolean add, String natureId) throws CoreException{
		IProjectDescription description = project.getDescription();
		boolean hasNature = project.hasNature(natureId);
		String [] newNatures=null;
		if( add ){
			if( !hasNature){
				String[] natures = description.getNatureIds();
				newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = natureId;
			}
		} else {
			if( hasNature){
				String[] natures = description.getNatureIds();
				Vector<String> v_newNatures= new  Vector<String>();
				for(int i=0; i< natures.length; i++){
					if( !natures[i].equals(natureId))
						v_newNatures.add(natures[i]);
				}
				newNatures = v_newNatures.toArray(new String[0]);
			}
		}
		if( newNatures != null){
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
	}
	
}
