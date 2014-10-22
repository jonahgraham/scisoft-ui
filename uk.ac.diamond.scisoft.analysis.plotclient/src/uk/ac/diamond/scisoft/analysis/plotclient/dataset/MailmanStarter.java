package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import org.eclipse.ui.IStartup;

public class MailmanStarter implements IStartup {

	/**
	 * We start the mailman which delivers datasets between python and java
	 */
	@Override
	public void earlyStartup() {
		DatasetMailman.init();
	}

}
