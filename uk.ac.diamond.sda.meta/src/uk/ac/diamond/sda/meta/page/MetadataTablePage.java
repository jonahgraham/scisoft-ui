/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.sda.meta.page;

import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.sda.meta.views.MetadataTableView;

public class MetadataTablePage implements IMetadataPage {

	private Composite control;
	MetadataTableView view = null;

	public MetadataTablePage() {
	}

	@Override
	public Composite createComposite(Composite parent) {

		this.control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, false));
		view = new MetadataTableView();
		view.createPartControl(control);
		return control;
	}

	@Override
	public void setMetaData(IMetadata metadata) {
		view.setMeta(metadata);
	}

}
