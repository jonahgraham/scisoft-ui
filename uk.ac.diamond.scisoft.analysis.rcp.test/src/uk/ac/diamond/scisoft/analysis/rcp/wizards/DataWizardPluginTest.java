/*-
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.wizards;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.utils.PluginTestHelpers;

public class DataWizardPluginTest {

	@Before
	public void setUp(){
		PluginTestHelpers.waitForJobs();
	}

	@After
	public void tearDown() {
		PluginTestHelpers.waitForJobs();
	}
	
	@Test
	public final void testWizard() {
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		DataWizard wizard = new DataWizard();
		wizard.init(window.getWorkbench(), StructuredSelection.EMPTY);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		int open = dialog.open();
		{
			//open again to check if setting tranferred across instantiations
			DataWizard wizard1 = new DataWizard();
			wizard1.init(window.getWorkbench(), StructuredSelection.EMPTY);
			WizardDialog dialog1 = new WizardDialog(window.getShell(), wizard1);
			dialog1.open();
		}
		if ( open == Window.OK){
			PluginTestHelpers.delay(1000); //give time for project to be created

		}
	}
}
