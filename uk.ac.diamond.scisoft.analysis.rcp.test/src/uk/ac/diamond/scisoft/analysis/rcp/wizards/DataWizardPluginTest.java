/*
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

package uk.ac.diamond.scisoft.analysis.rcp.wizards;


import junit.framework.Assert;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
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
	public final void testWizard() throws Exception {
		
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
