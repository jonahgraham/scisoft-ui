/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;

public class AxisValuesTest {

	@Test
	public void testAxisValues() {
		AxisValues axis = new AxisValues();
		assertEquals(Float.MAX_VALUE, axis.getMaxValue(),0.1);
		assertEquals(Float.MAX_VALUE, axis.getMaxValue(),0.1);
		assertEquals(-Float.MAX_VALUE, axis.getMinValue(),0.1);
		assertEquals(-Float.MAX_VALUE, axis.getMinValue(),0.1);
	}

	@Test
	public void testAxisValuesListOfDouble() {
		ArrayList<Double> arrayList = new ArrayList<Double>();
		for(int i = 0; i < 10; i++) {
			arrayList.add(i*10.0);
		}
		AxisValues axis = new AxisValues(arrayList);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
	}

	@Test
	public void testAxisValuesDoubleArray() {
		double[] doubleArray = new double[10];
		for(int i = 0; i < 10; i++) {
			doubleArray[i] = (i*10.0);
		}
		AxisValues axis = new AxisValues(doubleArray);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
	}

	@Test
	public void testAxisValuesDataset() {
		DoubleDataset dataset = DoubleDataset.createRange(0.0, 100.0, 10.0);
		AxisValues axis = new AxisValues(dataset);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
	}
	
	private AxisValues setupClass(double min, double step, int steps) {
		double[] axis = new double[steps];
		for(int i = 0; i < steps; i++) {
			axis[i] = min+(step*i);
		}
		
		return new AxisValues(axis);
	}
	
	
	@Test
	public void testGetValues() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-5, axis.getValue(0),0.1);
		assertEquals(-4, axis.getValue(1),0.1);
		assertEquals(-3, axis.getValue(2),0.1);
		assertEquals(-2, axis.getValue(3),0.1);
		assertEquals(-1, axis.getValue(4),0.1);
		assertEquals(0, axis.getValue(5),0.1);
		assertEquals(1, axis.getValue(6),0.1);
		assertEquals(2, axis.getValue(7),0.1);
		assertEquals(3, axis.getValue(8),0.1);
		assertEquals(4, axis.getValue(9),0.1);

	}

	@Test
	public void testToDataset() {
		AxisValues axis = setupClass(-5, 1, 10);
		DoubleDataset dataset = DoubleDataset.createRange(-5, 5, 1);

		try {
			axis.toDataset().checkCompatibility(dataset);
		} catch (Exception e) {
			fail("datasets are not compatible, "+e.toString());
		}
		
		assertEquals(axis.getValue(0),dataset.get(0),0.1);
		assertEquals(axis.getValue(5),dataset.get(5),0.1);
		assertEquals(axis.getValue(9),dataset.get(9),0.1);
		
	}

	@Test
	public void testAddValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.addValue(100.0);
		assertEquals(axis.getValues().size(), 11);
		assertEquals(100.0,axis.getMaxValue(),0.1);
		axis.addValue(-100);
		assertEquals(axis.getValues().size(), 12);
		assertEquals(-100.0,axis.getMinValue(),0.1);
	}

	@Test
	public void testSetValuesListOfDouble() {
		AxisValues axis = setupClass(-5, 1, 10);
		
		ArrayList<Double> arrayList = new ArrayList<Double>();
		for(int i = 0; i < 10; i++) {
			arrayList.add(i*10.0);
		}
		axis.setValues(arrayList);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);

	}

	@Test
	public void testSetValuesDoubleArray() {
		AxisValues axis = setupClass(-5, 1, 10);
		
		double[] doubleArray = new double[10];
		for(int i = 0; i < 10; i++) {
			doubleArray[i] = (i*10.0);
		}
		axis.setValues(doubleArray);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
	}

	@Test
	public void testSetValuesIDataset() {
		AxisValues axis = setupClass(-5, 1, 10);
		
		DoubleDataset dataset = DoubleDataset.createRange(0.0, 100.0, 10.0);
		axis.setValues(dataset);
		assertEquals(90.0, axis.getMaxValue(),0.1);
		assertEquals(0.0, axis.getMinValue(),0.1);
	}

	@Test
	public void testSetLowEntry() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.setLowEntry(-100);
		assertEquals(-100, axis.getMinValue(),0.1);
	}

	@Test
	public void testSetTopEntry() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.setTopEntry(100);
		assertEquals(100, axis.getMaxValue(),0.1);
	}

	@Test
	public void testGetMax() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(4, axis.getMaxValue(),0.1);
	}

	@Test
	public void testGetMin() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-5, axis.getMinValue(),0.1);
	}

	@Test
	public void testGetValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-5.0, axis.getValue(0),0.1);
		assertEquals(0.0, axis.getValue(5),0.1);
		assertEquals(4.0, axis.getValue(9),0.1);
	}

	@Test
	public void testIterator() {
		AxisValues axis = setupClass(-5, 1, 10);
		Iterator<Double> itt = axis.iterator();
		double count = -5.0;
		while(itt.hasNext()) {
			assertEquals(count, itt.next(),0.1);
			count += 1.0;
		}

		itt = axis.iterator();
		count = -5.0;
		do {
			assertEquals(count, itt.next(),0.1);
			count += 1.0;
		} while(itt.hasNext());
	}

	@Test
	public void testDistBetween() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(1, axis.distBetween(0, 1),0.1);
		assertEquals(2, axis.distBetween(1, 3),0.1);
		assertEquals(4, axis.distBetween(3, 7),0.1);
		assertEquals(9, axis.distBetween(0, 9),0.1);
	}

	@Test
	public void testClear() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.clear();
		assertEquals(0,axis.size());
		assertEquals(Float.MAX_VALUE, axis.getMaxValue(),0.1);
		assertEquals(-Float.MAX_VALUE, axis.getMinValue(),0.1);
		
		// Test to avoid bug
		axis = setupClass(-5, 1, 10);
		axis.clear();
		try {
			axis.addValue(2.0);		
		} catch (Exception e) {
			fail("Problem occured with adding new value : "+e);
		}
	}

	@Test
	public void testNearestUpEntry() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-1, axis.nearestUpEntry(-10.0));
		assertEquals(-1, axis.nearestUpEntry(10.0));
		assertEquals(1, axis.nearestUpEntry(-4.5));
		assertEquals(2, axis.nearestUpEntry(-3.2));
		assertEquals(4, axis.nearestUpEntry(-1.2));
		assertEquals(5, axis.nearestUpEntry(0));
		assertEquals(6, axis.nearestUpEntry(0.1));
		assertEquals(8, axis.nearestUpEntry(2.5));

		axis = setupClass(4, -1, 10);
		assertEquals(-1, axis.nearestUpEntry(-10.0));
		assertEquals(-1, axis.nearestUpEntry(10.0));
		assertEquals(9, axis.nearestUpEntry(-4.5));
		assertEquals(8, axis.nearestUpEntry(-3.2));
		assertEquals(6, axis.nearestUpEntry(-1.2));
		assertEquals(5, axis.nearestUpEntry(0));
		assertEquals(4, axis.nearestUpEntry(0.1));
		assertEquals(2, axis.nearestUpEntry(2.5));
	}

	@Test
	public void testNearestLowEntry() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-1, axis.nearestLowEntry(-10.0));
		assertEquals(-1, axis.nearestLowEntry(10.0));
		assertEquals(0, axis.nearestLowEntry(-4.5));
		assertEquals(1, axis.nearestLowEntry(-3.2));
		assertEquals(3, axis.nearestLowEntry(-1.2));
		assertEquals(5, axis.nearestLowEntry(0.1));
		assertEquals(5, axis.nearestLowEntry(0));
		assertEquals(7, axis.nearestLowEntry(2.5));

		axis = setupClass(4, -1, 10);
		assertEquals(-1, axis.nearestLowEntry(-10.0));
		assertEquals(-1, axis.nearestLowEntry(10.0));
		assertEquals(8, axis.nearestLowEntry(-4.5));
		assertEquals(7, axis.nearestLowEntry(-3.2));
		assertEquals(5, axis.nearestLowEntry(-1.2));
		assertEquals(3, axis.nearestLowEntry(0.1));
		assertEquals(4, axis.nearestLowEntry(0));
		assertEquals(1, axis.nearestLowEntry(2.5));}

	@Test
	public void testSubset() {
		AxisValues axis = setupClass(-5, 1, 10);
		AxisValues subset = axis.subset(3,6);
		assertEquals(0.0, subset.getMaxValue(),0.1);
		assertEquals(-2.0, subset.getMinValue(),0.1);
		assertEquals(3, subset.size());
		
	}

	@Test
	public void testSize() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(10, axis.size());
	}

	@Test
	public void testClone() {
		AxisValues axis = setupClass(-5, 1, 10);
		AxisValues axis2 = axis.clone();
		try {
			axis.toDataset().checkCompatibility(axis2.toDataset());
		} catch (Exception e) {
			fail("Axis are not compatible, "+e.toString());
		}
		
		assertEquals(axis.getValue(0),axis2.getValue(0),0.1);
		assertEquals(axis.getValue(5),axis2.getValue(5),0.1);
		assertEquals(axis.getValue(9),axis2.getValue(9),0.1);
	}

	@Test
	public void testGetMinValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(-5, axis.getMinValue(),0.1);
	}

	@Test
	public void testSetMinValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.setMinValue(-100);
		assertEquals(-100, axis.getMinValue(),0.1);
	}

	@Test
	public void testGetMaxValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(4, axis.getMaxValue(),0.1);
	}

	@Test
	public void testSetMaxValue() {
		AxisValues axis = setupClass(-5, 1, 10);
		axis.setMaxValue(100);
		assertEquals(100, axis.getMaxValue(),0.1);
	}

	@Test
	public void testIsAscending() {
		AxisValues axis = setupClass(-5, 1, 10);
		if(!axis.isAscending()) fail("IsAscending Failed");
		axis.addValue(-100.0);
		if(axis.isAscending()) fail("IsAscending Failed");
		axis = setupClass(-5, -1, 10);
		if(axis.isAscending()) fail("IsAscending Failed");
 	}

	@Test
	public void testGetSize() {
		AxisValues axis = setupClass(-5, 1, 10);
		assertEquals(10, axis.size());
	}

}
