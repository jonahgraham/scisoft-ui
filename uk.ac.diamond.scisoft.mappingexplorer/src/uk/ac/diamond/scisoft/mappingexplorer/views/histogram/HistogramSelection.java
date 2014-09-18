/**
 * 
 */
package uk.ac.diamond.scisoft.mappingexplorer.views.histogram;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.jface.viewers.ISelection;

/**
 * @author rsr31645
 * 
 */
public class HistogramSelection implements ISelection {

	private final IDataset ds;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	public HistogramSelection(IDataset ds) {
		this.ds = ds;
	}

	public IDataset getDataset() {
		return ds;
	}

}
