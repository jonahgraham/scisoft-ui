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
import org.eclipse.ui.IViewLayout;

public class DiffractionViewerPerspective implements IPerspectiveFactory {

	static final String METADATAPAGE_ID = "uk.ac.diamond.sda.meta.MetadataPageView";
	static final String ID = "uk.ac.diamond.scisoft.diffractionviewerperspective";
	@Override
	public void createInitialLayout(IPageLayout layout) {

		layout.setEditorAreaVisible(true);

		IFolderLayout navigatorLayout = layout.createFolder("navigatorFolder", IPageLayout.LEFT, 0.25f, layout.getEditorArea());
		navigatorLayout.addView("uk.ac.diamond.sda.navigator.views.FileView");
		navigatorLayout.addView("org.eclipse.ui.navigator.ProjectExplorer");

		IFolderLayout metadataLayout = layout.createFolder("metadataFolder", IPageLayout.BOTTOM, 0.65f, "navigatorFolder");
		metadataLayout.addView(METADATAPAGE_ID);
		IViewLayout metadataPageView = layout.getViewLayout(METADATAPAGE_ID);
		if (metadataPageView != null)
			metadataPageView.setCloseable(false);

		IFolderLayout explorerLayout = layout.createFolder("explorerFolder", IPageLayout.BOTTOM, 0.70f, layout.getEditorArea());
		explorerLayout.addView("uk.ac.diamond.scisoft.analysis.rcp.views.ImageExplorerView");
		explorerLayout.addPlaceholder("org.dawb.workbench.views.imageMonitorView");
		
		IFolderLayout toolPageLayout = layout.createFolder("toolPageFolder", IPageLayout.RIGHT, 0.50f, layout.getEditorArea());
		toolPageLayout.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawb.workbench.plotting.tools.diffraction.Diffraction");
		toolPageLayout.addPlaceholder("*");
		
		if (layout.getViewLayout("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView") != null)
			layout.getViewLayout("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView").setCloseable(false);
		
	}

}
