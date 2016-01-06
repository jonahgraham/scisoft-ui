package uk.ac.diamond.scisoft.mrc.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An editor part for providing a human 
 * @author Matthew Gerring
 *
 */
public class EMPEditor extends MultiPageEditorPart {
	
	private static Logger logger = LoggerFactory.getLogger(EMPEditor.class);

	@Override
	protected void createPages() {
		
		final TextEditor source = new TextEditor();
		try {
			addPage(0, source,       getEditorInput());
			setPageText(0, getEditorInput().getName());
		} catch (PartInitException e) {
			logger.error("Creating editor!", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


}
