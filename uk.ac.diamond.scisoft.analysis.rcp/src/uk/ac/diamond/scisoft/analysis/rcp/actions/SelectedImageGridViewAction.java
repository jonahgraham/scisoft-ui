/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.util.io.SortingUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

public class SelectedImageGridViewAction extends AbstractHandler implements IObjectActionDelegate {

	private static Logger logger = LoggerFactory.getLogger(SelectedImageGridViewAction.class);
	
	@Override
	public void run(IAction action) {
		doAction();		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doAction();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}
	
	private Object doAction() {
		
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IStructuredSelection sel = (IStructuredSelection)page.getSelection();
		
		Job job = new Job("Updating image Explorer View") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				if (sel != null) {
					
					Object[] selObjects = sel.toArray();
					if (selObjects==null) return Status.CANCEL_STATUS;
					
					ArrayList<String> files = new ArrayList<String>();
					try {
						if (selObjects.length==1) {
							
							File dir = null;
							if (selObjects[0] instanceof IFolder) {
								dir = new File(((IFolder)selObjects[0]).getLocation().toOSString());
							   
							} else if (selObjects[0] instanceof File) {
								final File file = (File)selObjects[0];
								if (file.isDirectory()) {
									dir = file;
								}
							}
							
							if (dir!=null) {
								final List<File> fa = SortingUtils.getSortedFileList(dir);
								if (fa!=null && fa.size()>0) {
									for (File file : fa) {
										if (!file.isDirectory()) files.add(file.getAbsolutePath());
									}
								}
							}
						}
					} catch (Exception ne) {
						logger.error("Cannot open selected objects as folder!", ne);
						return Status.CANCEL_STATUS;
					}
					
					if (files.isEmpty()) {
						for (Object obj : selObjects) {
							if(obj instanceof IFile) {
								IFile file = (IFile)obj;
								String path = file.getLocation().toString();
								files.add(path);
							} else if (obj instanceof File) {
								File file = (File)obj;
								String path = file.getAbsolutePath();
								files.add(path);
							}
						}
					}
					
					try {
						SDAPlotter.setupNewImageGrid("Image Explorer", files.size());
					} catch (Exception e1) {
						logger.debug("Problem with SDAPlotter.setupNewImageGrid",e1);
						return Status.CANCEL_STATUS;
					}
					for (String path : files) {						
						try {
							SDAPlotter.plotImageToGrid("Image Explorer", path);
						} catch (Exception e) {
							logger.debug("Problem with SDAPlotter.plotImageToGrid()",e);
							return Status.CANCEL_STATUS;
						}
					}
				} else
					return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
		return Boolean.TRUE;
	}

}
