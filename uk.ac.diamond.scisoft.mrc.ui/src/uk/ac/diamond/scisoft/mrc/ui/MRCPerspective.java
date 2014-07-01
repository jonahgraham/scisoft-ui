package uk.ac.diamond.scisoft.mrc.ui;

import org.dawnsci.commandserver.ui.view.ConsumerView;
import org.dawnsci.commandserver.ui.view.StatusQueueView;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MRCPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);

		final String queueViewId = StatusQueueView.createId("org.dawnsci.commandserver.foldermonitor", "org.dawnsci.commandserver.foldermonitor.FolderEventBean", "scisoft.mrc.STATUS_QUEUE", "scisoft.mrc.STATUS_TOPIC", "scisoft.mrc.FOLDER_QUEUE");
		layout.addView(queueViewId, IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(ConsumerView.ID, IPageLayout.RIGHT, 0.5f, queueViewId);
	}


}
