/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.arpes.calibration.wizards;

import org.eclipse.jface.wizard.WizardPage;

public abstract class CalibrationWizardPage extends WizardPage {

	protected CalibrationWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * This Method is called when a page is changed
	 * @return true if process was successful
	 */
	public abstract boolean runProcess() throws InterruptedException;

	/**
	 * Returns the page number
	 */
	public abstract int getPageNumber();
}
