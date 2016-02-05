/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.mappingexplorer.perspective.MappingExplorerPerspective;

/**
 * Test to ensure that the Mapping explorer perspective is available and can be opened
 */

public class MappingExplorerPerspectiveTest {

	@Test
	public final void testOpenPerspective() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPerspectiveDescriptor[] perspectives = window.getWorkbench().getPerspectiveRegistry().getPerspectives();
		IPerspectiveDescriptor descriptorToBeOpened = null;
		for (IPerspectiveDescriptor descriptor : perspectives) {
			if (descriptor.getId().equals(MappingExplorerPerspective.ID)) {
				descriptorToBeOpened = descriptor;
				break;
			}
		}
		IViewReference[] viewReferences = window.getActivePage().getViewReferences();
		for (IViewReference iViewReference : viewReferences) {
			if (iViewReference.getPartName().equals("Welcome")) {
				window.getActivePage().hideView(iViewReference);
				break;
			}
		}

		window.getActivePage().setPerspective(descriptorToBeOpened);
		Assert.assertNotNull("Perspective 'Mapping Explorer' not found", descriptorToBeOpened);
	}
}
