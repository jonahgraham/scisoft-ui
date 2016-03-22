/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.python.pydev.ui.perspective.PythonPerspectiveFactory;

public class JythonPerspective implements IPerspectiveFactory {
	
	public static final String ID = "uk.ac.diamond.scisoft.jythonperspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
			
		// get the editor area
		String editorArea = layout.getEditorArea();
		
		IFolderLayout navigatorLayout = layout.createFolder("navigators", IPageLayout.LEFT, 0.15f, editorArea);
		navigatorLayout.addView("org.python.pydev.navigator.view");
		navigatorLayout.addView("org.dawnsci.fileviewer.FileViewer");

		// add plot and debug to the left
		IFolderLayout debugLayout = layout.createFolder("debug", IPageLayout.RIGHT, 0.6f, editorArea);
		debugLayout.addView("org.eclipse.debug.ui.VariableView");
		debugLayout.addView("org.eclipse.debug.ui.ExpressionView");
		debugLayout.addView("org.eclipse.debug.ui.DebugView");
		
		IFolderLayout plotLayout = layout.createFolder("plot", IPageLayout.BOTTOM, 0.3f, "debug");
		plotLayout.addView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");
		
		// add the console and the outline to the bottom
		IFolderLayout bottomLayout = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.6f, editorArea);
		bottomLayout.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottomLayout.addView(IPageLayout.ID_OUTLINE);
		bottomLayout.addView("org.dawb.common.ui.views.headerTableView");
		
		// Finaly add all the Pydev actions as are required for running stuff etc.
		(new PythonPerspectiveFactory()).defineActions(layout);

	}

}
