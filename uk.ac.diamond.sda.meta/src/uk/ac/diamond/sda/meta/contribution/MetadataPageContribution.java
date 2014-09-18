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

package uk.ac.diamond.sda.meta.contribution;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.metadata.IMetadata;
import uk.ac.diamond.sda.meta.discriminator.IMetadataDiscriminator;
import uk.ac.diamond.sda.meta.page.IMetadataPage;

import java.net.URL;

public class MetadataPageContribution {

	private static final Logger logger = LoggerFactory.getLogger(MetadataPageContribution.class);

	public static final String CLASS_NAME = "class";
	public static final String NAME = "name";
	public static final String SUPPORTED_METADATA = "supportedMetadata";
	public static final String ICON = "icon";

	private String extentionPointname;
	private ImageDescriptor icon;
	final private IConfigurationElement configurationElement;
	private IMetadataDiscriminator discriminator;

	public MetadataPageContribution(IConfigurationElement iConfigurationElement) {
		this.configurationElement = iConfigurationElement;
		// get the icon
		IContributor contrib = configurationElement.getContributor();
		String contribName = contrib instanceof RegistryContributor ? ((RegistryContributor) contrib).getActualName()
				: contrib.getName();
		URL imgURL = FileLocator.find(Platform.getBundle(contribName),
				new Path(configurationElement.getAttribute(ICON)), null);
		icon = ImageDescriptor.createFromURL(imgURL);

		extentionPointname = iConfigurationElement.getAttribute(NAME);
		try {
			discriminator = (IMetadataDiscriminator) iConfigurationElement
					.createExecutableExtension(SUPPORTED_METADATA);
		} catch (CoreException e) {
			logger.warn("Could not load the discriminator for different metadata types");
		}
	}

	public IMetadataPage getPage() throws CoreException {
		return (IMetadataPage) configurationElement.createExecutableExtension(CLASS_NAME);
	}

	public String getExtentionPointname() {
		return extentionPointname;
	}

	public ImageDescriptor getIcon() {
		return icon;
	}

	public boolean isApplicableFor(IMetadata meta) {
		if (discriminator == null)
			return false;
		return discriminator.isApplicableFor(meta);
	}

}
