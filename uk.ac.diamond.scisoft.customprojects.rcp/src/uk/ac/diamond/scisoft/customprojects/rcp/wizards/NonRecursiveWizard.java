/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.customprojects.rcp.wizards;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.customprojects.rcp.internal.CustomProjectActivator;
import uk.ac.diamond.scisoft.customprojects.rcp.natures.SingleLevelProjectNature;


public class NonRecursiveWizard extends Wizard implements INewWizard {
	private static final String NON_RECURSIVE_WIZARD = "NonRecursiveWizard";
	public static final String DIALOG_SETTING_KEY_DIRECTORY = "directory";
	public static final String DIALOG_SETTING_KEY_FOLDER = "folder";
	public static final String DIALOG_SETTING_KEY_PROJECT = "project";
	private static final Logger logger = LoggerFactory.getLogger(NonRecursiveWizard.class);	
	private NonRecursiveWizardPage page;
	private ISelection selection;
	private String defaultDataLocation, defaultFolderName;

	/**
	 * Constructor for TestWizard.
	 */
	public NonRecursiveWizard() {
		super();
		setNeedsProgressMonitor(true);
		IDialogSettings dialogSettings = CustomProjectActivator.getDefault().getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(NON_RECURSIVE_WIZARD);
		if(section == null){
			section = dialogSettings.addNewSection(NON_RECURSIVE_WIZARD);
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
		
		page = new NonRecursiveWizardPage(selection, prevProject, prevFolder, prevDirectory);
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
		final String folder = page.getFolder();

		final Job loadDataProject = new Job("Load non-recursive project") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Importing content", 100);
				try {
					NonRecursiveProjectUtils.createImportProjectAndFolder(project, folder, directory, SingleLevelProjectNature.NATURE_ID, null, monitor);
				} catch (CoreException e) {
					logger.error("Error creating project " + project, e);
					return new Status(IStatus.ERROR, CustomProjectActivator.PLUGIN_ID, "Error creating project " + project);
				}
				return new Status(IStatus.OK, CustomProjectActivator.PLUGIN_ID, "Project " + project + " created");
			}
		};

		loadDataProject.setUser(true);
		loadDataProject.setPriority(Job.DECORATE);
		loadDataProject.schedule(100);
		

		IDialogSettings settings = getDialogSettings();
		if( settings != null){
			settings.put(DIALOG_SETTING_KEY_PROJECT, project);
			settings.put(DIALOG_SETTING_KEY_FOLDER, folder);
			settings.put(DIALOG_SETTING_KEY_DIRECTORY, directory);
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
