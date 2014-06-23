package uk.ac.diamond.scisoft.feedback.attachment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

/**
 * Editing Support of the table cells (the boolean delete icon)
 */
public class AttachedFileEditingSupport extends EditingSupport {
	private TableViewer tv;
	private int column;

	public AttachedFileEditingSupport(TableViewer viewer, int col) {
		super(viewer);
		tv = viewer;
		this.column = col;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor(null, SWT.CHECK);
	}

	@Override
	protected boolean canEdit(Object element) {
		if (column == 2)
			return true;
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		if (column == 2) {
			File file = (File) element;
			List<File> files = new ArrayList<File>();
			files = (List<File>) tv.getInput();
			files.remove(file);
			tv.setInput(files);
			tv.refresh();
		}
	}
}