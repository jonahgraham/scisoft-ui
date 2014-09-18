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
