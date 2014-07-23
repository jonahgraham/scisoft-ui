/*-
 * Copyright 2012 Diamond Light Source Ltd.
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
package uk.ac.diamond.sda.exporter.io;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.util.StringUtils;

public class EDXDDataExtractor {

	private static final Logger logger = LoggerFactory.getLogger(EDXDDataExtractor.class);

	@SuppressWarnings("unused")
	public static void EDXDDataExtract(final IFile dataFile, final Object[] dataSetNames, final String delimiter) {

		// final IFile file = EclipseUtils.getUniqueFile(dataFile, "dat");

		// final IProgressService service = PlatformUI.getWorkbench().getProgressService();
		// try {
		// service.run(true, true, new IRunnableWithProgress() {
		//
		// @Override
		// public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {

			String fullPath = "";
			ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getSelectionService();
			ISelection selection = selectionService.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof IResource) {
					fullPath = ((IResource) element).getFullPath().toString();
				}
			}
			fullPath = fullPath.substring(0, fullPath.length() - 4); // we replace the last 4 char by ""

			final IDataHolder dh = LoaderFactory.getData(dataFile.getLocation().toOSString());
			// , new ProgressMonitorWrapper(monitor));

			createDATFile(dh, "anewDATFile.dat");
			
			List<String> subFolders = StringUtils.getAllPathnames(dh.getNames(), "/");
			for (Iterator<String> iterator = subFolders.iterator(); iterator.hasNext();) {
				String name = iterator.next();
				// We create the folders
				createDirectory(fullPath + name);

				// We create the dat files
				ILazyDataset iLazyDataset = dh.getLazyDataset(name);
				if (iLazyDataset != null)
					createDATFile(dh, iLazyDataset.getName()+".dat");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
			// throw new InvocationTargetException(e, e.getMessage());
		}
		//
		// }
		// });
		// } catch (Exception ne) {
		// final String message = "The file '" + dataFile.getName() + "' was not converted to '" + file.getName()
		// + "'";
		// ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
		// "File Not Converted", ne.getMessage(), new Status(IStatus.WARNING, "uk.ac.diamond.sda.exporter",
		// message, ne));
		// }
	}

	private static void createDirectory(String directory) {
		try {
			IPath exportPath = new Path(directory);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFolder exportFolder = workspace.getRoot().getFolder(exportPath);
			exportFolder.create(true, true, null);
			//logger.info("Folder " + exportFolder.getName() + " created");
		} catch (Exception e) {
			logger.error("Error creating '" + directory +"' folder:"+ e.getMessage());
			e.printStackTrace();
		}
	}

	private static void createDATFile(IDataHolder dh, String filename) {
		try {
//			ASCIILazyDatasetSaver asciiWriter = new ASCIILazyDatasetSaver(filename);
//			asciiWriter.saveFile(dh);
			
			//logger.debug(dataset.getName());
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
