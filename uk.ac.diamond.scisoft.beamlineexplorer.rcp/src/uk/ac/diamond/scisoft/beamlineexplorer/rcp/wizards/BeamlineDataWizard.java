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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import uk.ac.diamond.scisoft.beamlineexplorer.rcp.natures.SingleLevelProjectNature;


public class BeamlineDataWizard extends Wizard implements INewWizard {
	private static final String BEAMLINE_DATA_WIZARD = "BeamlineDataWizard";
	public static final String DIALOG_SETTING_KEY_DIRECTORY = "directory";
	public static final String DIALOG_SETTING_KEY_FOLDER = "beamlinedata";
	public static final String DIALOG_SETTING_KEY_PROJECT = "project";
	private static final Logger logger = LoggerFactory.getLogger(BeamlineDataWizard.class);	
	private BeamlineDataWizardPage page;
	private ISelection selection;
	private String defaultDataLocation, defaultFolderName;
	
	public static boolean RECURSIVE_BROWSING = false;

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
		//final String folder = page.getFolder();
		
		File f = new File(directory);
		if (f.exists()){
		final Job loadDataProject = new Job("Load Beamline Data") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Importing content", 100);
				try {
					logger.debug("project: " + project);
					logger.debug("directory: " + directory);
					BeamlineDataProjectUtils.createImportProjectAndFolder(project, "beamlinedata", directory, SingleLevelProjectNature.NATURE_ID, null, monitor);
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

	public void setDataLocation(File selectedPath) {
		this.defaultDataLocation = selectedPath.getAbsolutePath();
		this.defaultFolderName   = selectedPath.getName();
	}
	

}
