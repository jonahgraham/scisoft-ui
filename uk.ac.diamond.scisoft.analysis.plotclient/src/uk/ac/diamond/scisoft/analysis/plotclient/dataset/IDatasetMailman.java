package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public interface IDatasetMailman {

	public static final String RPC_DATASET_SERVICE_NAME = "DatasetManager";
	public static final String RMI_DATASET_SERVICE_NAME = "RMIDatasetManager";
	
	/**
	 * Add a listener
	 * @param l
	 */
	public void addMailListener(IDataMailListener l);
	
	/**
	 * Remove that listener
	 * @param l
	 */
	public void removeMailListener(IDataMailListener l);
	
	/**
	 * Removes all listeners, but still listens for mail, the mail must always get through, rain or shine...
	 */
	public void clear();

	/**
	 * Send some data from python to java
	 * @param data
	 */
	public void send(String datasetName, Map<String, IDataset> data);
	
	/**
	 * Get some data or null if the data does not exist.
	 * @param name
	 * @return
	 */
	public IDataset get(String name);
}
