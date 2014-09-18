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
