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
	public abstract boolean runProcess();

	/**
	 * Returns the page number
	 */
	public abstract int getPageNumber();
}
