package uk.ac.diamond.scisoft.feedback.attachment;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.feedback.utils.FeedbackConstants;
import uk.ac.diamond.scisoft.feedback.utils.FeedbackUtils;

/**
 * Label Provider of the Table Viewer
 */
public class AttachedFileLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex != 2)
			return null;
		if (element == null)
			return null;
		return FeedbackConstants.DELETE;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element == null)
			return null;
		File file = (File) element;
		if (columnIndex == 0) {
			return file.getName().substring((file.getName().lastIndexOf(File.separator)+1));
		} else if (columnIndex == 1) {
			return FeedbackUtils.getValueWithUnit(file.length());
		} else if (columnIndex == 2) {
			return null;
		}
		return null;
	}
}