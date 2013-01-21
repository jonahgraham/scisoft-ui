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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

/**
 * @author rsr31645
 * 
 */
public class MappingDefaultViewPage implements IPageBookViewPage {

	private static final String VIEW_UNAVAILABLE = "An view of this kind is not available.";

	@Override
	public void setFocus() {

	}

	@Override
	public void setActionBars(IActionBars actionBars) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void createControl(Composite parent) {
		rootControl = new Composite(parent, SWT.None);
		rootControl.setLayout(new GridLayout());

		Label lbl = new Label(rootControl, SWT.None);
		lbl.setText(VIEW_UNAVAILABLE);
		lbl.setLayoutData(new GridData());
	}

	@Override
	public void init(IPageSite site) throws PartInitException {

	}

	@Override
	public IPageSite getSite() {
		return null;
	}

	private Composite rootControl;

	@Override
	public Control getControl() {
		return rootControl;
	}

}
