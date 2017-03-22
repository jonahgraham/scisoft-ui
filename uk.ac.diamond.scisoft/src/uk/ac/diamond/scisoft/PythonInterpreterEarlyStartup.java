/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.osgi.FunctionFactoryStartup;
import uk.ac.diamond.scisoft.analysis.osgi.LoaderFactoryStartup;

public class PythonInterpreterEarlyStartup implements IStartup {

	public static final String DAWN_SKIP_UPDATE_PYTHON_PATH = "DAWN_SKIP_UPDATE_PYTHON_PATH";
	private static Logger logger = LoggerFactory.getLogger(PythonInterpreterEarlyStartup.class);

	@Override
	public void earlyStartup() {

		// initialiseInterpreter only when
		// loader factory and function factory plugins
		// are known.
		final Runnable runner = new Runnable() {
			@Override
			public void run() {

				try {
					Thread.sleep(500); // 1/2 second
				} catch (InterruptedException e) {
					logger.error("Cannot wait on worker thread", e);
				}

				while (!LoaderFactoryStartup.isStarted() || !FunctionFactoryStartup.isStarted()) {

					try {
						Thread.sleep(500); // 1/2 second
					} catch (InterruptedException e) {
						logger.error("Cannot sleep on worker thread", e);
					}
				}
				try {
					new PyDevConfiguration().initialiseConsole();
				} catch (Exception e) {
					logger.error("Cannot initialize the PyDev Console settings.", e);
				}
				try {
					new JythonCreator().initialiseInterpreter(new NullProgressMonitor());
				} catch (Exception e) {
					logger.error("Cannot initialize the PyDev Jython interpreter settings.", e);
				}
				try {
					if (Boolean.getBoolean(DAWN_SKIP_UPDATE_PYTHON_PATH)) {
						logger.info(
								"Skipping update of Python Path because " + DAWN_SKIP_UPDATE_PYTHON_PATH + " is true");
					} else {
						logger.info("Running update of Python Path because " + DAWN_SKIP_UPDATE_PYTHON_PATH
								+ " is unset or not true");
						new PythonUpdater().updatePythonPath(new NullProgressMonitor());
					}
				} catch (Exception e) {
					logger.error("Cannot update the PyDev Python interpreter settings.", e);
				}
			}
		};

		final Thread daemon = new Thread(runner);
		daemon.setPriority(Thread.MIN_PRIORITY);
		daemon.setDaemon(true);
		daemon.start();
	}

}
