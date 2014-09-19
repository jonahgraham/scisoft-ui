/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.pydev;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.python.pydev.debug.model.AbstractDebugTarget;
import org.python.pydev.debug.model.IVariableLocator;
import org.python.pydev.debug.model.remote.RunCustomOperationCommand;
import org.python.pydev.shared_core.structure.Tuple;

public class PydevDebugSaveHDF5CommandHandler extends AbstractHandler {
	private static final String SETUP_CODE =
					"import h5py;\n" +
					"def save_hdf5(x):\n" +
					"    filename = '''%s'''\n" +
					"    print 'Saving to ' + filename\n" +
					"    output = h5py.File(filename, 'w')\n" +
					"    output.create_dataset('data', x.shape, x.dtype, x)\n" +
					"    output.close()\n";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		Tuple<AbstractDebugTarget, IVariableLocator> context = RunCustomOperationCommand.extractContextFromSelection(selection);
		if (context != null) {
			Shell activeShell = Display.getDefault().getActiveShell();
			FileDialog dialog = new FileDialog(activeShell, SWT.SAVE);
			String filename = dialog.open();
			if (filename != null) {
				String setup_code = String.format(SETUP_CODE, filename);
				RunCustomOperationCommand cmd = new RunCustomOperationCommand(context.o1, context.o2, setup_code,
						"save_hdf5");
				context.o1.postCommand(cmd);
			}
		}

		return null;
	}
}
