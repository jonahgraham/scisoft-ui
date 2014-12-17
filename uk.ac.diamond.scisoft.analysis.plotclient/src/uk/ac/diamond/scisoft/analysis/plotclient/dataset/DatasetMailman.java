/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.RMIClientProvider;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.plotclient.rpc.AnalysisRpcSyncExecDispatcher;
import uk.ac.diamond.scisoft.analysis.rpc.IAnalysisRpcHandler;


/**
 * Class to send datasets between Java and python and python and Java.
 * 
 * The init method is called from an early startup to connect this
 * class into the RMI(Jython) and RPC(Python) servers.
 * 
 * Then if the user uses jython(RMI) or Python(RPC) they can connect to
 * this mailman and send data back into the Java GUI. 
 * 
 * @author Matthew Gerring
 *
 */
public class DatasetMailman implements IDatasetMailman, Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6654851052590631220L;

	static private Logger logger = LoggerFactory.getLogger(DatasetMailman.class);

	private static DatasetMailman manager;

	/**
	 * Use this method to obtain a handle to the manager singleton.
	 * <p>
	 * 
	 * @return PlotWindowManager
	 */
	synchronized static void init() {
		if (manager == null) {
			manager = new DatasetMailman();

			// register as an RMI service for Jython
			try {
				RMIServerProvider.getInstance().exportAndRegisterObject(RMI_DATASET_SERVICE_NAME, manager);
			} catch (Exception e) {
				logger.warn("Unable to register PlotWindowManager for use over RMI - it might be disabled", e);
			}

			// register as an RPC service for Python
			try {
				IAnalysisRpcHandler dispatcher = new AnalysisRpcSyncExecDispatcher(IDatasetMailman.class, manager);
				AnalysisRpcServerProvider.getInstance().addHandler(RPC_DATASET_SERVICE_NAME, dispatcher);
			} catch (Exception e) {
				logger.warn("Not registered IDatasetManager as RPC service - but might be disabled");
			}
		}
	}
	
	/**
	 * This is what you call in the Java code to get the mailman and
	 * add a listener to it which is notified of datasets being sent
	 * from Python.
	 * 
	 * @return
	 */
	public static DatasetMailman getLocalManager() {
		if (manager==null) init();
		return manager;
	}

	/**
	 * This is used from Jython do not call it from the Java code.
	 * @return
	 */
	public static IDatasetMailman getRemoteManager() {
		try {
			return (IDatasetMailman) RMIClientProvider.getInstance().lookup(null, RMI_DATASET_SERVICE_NAME);
		} catch (Throwable e) {
			e.printStackTrace();
			// It will not work in Jython if this exception is thrown. However people are not using this in
			// Jython at the moment...?
			//logger.error("Unable to obtain IPlotWindowManagerRMI manager", e);
			return manager;
		}
	}


	private DatasetMailman() {
		
	}

	private Set<IDataMailListener> listeners;

	@Override
	public void send(String datasetName, Map<String, IDataset> data) {
		
		if (listeners==null) return;
		
		final DataMailEvent evt = new DataMailEvent(this, datasetName, data);
		for (Object listener : listeners) {
			((IDataMailListener)listener).mailReceived(evt);
		}
	}

	@Override
	public IDataset get(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMailListener(IDataMailListener l) {
		if (listeners==null) listeners = new HashSet<IDataMailListener>();
		listeners.add(l);
	}

	@Override
	public void removeMailListener(IDataMailListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}

	@Override
	public void clear() {
		listeners.clear();
	}
}
