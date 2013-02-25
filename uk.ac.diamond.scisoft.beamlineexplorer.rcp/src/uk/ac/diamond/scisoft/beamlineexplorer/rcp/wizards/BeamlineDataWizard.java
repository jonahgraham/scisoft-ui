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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.beamlineexplorer.rcp.internal.BeamlineExplorerProjectActivator;
import uk.ac.diamond.scisoft.beamlineexplorer.rcp.natures.BeamlineRecursiveControlledNature;


public class BeamlineDataWizard extends Wizard implements INewWizard {
	private static final String BEAMLINE_DATA_WIZARD = "BeamlineDataWizard";
	public static final String DIALOG_SETTING_KEY_DIRECTORY = "directory";
	public static final String DIALOG_SETTING_KEY_FOLDER = "beamlinedata";
	public static final String DIALOG_SETTING_KEY_PROJECT = "project";
	private static final Logger logger = LoggerFactory.getLogger(BeamlineDataWizard.class);	
	private BeamlineDataWizardPage page;
	private ISelection selection;
	private String defaultDataLocation, defaultFolderName;
	
	public static boolean SHOW_FILES_ONLY = false;

	/**
	 * Constructor for TestWizard.
	 */
	public BeamlineDataWizard() {
		super();
		setNeedsProgressMonitor(true);
		IDialogSettings dialogSettings = BeamlineExplorerProjectActivator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(BEAMLINE_DATA_WIZARD);
		if(section == null){
			section = dialogSettings.addNewSection(BEAMLINE_DATA_WIZARD);
		}
		setDialogSettings(section);
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		String prevProject = null , prevFolder = null, prevDirectory = null;
		IDialogSettings  settings = getDialogSettings();
		if( settings != null){
			prevProject = settings.get(DIALOG_SETTING_KEY_PROJECT);
			prevFolder = settings.get(DIALOG_SETTING_KEY_FOLDER);
			prevDirectory = settings.get(DIALOG_SETTING_KEY_DIRECTORY);
		}
		if (defaultDataLocation!=null) {
			prevDirectory = defaultDataLocation;
		}
		if (defaultFolderName!=null) {
			prevFolder = defaultFolderName;
		}
		
		page = new BeamlineDataWizardPage(selection, prevProject, prevFolder, prevDirectory);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
	
		final String project = page.getProject();
		final String directory = page.getDirectory();
		final boolean showFilesOnly = page.showFilesOnly();
		//final String folder = page.getFolder();
		
		SHOW_FILES_ONLY = showFilesOnly;
		
		File f = new File(directory);
		if (f.exists()){
		final Job loadDataProject = new Job("Load Beamline Data") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Importing content", 100);
				try {
						// create project
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

						IProject iproject = root.getProject(project);
						iproject.create(monitor);
						iproject.open(monitor);
						
						// adding persistent properties needed to differentiate between multiple beamline data projects
						QualifiedName qRecursiveBrowsingStatus = new QualifiedName("RECURSIVE.VIEWING", "String");
						iproject.setPersistentProperty(qRecursiveBrowsingStatus, String.valueOf(SHOW_FILES_ONLY));
				
						// associating the beamlineData nature to the newly created project
						try {
							IProjectDescription description = iproject.getDescription();
							String[] natures = description.getNatureIds();
							String[] newNatures = new String[natures.length + 1];
							System.arraycopy(natures, 0, newNatures, 0, natures.length);

							newNatures[natures.length] = BeamlineRecursiveControlledNature.NATURE_ID;
							description.setNatureIds(newNatures);
							iproject.setDescription(description, monitor);

						} catch (CoreException e) {
							logger.error("problem setting BeamlineData Project nature to project: " + iproject.getName() + " - Error: " + e);
						}
						
						logger.debug("BeamlineData project created: " + project);

						// create link into project
						if (iproject.findMember(directory) == null) {
							final IFolder src = iproject.getFolder("beamlineData");
							src.createLink(new Path(directory), IResource.DEPTH_ZERO, monitor);
						}
						
						// refresh
						//iproject.refreshLocal(IResource.BACKGROUND_REFRESH, null);
						logger.debug("project structure for '" + iproject.getName() + "' created.");
					
				} catch (CoreException e) {
					logger.error("Error creating project " + project, e);
					return new Status(IStatus.ERROR, BeamlineExplorerProjectActivator.PLUGIN_ID, "Error creating project " + project + "\n folder '"+ directory + "' does not exit on file system");
				}
				return new Status(IStatus.OK, BeamlineExplorerProjectActivator.PLUGIN_ID, "Project " + project + " created");
			}
		};

		loadDataProject.setUser(true);
		loadDataProject.setPriority(Job.DECORATE);
		loadDataProject.schedule(100);
		

		IDialogSettings settings = getDialogSettings();
		if( settings != null){
			settings.put(DIALOG_SETTING_KEY_PROJECT, project);
			//settings.put(DIALOG_SETTING_KEY_FOLDER, folder);
			settings.put(DIALOG_SETTING_KEY_DIRECTORY, directory);
		}
	}else //directory does not exist in file system
	{
		logger.error("Data directory does not exist on file system: " + directory);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().asyncExec
			    (new Runnable() {
			        @Override
					public void run() {
			            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			            		.getShell(),"Error","Data directory does not exist on file system:\n" + directory);
			            }
			    });
	}
		return true;
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialise from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}
