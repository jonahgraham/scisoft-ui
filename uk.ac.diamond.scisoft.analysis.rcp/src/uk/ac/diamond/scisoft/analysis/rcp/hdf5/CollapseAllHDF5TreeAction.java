package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import uk.ac.diamond.scisoft.analysis.rcp.views.HDF5TreeView;

public class CollapseAllHDF5TreeAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		final HDF5TreeView hdf5TreeView = (HDF5TreeView) EclipseUtils.getActivePage().getActivePart();
		hdf5TreeView.collapseAll();
		return Boolean.TRUE;
	}

}
