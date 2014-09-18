/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package extendedMetadata;

import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.metadata.IExtendedMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import uk.ac.diamond.sda.meta.page.IMetadataPage;
import uk.ac.diamond.sda.meta.views.MetadataTableView;

public class ExtendedMetaDataComposite implements IMetadataPage {

	private Composite comp;
	private Text scanCommand;
	private Text fullPath;
	private Text lastMod;
	private Text size;
	private Text owner;
	private Text creator;
	private Text fileName;
	private Text creation;
	private MetadataTableView view;
	private static SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy 'at' HH:mm:ss");

	public ExtendedMetaDataComposite() {
		// Need a default constructor for extension point
	}

	@Override
	public void setMetaData(IMetadata metadata) {
		if (metadata instanceof IExtendedMetadata)
			updateComposite((IExtendedMetadata) metadata);

	}

	private void updateComposite(final IExtendedMetadata metadata) {
		UIJob updateCompositeGUI = new UIJob("Update with new metadata") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (metadata.getScanCommand() != null)
					scanCommand.setText(metadata.getScanCommand());
				if (metadata.getFullPath() != null)
					fullPath.setText(metadata.getFullPath());
				if (metadata.getFileName() != null)
					fileName.setText(metadata.getFileName());
				if (metadata.getLastModified() != null)
					lastMod.setText(dateformat.format(metadata.getLastModified()));
				if (metadata.getCreation() != null)
					creation.setText(dateformat.format(metadata.getCreation()));
				if (metadata.getFileSize() != 0)
					size.setText(FileUtils.byteCountToDisplaySize(metadata.getFileSize()));
				if (metadata.getFileOwner() != null)
					owner.setText(metadata.getFileOwner());
				if (metadata.getCreator() != null)
					creator.setText(metadata.getCreator());

				view.setMeta(metadata);
				return Status.OK_STATUS;
			}
		};
		updateCompositeGUI.schedule();
	}

	@Override
	public Composite createComposite(Composite parent) {
		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Group gpExtendedMetadataViewer = new Group(comp, SWT.NONE);
		gpExtendedMetadataViewer.setLayout(new GridLayout(2, false));
		gpExtendedMetadataViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		gpExtendedMetadataViewer.setText("Extended Metadata");

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Scan Command");
		scanCommand = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		scanCommand.setBackground(gpExtendedMetadataViewer.getBackground());
		scanCommand.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Full Path");
		fullPath = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		fullPath.setBackground(gpExtendedMetadataViewer.getBackground());
		fullPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("File name");
		fileName = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		fileName.setBackground(gpExtendedMetadataViewer.getBackground());
		fileName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Last Modified");
		lastMod = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		lastMod.setBackground(gpExtendedMetadataViewer.getBackground());
		lastMod.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Cretion");
		creation = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		creation.setBackground(gpExtendedMetadataViewer.getBackground());
		creation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("File Size");
		size = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		size.setBackground(gpExtendedMetadataViewer.getBackground());
		size.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Owner");
		owner = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		owner.setBackground(gpExtendedMetadataViewer.getBackground());
		owner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(gpExtendedMetadataViewer, SWT.NONE).setText("Creator");
		creator = new Text(gpExtendedMetadataViewer, SWT.READ_ONLY);
		creator.setBackground(gpExtendedMetadataViewer.getBackground());
		creator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Composite tableComp = new Composite(comp, SWT.NONE);

		view = new MetadataTableView();
		view.createPartControl(comp);

		return comp;
	}

}
