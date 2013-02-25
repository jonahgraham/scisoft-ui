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

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.projects;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HideDeepProjects extends ViewerFilter {

	private static Logger logger = LoggerFactory.getLogger(HideDeepProjects.class);
	
	static final String BEAMLINE_RECURSIVE_CONTROLLED = uk.ac.diamond.scisoft.beamlineexplorer.rcp.natures.BeamlineRecursiveControlledNature.NATURE_ID;
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
	
		if(element instanceof IResource){

			boolean isTopRecursiveControlled = false;
			try {
				if(((IResource)element).getProject().isAccessible()){
					if( ((IResource)element).getParent().getProject() != null)
						isTopRecursiveControlled = ((IResource)element).getParent().getProject().hasNature(BEAMLINE_RECURSIVE_CONTROLLED);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}

			if  (isTopRecursiveControlled){
				if (((IResource)element).getParent().isLinked()){
					
					// get project persistent property
					QualifiedName qRecursiveBrowsingStatus = new QualifiedName("RECURSIVE.VIEWING", "String");
					boolean showFilesOnly = false;
					try {
						showFilesOnly = Boolean.valueOf(((IResource)element).getParent().getParent().getPersistentProperty(qRecursiveBrowsingStatus));
					} catch (CoreException e) {
						logger.error("can't extract persistent property");
					}
					
					if (showFilesOnly && !(((IResource)element) instanceof IFile)){
						return false;
					}
					
				}

			}
		}// isTopRecursiveControlled
		
		return true;
	}
		
}
