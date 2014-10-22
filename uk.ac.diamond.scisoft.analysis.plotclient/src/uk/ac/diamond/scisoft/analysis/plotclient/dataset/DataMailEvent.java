package uk.ac.diamond.scisoft.analysis.plotclient.dataset;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * When something changes, we notify the data sent using
 * this event
 * 
 * @author Matthew Gerring
 *
 */
public class DataMailEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5441046151686162624L;
	
	private Map<String, IDataset> data;

	public DataMailEvent(Object source, Map<String, IDataset> data) {
		super(source);
		this.data = data;
	}

	public Map<String, IDataset> getData() {
		return new HashMap<String, IDataset>(data);
	}

	public IDataset get(String name) {
		return data.get(name);
	}
}
