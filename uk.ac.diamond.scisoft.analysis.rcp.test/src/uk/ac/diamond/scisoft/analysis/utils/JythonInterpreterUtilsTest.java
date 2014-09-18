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

package uk.ac.diamond.scisoft.analysis.utils;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyObjectDerived;
import org.python.util.PythonInterpreter;

import uk.ac.diamond.scisoft.python.JythonInterpreterUtils;

public class JythonInterpreterUtilsTest {

	
	/**
	 * This test is used to ensure that a Jython interpreter
	 * importing the scisoft python files can be created.
	 * 
	 * It checks that the .py files created can be instantiated
	 * without errors. The method JythonInterpreterUtils.getInterpreter()
	 * is used within the workflow tool to run jython nodes directly
	 * in the same VM.
	 * 
	 */
	@Test
	public void test() throws Exception {
		
		PythonInterpreter interpreter = JythonInterpreterUtils.getInterpreter();
		if (interpreter == null) throw new Exception("No Jython interpreter found!");

		interpreter.set("fred", 10d);
		interpreter.exec("fred = dnp.Sciwrap(fred)");
        
		final Object fred = interpreter.get("fred");
		if (fred==null) throw new Exception("Cannot read object 'fred'!");
		if (!(fred instanceof PyFloat)) throw new Exception("Fred should be a float!");
		
		final Dataset set = DatasetFactory.createRange(0, 100, 1, Dataset.FLOAT32);
		interpreter.set("x", set);
		interpreter.exec("x = dnp.Sciwrap(x)");
		final Object x = interpreter.get("x");
		if (x==null) throw new Exception("Cannot read object 'x'!");
		if (!(x instanceof PyObjectDerived)) throw new Exception("x should be a PyObjectDerived!");
		
		interpreter.exec("sum = x.sum()");
		final Object sumX = interpreter.get("sum");
		if (sumX==null) throw new Exception("Cannot read object 'sumX'!");
		if (!(sumX instanceof PyFloat)) throw new Exception("sumX should be a float!");
        if (((PyFloat)sumX).getValue()!=4950.0d) throw new Exception("sumX should be 4950.0!");
	}
}
