/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.variables;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.dawnsci.analysis.api.RMIServerProvider;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.AnalysisRpcServerProvider;
import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;

/**
 * This class is interested that the variable names are defined correctly and that they expand.
 */
public class RpcRmiVariablesPluginTest {

	public void testCommon(String expected, String variable) throws CoreException
	{
		String sub = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(variable);
		Assert.assertEquals(expected, sub);
	}
	
	@Test
	public void testRPCPort() throws CoreException {
		int port = AnalysisRpcServerProvider.getInstance().getPort();
		testCommon(Integer.toString(port), "${scisoft_rpc_port}");
	}
	
	@Test
	public void testRMIPort() throws CoreException {
		int port = RMIServerProvider.getInstance().getPort();
		testCommon(Integer.toString(port), "${scisoft_rmi_port}");
	}
	
	@Test
	public void testTempLoc() throws CoreException {
		File tempLocation = FlatteningService.getFlattener().getTempLocation();
		String loc = "";
		if (tempLocation != null)
			loc = tempLocation.toString();
		testCommon(loc, "${scisoft_rpc_temp}");
	}
}
