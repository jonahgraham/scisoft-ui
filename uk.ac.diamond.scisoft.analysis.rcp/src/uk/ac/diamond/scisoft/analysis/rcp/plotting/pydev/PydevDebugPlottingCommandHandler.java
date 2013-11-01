package uk.ac.diamond.scisoft.analysis.rcp.plotting.pydev;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.python.pydev.debug.model.AbstractDebugTarget;
import org.python.pydev.debug.model.remote.RunCustomOperationCommand;
import org.python.pydev.shared_core.structure.Tuple;

abstract public class PydevDebugPlottingCommandHandler extends AbstractHandler {
	private static final String SETUP_CODE = "from scisoftpy.plot import ";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		Tuple<AbstractDebugTarget, String> context = RunCustomOperationCommand.extractContextFromSelection(selection);
		if (context != null) {
			RunCustomOperationCommand cmd = new RunCustomOperationCommand(context.o1, context.o2, SETUP_CODE + getCommand(), getCommand());
			context.o1.postCommand(cmd);
		}

		return null;
	}

	abstract protected String getCommand();
}
