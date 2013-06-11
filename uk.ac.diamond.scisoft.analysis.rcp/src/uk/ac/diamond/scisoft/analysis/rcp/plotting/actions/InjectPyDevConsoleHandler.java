/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.debug.newconsole.PydevConsole;
import org.python.pydev.debug.newconsole.PydevConsoleConstants;
import org.python.pydev.debug.newconsole.PydevConsoleFactory;
import org.python.pydev.debug.newconsole.PydevConsoleInterpreter;
import org.python.pydev.debug.newconsole.env.IProcessFactory;
import org.python.pydev.debug.newconsole.env.IProcessFactory.PydevConsoleLaunchInfo;
import org.python.pydev.debug.newconsole.env.UserCanceledException;
import org.python.pydev.debug.newconsole.prefs.InteractiveConsolePrefs;
import org.python.pydev.dltk.console.ui.ScriptConsole;
import org.python.pydev.dltk.console.ui.internal.ScriptConsoleViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectPyDevConsoleHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(InjectPyDevConsoleHandler.class);

	/**
	 * Command ID (as defined in plugin.xml)
	 */
	public static String COMMAND_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.injectPyDevConsole";
	/**
	 * The parameter key for the ExecutionEvent that specifies whether to create a new console or reuse an existing one
	 * if possible, value should be "true" or "false". Optional, default false.
	 */
	public static String CREATE_NEW_CONSOLE_PARAM = COMMAND_ID + ".createNewConsoleAlways";
	/**
	 * The parameter key for the ExecutionEvent that specifies the name of the view to link. Optional. If unspecified
	 * default plot is left.
	 */
	public static String VIEW_NAME_PARAM = COMMAND_ID + ".viewName";
	/**
	 * The parameter key for the ExecutionEvent that specifies "always", "never" or "newonly" as to whether to setup
	 * scisoftpy. Optional, default is "newonly".
	 * <p>
	 * e.g.
	 * 
	 * <pre>
	 * import scisoftpy as dnp
	 * </pre>
	 */
	public static String SETUP_SCISOFTPY_PARAM = COMMAND_ID + ".addScisoftPySetup";
	/**
	 * Commands to be injected.
	 */
	public static String INJECT_COMMANDS_PARAM = COMMAND_ID + ".commandToInject";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ScriptConsole console = null;
			if (!Boolean.parseBoolean(event.getParameter(CREATE_NEW_CONSOLE_PARAM))) {
				console = getActiveScriptConsole(PydevConsoleConstants.CONSOLE_TYPE);
			}

			if (console == null) {
				PydevConsoleFactory pcf = new PydevConsoleFactory();
				PydevConsoleInterpreter pci = getConsole();
				if(pci == null) return null;
				pcf.createConsole(pci, createPythonCommands(true, event));
			} else {
				sendCommands(event, console);
			}
		} catch (UserCanceledException e) {
			logger.error("Operation canceled", e);
			throw new ExecutionException("Operation canceled", e);
		} catch (Exception e) {
			logger.error("Cannot open console", e);
			throw new ExecutionException("Cannot open console", e);
		}

		return null;
	}

	void sendCommands(ExecutionEvent event, ScriptConsole console) throws BadLocationException {
		PydevConsole pydevConsole = (PydevConsole) console;
		IDocument document = pydevConsole.getDocument();

		String cmd = createPythonCommands(false, event);
		if (cmd != null) {
			document.replace(document.getLength(), 0, cmd);
		}

		if (InteractiveConsolePrefs.getFocusConsoleOnSendCommand()) {
			ScriptConsoleViewer viewer = pydevConsole.getViewer();
			if (viewer != null) {

				StyledText textWidget = viewer.getTextWidget();
				if (textWidget != null) {
					textWidget.setFocus();
				}
			}
		}
	}

	String createPythonCommands(boolean newConsole, ExecutionEvent event) {
		StringBuffer cmds = new StringBuffer();
		SetupScisoftpy setup = SetupScisoftpy.valueOfIgnoreCase(event.getParameter(SETUP_SCISOFTPY_PARAM));
		if (setup.setupScisoftpy(newConsole)) {
			cmds.append("# Importing scisoftpy.\n");
			cmds.append("import scisoftpy as dnp\n");
		}
		if (event.getParameter(VIEW_NAME_PARAM) != null && !"".equals(event.getParameter(VIEW_NAME_PARAM))) {
			String viewName = event.getParameter(VIEW_NAME_PARAM);
			cmds.append("# Connecting to plot '" + viewName + "'.\n");
			cmds.append("dnp.plot.setdefname('" + viewName + "')\n");
		}
		if (event.getParameter(INJECT_COMMANDS_PARAM) != null && !"".equals(event.getParameter(INJECT_COMMANDS_PARAM))) {
			cmds.append(event.getParameter(INJECT_COMMANDS_PARAM));
		}
		return cmds.toString();
	}

	private PydevConsoleInterpreter getConsole() throws Exception {

		IProcessFactory iprocessFactory = new IProcessFactory();

		// Shows GUI - NOTE Change here to always link into Jython without showing dialog.
		PydevConsoleLaunchInfo createInteractiveLaunch = null;
		try {
			createInteractiveLaunch = iprocessFactory.createInteractiveLaunch();
		} catch (UserCanceledException e) {
			throw new Exception("Interpreter creation canceled", e);
		}

		if (createInteractiveLaunch == null) {
			return null;
		}

		List<IPythonNature> naturesUsed = iprocessFactory.getNaturesUsed();
		return PydevConsoleFactory.createPydevInterpreter(createInteractiveLaunch, naturesUsed);
	}

	/**
	 * Code borrowed from EvaluateActionSetter in com.python.pydev
	 * 
	 * @param consoleType
	 *            the console type we're searching for
	 * @return the currently active console.
	 */
	private ScriptConsole getActiveScriptConsole(String consoleType) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {

				List<IViewPart> consoleParts = getConsoleParts(page, false);
				if (consoleParts.size() == 0) {
					consoleParts = getConsoleParts(page, true);
				}

				if (consoleParts.size() > 0) {
					IConsoleView view = null;
					long lastChangeMillis = Long.MIN_VALUE;

					if (consoleParts.size() == 1) {
						view = (IConsoleView) consoleParts.get(0);
					} else {
						// more than 1 view available
						for (int i = 0; i < consoleParts.size(); i++) {
							IConsoleView temp = (IConsoleView) consoleParts.get(i);
							IConsole console = temp.getConsole();
							if (console instanceof PydevConsole) {
								PydevConsole tempConsole = (PydevConsole) console;
								ScriptConsoleViewer viewer = tempConsole.getViewer();

								long tempLastChangeMillis = viewer.getLastChangeMillis();
								if (tempLastChangeMillis > lastChangeMillis) {
									lastChangeMillis = tempLastChangeMillis;
									view = temp;
								}
							}
						}
					}

					if (view != null) {
						IConsole console = view.getConsole();

						if (console instanceof ScriptConsole && console.getType().equals(consoleType)) {
							return (ScriptConsole) console;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Code borrowed from EvaluateActionSetter in com.python.pydev
	 * 
	 * @param page
	 *            the page where the console view is
	 * @param restore
	 *            whether we should try to restore it
	 * @return a list with the parts containing the console
	 */
	private List<IViewPart> getConsoleParts(IWorkbenchPage page, boolean restore) {
		List<IViewPart> consoleParts = new ArrayList<IViewPart>();

		IViewReference[] viewReferences = page.getViewReferences();
		for (IViewReference ref : viewReferences) {
			if (ref.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
				IViewPart part = ref.getView(restore);
				if (part != null) {
					consoleParts.add(part);
					if (restore) {
						return consoleParts;
					}
				}
			}
		}
		return consoleParts;
	}

	public static enum SetupScisoftpy {
		ALWAYS("Yes") {
			@Override
			public boolean setupScisoftpy(boolean isNewConsole) {
				return true;
			}
		},
		NEVER("No") {
			@Override
			public boolean setupScisoftpy(boolean isNewConsole) {
				return false;
			}
		},
		NEWONLY("Only if a new console is created") {
			@Override
			public boolean setupScisoftpy(boolean isNewConsole) {
				return isNewConsole;
			}
		};

		private final String display;

		SetupScisoftpy(String display) {
			this.display = display;
		}

		public String getDisplay() {
			return display;
		}

		public static SetupScisoftpy valueOfIgnoreCase(String value) {
			for (SetupScisoftpy s : SetupScisoftpy.values()) {
				if (s.toString().equalsIgnoreCase(value)) {
					return s;
				}
			}
			// return default
			return NEWONLY;
		}

		public abstract boolean setupScisoftpy(boolean isNewConsole);

	}

	public static class SetupSciSoftPyParameterValues implements IParameterValues {

		@Override
		public Map<String, String> getParameterValues() {
			Map<String, String> values = new HashMap<String, String>();
			for (SetupScisoftpy s : SetupScisoftpy.values()) {
				values.put(s.getDisplay(), s.toString());
			}
			return values;
		}

	}

}
