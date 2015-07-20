package uk.ac.diamond.scisoft.analysis.plotclient.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.VanillaPlottingSystemView;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.plotclient.ScriptingConnection;

/**
 * A view with a plotting system on it and connected to python.
 * 
 * You may use this class to replace plot view because it has fewer dependencies
 * as the whole of analysis.rcp is not required.
 * 
 * @author fcp94556
 *
 */
public class ScriptablePlottingSystemView extends VanillaPlottingSystemView implements IAdaptable {
	
	private ScriptingConnection connection;

	@Override
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent);
		try {
		    
		    connection = new ScriptingConnection(getPartName());
		    connection.setPlottingSystem(system);
		    
		} catch (Exception ne) {
			throw new RuntimeException(ne); // Lazy
		}	
	}

	@Override
	public void dispose() {
		connection.dispose();
		super.dispose();
	}
	
}
