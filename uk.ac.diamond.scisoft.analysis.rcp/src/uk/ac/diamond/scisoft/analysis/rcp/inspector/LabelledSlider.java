/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

/**
 * Class for a labelled Slider. The labels are positioned below the slider at its extremities
 */
public class LabelledSlider extends Composite {
	private Slider slider = null;
	private Label leftLabel = null;
	private Label rightLabel = null;

	/**
	 * @param parent
	 * @param style
	 */
	public LabelledSlider(Composite parent, int style) {
		super(parent, SWT.NONE);

		setLayout(new GridLayout(3, false));
		GridData gd;

		slider = new Slider(this, style);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		slider.setLayoutData(gd);
		slider.setToolTipText("Adjust starting value");

		leftLabel = new Label(this, SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 80;
		leftLabel.setLayoutData(gd);
		leftLabel.setToolTipText("Start value");

		Label l = new Label(this, SWT.RIGHT);
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		rightLabel = new Label(this, SWT.RIGHT);
		gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = 80;
		rightLabel.setLayoutData(gd);
		rightLabel.setToolTipText("End value");
	}

	/**
	 * @param label
	 */
	public void setLeftLabel(String label) {
		leftLabel.setText(label);
	}

	/**
	 * @param label
	 */
	public void setRightLabel(String label) {
		rightLabel.setText(label);
	}

	/**
	 * @param sListener
	 */
	public void addSelectionListener(SelectionListener sListener) {
		slider.addSelectionListener(sListener);
	}

	/**
	 * @param minimum
	 * @param maximum
	 * @param minText
	 * @param maxText
	 */
	public void setMinMax(int minimum, int maximum, String minText, String maxText) {
		slider.setMinimum(minimum);
		slider.setMaximum(maximum);
		leftLabel.setText(minText);
		rightLabel.setText(maxText);
		this.pack();
	}

	/**
	 * @param i
	 */
	public void setThumb(int i) {
		slider.setThumb(i);
	}

	/**
	 * @param inc
	 * @param pageInc
	 */
	public void setIncrements(int inc, int pageInc) {
		slider.setIncrement(inc);
		slider.setPageIncrement(pageInc);
	}

	@Override
	public void setEnabled(boolean enabled) {
		slider.setEnabled(enabled);
	}

	/**
	 * @param s
	 * @return true if s is the same as slider 
	 */
	public boolean equals(Slider s) {
		return slider.equals(s);
	}

	/**
	 * Set slider to given value
	 * @param value
	 */
	public void setValue(int value) {
		if (value < 0 || value >= slider.getMaximum())
			value = 0;
		slider.setSelection(value);
	}

	/**
	 * @return slider value
	 */
	public int getValue() {
		return slider.getSelection();
	}
}
