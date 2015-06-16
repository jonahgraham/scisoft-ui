package uk.ac.diamond.scisoft.mrc.ui.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.scisoft.mrc.ui.Activator;

public class EMPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EMPreferencePage() {
		setDescription("Advanced properties to configure pipeline launching and queue names.");
	}


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);

		StringFieldEditor lcmd = new StringFieldEditor(EMConstants.LCMD, "Linux", getFieldEditorParent());
		addField(lcmd);
		
		StringFieldEditor wcmd = new StringFieldEditor(EMConstants.WCMD, "Windows", getFieldEditorParent());
		addField(wcmd);

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		
		StringFieldEditor fq = new StringFieldEditor(EMConstants.FOLDER_QUEUE, "Folder Queue", getFieldEditorParent());
		addField(fq);
		
		StringFieldEditor ft = new StringFieldEditor(EMConstants.FOLDER_TOPIC, "Folder Topic", getFieldEditorParent());
		addField(ft);
		
		StringFieldEditor emq = new StringFieldEditor(EMConstants.EM_QUEUE, "EM Queue", getFieldEditorParent());
		addField(emq);
		
		StringFieldEditor emt = new StringFieldEditor(EMConstants.EM_TOPIC, "EM Topic", getFieldEditorParent());
		addField(emt);

	}

}
