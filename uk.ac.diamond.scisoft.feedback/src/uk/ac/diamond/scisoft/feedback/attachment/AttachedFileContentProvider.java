package uk.ac.diamond.scisoft.feedback.attachment;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content Provider of the Table Viewer
 */
public class AttachedFileContentProvider implements IStructuredContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null) {
			return null;
		}
		return ((List<?>) inputElement).toArray();
	}
}