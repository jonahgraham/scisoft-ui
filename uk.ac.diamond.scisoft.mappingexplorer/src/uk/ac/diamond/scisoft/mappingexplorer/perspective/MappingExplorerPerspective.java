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
package uk.ac.diamond.scisoft.mappingexplorer.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;
import uk.ac.diamond.scisoft.mappingexplorer.views.histogram.HistogramMappingView;
import uk.ac.diamond.scisoft.mappingexplorer.views.oned.OneDMappingView;
import uk.ac.diamond.scisoft.mappingexplorer.views.twod.TwoDMappingView;

/**
 * Mapping explorer as part of GDA-4659
 * 
 * @author rsr31645
 */
public class MappingExplorerPerspective implements IPerspectiveFactory {
	private static final String ONE_D_VIEW_FOLDER = "ONE_D_VIEW_FOLDER";
	private static final String TWO_D_VIEW_FOLDER = "TWO_D_VIEW_FOLDER";
	private static final String HISTOGRAM_VIEW_FOLDER = "HISTOGRAM_VIEW_FOLDER";
	private static final String FITTER_VIEW_FOLDER = "FITTER_VIEW_FOLDER";
	private static final String PROJECT_EXPLORER = "PROJECT_EXPLORER";
	/**
	 * Perspective Id
	 */
	public static final String ID = "uk.ac.diamond.scisoft.mappingexplorer.perspective";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui .IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		IFolderLayout projectExplorerFolder = layout.createFolder(PROJECT_EXPLORER, IPageLayout.LEFT, 0.2f,
				layout.getEditorArea());
		projectExplorerFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout additionalViewsFolder = layout.createFolder(FITTER_VIEW_FOLDER, IPageLayout.BOTTOM, 0.55f,
				layout.getEditorArea());
		additionalViewsFolder.addPlaceholder(TwoDMappingView.ID + ":*");
		additionalViewsFolder.addPlaceholder(OneDMappingView.ID + ":*");

		IFolderLayout histViewFolder = layout.createFolder(HISTOGRAM_VIEW_FOLDER, IPageLayout.RIGHT, 0.5f,
				FITTER_VIEW_FOLDER);
		histViewFolder.addView(HistogramMappingView.ID);
		histViewFolder.addPlaceholder(HistogramView.ID + ":*");

		IFolderLayout twoDViewFolder = layout.createFolder(TWO_D_VIEW_FOLDER, IPageLayout.RIGHT, 0.3f,
				layout.getEditorArea());
		twoDViewFolder.addView(TwoDMappingView.ID);

		IFolderLayout oneDViewFolder = layout.createFolder(ONE_D_VIEW_FOLDER, IPageLayout.RIGHT, 0.5f,
				TWO_D_VIEW_FOLDER);
		oneDViewFolder.addView(OneDMappingView.ID);
		//
		layout.addShowViewShortcut(TwoDMappingView.ID);
		layout.addShowViewShortcut(OneDMappingView.ID);
		layout.addShowViewShortcut(HistogramMappingView.ID);
	}
}
