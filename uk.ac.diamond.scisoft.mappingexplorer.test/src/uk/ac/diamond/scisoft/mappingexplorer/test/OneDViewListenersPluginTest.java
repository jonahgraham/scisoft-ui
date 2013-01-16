/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.mappingexplorer.test;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.filesystem.FileSystemStructureProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.editors.HDF5TreeEditor;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDMappingView;

/**
 * Test the 2D mapping view
 */
public class OneDViewListenersPluginTest {

	static final long MAX_TIMEOUT_MS = 500;

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	// private String scratchFolder;

	/**
	 */
	@Before
	public void setUp() {
	}

	/**
	 */
	@After
	public void tearDown() {

	}

	/**
	 * @throws Exception
	 */
	@Test
	public final void testShowView() throws Exception {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test Project");
		if (!project.exists()) {
			project.create(null);
		}
		project.open(null);

		ImportOperation op = new ImportOperation(project.getFullPath(), new FileSystemStructureProvider(),
				new IOverwriteQuery() {

					@Override
					public String queryOverwrite(String pathString) {
						return pathString;
					}
				}, Arrays.asList(new File[] { getSourceDirectory() }));
		op.setCreateContainerStructure(false);
		op.run(null);
		// window.getActivePage().showView(TwoDMappingView.ID);

		IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject("Test Project").getFolder("mappingexptest")
				.getFile("5.nxs");
		IEditorPart mappingViewprovidereditor = window.getActivePage().openEditor(new FileEditorInput(file),
				MappingViewPageProvider.ID);
		PluginTestHelpers.delay(10000);
		IFile file2 = ResourcesPlugin.getWorkspace().getRoot().getProject("Test Project").getFolder("mappingexptest")
				.getFile("2495.nxs");
		HDF5TreeEditor hdf5TreeEditor = (HDF5TreeEditor) window.getActivePage().openEditor(new FileEditorInput(file2),
				HDF5TreeEditor.ID);
		PluginTestHelpers.delay(10000);
		
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//				.closeEditor(mappingViewprovidereditor, false);

		IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		PluginTestHelpers.delay(10000);
		
		TwoDMappingView mappingView = null;
		for (IViewReference iViewReference : viewReferences) {
			if (iViewReference.getId().equals(TwoDMappingView.ID) && iViewReference.getSecondaryId() == null) {
				mappingView = (TwoDMappingView) iViewReference.getPart(false);
			}
		}
		
		PluginTestHelpers.delay(3000000);
	}

	private File getSourceDirectory() {
		return new File(
				"/scratch/excalibur1/plugins/uk.ac.diamond.scisoft.mappingexplorer.test/testFiles/mappingexptest");
	}

}
