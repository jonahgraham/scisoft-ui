package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import java.util.EventListener;

public interface IDataMailListener extends EventListener {

	/**
	 * Called when python client sends us some data.
	 * @param evt
	 */
	public void mailReceived(DataMailEvent evt);
}
