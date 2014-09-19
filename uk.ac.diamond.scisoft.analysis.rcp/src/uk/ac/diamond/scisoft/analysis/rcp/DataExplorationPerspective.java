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

import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

public class DataExplorationPerspective implements IPerspectiveFactory {

	/**
	 * ID used elsewhere, do not change
	 */
	public static final String ID = "uk.ac.diamond.scisoft.dataexplorationperspective";
	final static String METADATAPAGE_ID = "uk.ac.diamond.sda.meta.MetadataPageView";
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		
		IFolderLayout navigatorLayout = layout.createFolder("navigators", IPageLayout.LEFT, 0.15f, editorArea);
		String explorer = "org.eclipse.ui.navigator.ProjectExplorer";
		navigatorLayout.addView(explorer);
		navigatorLayout.addView("uk.ac.diamond.sda.navigator.views.FileView");
		
		if (layout.getViewLayout(explorer) != null)
			layout.getViewLayout(explorer).setCloseable(false);

		IFolderLayout dataLayout = layout.createFolder("data", IPageLayout.RIGHT, 0.25f, editorArea);
		String plot = PlotView.ID + "DP";
		dataLayout.addView(plot);
		
		layout.addView(plot, IPageLayout.RIGHT, 0.30f, editorArea);
		if (layout.getViewLayout(plot) != null)
			layout.getViewLayout(plot).setCloseable(true);

		IFolderLayout metaFolderLayout = layout.createFolder("toolPageFolder", IPageLayout.RIGHT, 0.6f, plot);
		metaFolderLayout.addView(METADATAPAGE_ID);

		String inspector = DatasetInspectorView.ID;
		layout.addStandaloneView(inspector, false, IPageLayout.BOTTOM, 0.50f, editorArea);
		if (layout.getViewLayout(inspector) != null)
			layout.getViewLayout(inspector).setCloseable(false);
		
	}
}
