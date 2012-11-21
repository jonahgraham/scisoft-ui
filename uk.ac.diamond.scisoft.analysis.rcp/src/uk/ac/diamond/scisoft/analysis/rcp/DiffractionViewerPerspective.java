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

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;
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
		
		
		if (layout.getViewLayout("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView") != null)
			layout.getViewLayout("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView").setCloseable(false);
		
	}

}
