/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import org.dawb.common.ui.widgets.LabelFieldEditor;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

public class AnalysisRpcAndRmiPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		// setDescription("RMI and Analysis RPC Preferences");
		setPreferenceStore(AnalysisRCPActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(
				"Analysis RPC Temporary File Location (blank for default of system temp directory)",
				getFieldEditorParent()));

		addField(new DirectoryFieldEditor(PreferenceConstants.ANALYSIS_RPC_TEMP_FILE_LOCATION, "Directory:",
				getFieldEditorParent()));

		IntegerFieldEditor analysisPort = new IntegerFieldEditor(PreferenceConstants.ANALYSIS_RPC_SERVER_PORT,
				"Analysis RPC Port: (0 for auto, requires restart)", getFieldEditorParent());
		analysisPort.setValidRange(0, 65535);
		addField(analysisPort);
		
		IntegerFieldEditor rmiPort = new IntegerFieldEditor(PreferenceConstants.RMI_SERVER_PORT,
				"RMI Port: (0 for auto, requires restart)", getFieldEditorParent());
		rmiPort.setValidRange(0, 65535);
		addField(rmiPort);

		addField(new LabelFieldEditor("The currently in use ports are:\n" + "- Analysis RPC: "
				+ AnalysisRpcServerProvider.getInstance().getPort() + "\n" + "- RMI: "
				+ RMIServerProvider.getInstance().getPort() + "\n" + "\n"
				+ "SDA automatically passes the parameters on this page using environment variables \n"
				+ "which are set up in the PyDev Interpreter Info preference pages. \n"
				+ "These are automatically used by scisoftpy. The environment variables are: \n"
				+ "Analysis RPC Port: SCISOFT_RPC_PORT\n" + "RMI Port: SCISOFT_RMI_PORT\n"
				+ "Temporary File Location: SCISOFT_RPC_TEMP\n", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceConstants.ANALYSIS_RPC_RMI_INJECT_VARIABLES,
				"Add above variables automatically to PyDev's Interpreters",
				getFieldEditorParent()));

	}

	@Override
	public boolean performOk() {
		boolean performOk = super.performOk();
		if (performOk) {
			int runningRmiPort = RMIServerProvider.getInstance().getPort();
			int runningRpcPort = AnalysisRpcServerProvider.getInstance().getPort();
			int newRpcPort = getAnalysisRpcPort();
			int newRmiPort = getRmiPort();
			boolean rpcPortChanged = newRpcPort != 0 && runningRpcPort != newRpcPort;
			boolean rmiPortChanged = newRmiPort != 0 && runningRmiPort != newRmiPort;

			if (rpcPortChanged || rmiPortChanged) {
				if (new MessageDialog(
						null,
						"Restart Required",
						null,
						"The ports have changed and will not take effect until the workbench is restarted. Restart now?",
						MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1).open() == Window.OK) {
					PlatformUI.getWorkbench().restart();
				}
			}
		}
		return performOk;
	}

	
	/**
	 * Retrieve current setting for Analysis RPC Port.
	 * 
	 * @return port number, 0 for use default
	 */
	public static int getAnalysisRpcPort() {
		return AnalysisRCPActivator.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.ANALYSIS_RPC_SERVER_PORT);
	}

	/**
	 * Retrieve current setting for Analysis RPC Temporary File Location.
	 * 
	 * @return temp file location, or <code>null</code> for use default
	 */
	public static String getAnalysisRpcTempFileLocation() {
		String loc = AnalysisRCPActivator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.ANALYSIS_RPC_TEMP_FILE_LOCATION);
		if (loc == null || "".equals(loc))
			return null;
		return loc;
	}

	/**
	 * Retrieve current setting for RMI Port.
	 * 
	 * @return port number, 0 for use default
	 */
	public static int getRmiPort() {
		return AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.RMI_SERVER_PORT);
	}

	/**
	 * Retrieve whether SDA should inject into PyDev's IInterpretterInfos the variables
	 * configured within this page.
	 * 
	 * @return true for automatic insertion
	 */
	public static boolean isInjectVariablesAutomaticallyIntoPyDev() {
		return AnalysisRCPActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ANALYSIS_RPC_RMI_INJECT_VARIABLES);
	}
}
