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
	private String                fullName;

	public DataMailEvent(Object source, String fullName, Map<String, IDataset> data) {
		super(source);
		this.fullName = fullName;
		this.data = data;
	}

	public Map<String, IDataset> getData() {
		if (data==null) return null;
		return new HashMap<String, IDataset>(data);
	}

	public IDataset get(String name) {
		return data.get(name);
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result
				+ ((fullName == null) ? 0 : fullName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataMailEvent other = (DataMailEvent) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		return true;
	}
}
