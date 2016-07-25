package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import org.eclipse.january.dataset.IDataset;

public interface IDatasetMailman extends Remote {

	public static final String RPC_DATASET_SERVICE_NAME = "DatasetManager";
	public static final String RMI_DATASET_SERVICE_NAME = "RMIDatasetManager";
	
	/**
	 * Add a listener
	 * @param l
	 */
	public void addMailListener(IDataMailListener l) throws RemoteException;
	
	/**
	 * Remove that listener
	 * @param l
	 */
	public void removeMailListener(IDataMailListener l) throws RemoteException;
	
	/**
	 * Removes all listeners, but still listens for mail, the mail must always get through, rain or shine...
	 */
	public void clear() throws RemoteException;

	/**
	 * Send some data from python to java
	 * @param data
	 */
	public void send(String datasetName, Map<String, IDataset> data) throws RemoteException;
	
	/**
	 * Get some data or null if the data does not exist.
	 * @param name
	 * @return
	 */
	public IDataset get(String name) throws RemoteException;
}
