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
package uk.ac.diamond.scisoft.mappingexplorer.views;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.mappingexplorer.views.twod.IMappingDataControllingView;

/**
 * Abstract {@link PageBookView} that is used by the plotter views in the mapping explorer.
 * 
 * @author rsr31645
 */
public abstract class MappingPageBookView extends PageBookView implements IMappingDataControllingView {

	private static final Logger logger = LoggerFactory.getLogger(MappingPageBookView.class);

	@Override
	protected IPage createDefaultPage(PageBook book) {
		if (getViewSite().getSecondaryId() == null) {
			MappingDefaultViewPage defaultViewPage = new MappingDefaultViewPage();
			initPage(defaultViewPage);
			defaultViewPage.createControl(book);
			return defaultViewPage;
		}
		IWorkbenchPart part = getWorkbenchPart();
		return createPage(part);
	}

	protected abstract IPageBookViewPage createPage(IWorkbenchPart part);

	private IWorkbenchPart getWorkbenchPart() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
	}

	@Override
	public IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			return page.getActiveEditor();
		}
		return null;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		if (getViewSite().getSecondaryId() != null) {
			return new PageRec(part, getDefaultPage());
		}
		IPageBookViewPage pageView = createPage(part);
		if (pageView != null) {
			return new PageRec(part, pageView);
		}
		return null;
	}

	@Override
	public IMappingViewData getMappingViewData() {
		if (getCurrentPage() instanceof IMappingViewDataContainingPage) {
			IMappingViewDataContainingPage currentPage = (IMappingViewDataContainingPage) getCurrentPage();
			return currentPage.getMappingViewData();
		}
		return null;
	}

	@Override
	public String getOriginIdentifier() {
		if (getCurrentPage() instanceof IMappingViewDataContainingPage) {
			IMappingViewDataContainingPage currentPage = (IMappingViewDataContainingPage) getCurrentPage();
			return currentPage.getOriginIdentifer();
		}
		return null;
	}

}
