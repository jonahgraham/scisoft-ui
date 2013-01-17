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
package uk.ac.diamond.scisoft.mappingexplorer.views.twod;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.mappingexplorer.views.BaseViewPageComposite;
import uk.ac.diamond.scisoft.mappingexplorer.views.IMappingViewData;

/**
 * @author rsr31645
 * 
 */
public class BlankPageComposite extends BaseViewPageComposite {

	public BlankPageComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		new Label(this, SWT.None).setText("No data to be plotted");
	}

	@Override
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

	}

	@Override
	public void cleanup() {

	}

	@Override
	public void updatePlot() throws Exception {

	}

	@Override
	public void initialPlot() throws Exception {

	}

	@Override
	public IMappingViewData getMappingViewData() {
		return null;
	}

}
