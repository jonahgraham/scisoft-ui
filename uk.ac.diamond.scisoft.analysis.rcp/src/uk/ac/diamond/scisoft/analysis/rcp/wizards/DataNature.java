/*-
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
package uk.ac.diamond.scisoft.analysis.rcp.wizards;

import org.dawb.common.ui.project.XMLBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.dawb.common.ui.util.EclipseUtils;

public class DataNature implements IProjectNature {

	private IProject project;
	/**
	 * 
	 */
	public static String ID = "uk.ac.diamond.scisoft.analysis.rcp.DataProjectNature";

	@Override
	public void configure() throws CoreException {
		
		if (project==null) return;
		
		EclipseUtils.addBuilderToProject(project, XMLBuilder.ID);
	}

	@Override
	public void deconfigure() throws CoreException {
		project = null;
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
