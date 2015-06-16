package uk.ac.diamond.scisoft.mrc.ui.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.mrc.ui.Activator;

public class EMPreferenceInitializer extends AbstractPreferenceInitializer {

	public EMPreferenceInitializer() {
	
	}

	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(EMConstants.LCMD, "module load dawn/snapshot ; $DAWN_RELEASE_DIRECTORY/dawn");
		store.setDefault(EMConstants.WCMD, "C:/Users/fcp94556/Desktop/DawnMaster/dawn.exe");
		store.setDefault(EMConstants.SEP_PROCESS, true);
		
		store.setDefault(EMConstants.FOLDER_QUEUE, "scisoft.diamond.FOLDER_QUEUE");
		store.setDefault(EMConstants.FOLDER_TOPIC, "scisoft.diamond.FOLDER_TOPIC");
		store.setDefault(EMConstants.EM_QUEUE,     "scisoft.em.STATUS_QUEUE");
		store.setDefault(EMConstants.EM_TOPIC,     "scisoft.em.STATUS_TOPIC");
	}

}
