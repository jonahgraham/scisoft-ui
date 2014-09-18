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

import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.metadata.IMetadata;

public interface IMetadataPage {

	/**
	 * This is a setter that will allow the page to process the metadata.
	 * 
	 * @param metadata
	 */
	public void setMetaData(IMetadata metadata);

	/**
	 * Each IMetadata Page should be capable of returning a composite containing the GUI elements
	 * 
	 * @param parent
	 * @return a composite containing a gui
	 */
	public Composite createComposite(Composite parent);

}
