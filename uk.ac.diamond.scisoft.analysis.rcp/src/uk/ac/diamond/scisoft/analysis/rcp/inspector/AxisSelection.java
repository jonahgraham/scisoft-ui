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

package uk.ac.diamond.scisoft.analysis.rcp.inspector;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

/**
 * Class to hold a list of axis names and dataset from which an axis can be selected
 */
public class AxisSelection extends InspectorProperty implements Iterable<String> {
	class AxisSelData implements Comparable<AxisSelData> {
		private boolean selected;
		private int order; // possible order in a list of choices (0 signifies leave to end of list) 
		private AxisChoice data;

		public AxisSelData(AxisChoice axisData, boolean axisSelected) {
			setData(axisData);
			setSelected(axisSelected);
		}

		/**
		 * @param selected The selected to set.
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		/**
		 * @return Returns the selected.
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * @param order The order to set.
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * @return Returns the order.
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * @param data The data to set.
		 */
		public void setData(AxisChoice data) {
			this.data = data;
		}

		/**
		 * @return Returns the data.
		 */
		public AxisChoice getData() {
			return data;
		}

		@Override
		public int compareTo(AxisSelData axisSelData) {
			int cOrder = axisSelData.getOrder();
			if (order == 0)
				return cOrder == 0 ? 0 : 1;
			if (cOrder == 0)
				return -1;
			return order - cOrder;
		}
		
	}

	private final static String propName = "axisselection";
	private int dim;    // dimension (or position index) of dataset
	private int length; // length of axis
	private List<AxisSelData> asData;
	private List<String> names;
	private final String suffix;

	Transformer orderTransformer = new Transformer() {
		@Override
		public Object transform(Object o) {
			if (o instanceof AxisSelData)
				return ((AxisSelData) o).getOrder();
			return null;
		}
	};

	Predicate axisSelectionPredicate = new Predicate() {
		@Override
		public boolean evaluate(Object o) {
			if (o instanceof AxisSelData)
				return ((AxisSelData) o).isSelected();
			return false;
		}
	};

	class OrderPredicate implements Predicate {
		int order;
		public void setOrder(int order) {
			this.order = order;
		}

		@Override
		public boolean evaluate(Object obj) {
			AxisSelData a = (AxisSelData) obj;
			int o = a.getOrder();
			return o == 0 || order < o;
		}
	}

	OrderPredicate orderPredicate = new OrderPredicate();

	/**
	 * Create an axis selection that corresponds to a dataset dimension of given length
	 * @param length 
	 * @param dimension
	 */
	public AxisSelection(int length, int dimension) {
		dim = dimension;
		this.length = length;
		asData = new ArrayList<AxisSelData>();
		names = new ArrayList<String>();
		suffix = ":" + (dim + 1);
	}

	/**
	 * @return Returns the length
	 */
	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append('(');
		for (AxisSelData a : asData) {
			text.append(a.getData().getName());
			if (a.isSelected()) {
				text.append('*');
			}
			text.append(", ");
		}
		if (text.length() > 0) {
			text.deleteCharAt(text.length()-1);
			text.deleteCharAt(text.length()-1);
		}
		text.append(')');
		return text.toString();
	}

	/**
	 * Add axis choice with given order
	 * @param axis
	 * @param order (can be zero to denote last)
	 */
	public void addChoice(AxisChoice axis, int order) {
		String name = axis.getName();
		if (axis.getRank() > 1)
			name += suffix;
		addChoice(name, axis, order);
	}

	/**
	 * Add axis choice with given name and order
	 * @param name
	 * @param axis
	 * @param order (can be zero to denote last)
	 */
	public void addChoice(String name, AxisChoice axis, int order) {
		AxisSelData a;
		int i = names.indexOf(name);
		if (i >= 0) { // existing axis so replace
			a = asData.get(i);
			if (axis != a.getData())
				a.setData(axis);
			int o = a.getOrder();
			if (o == order)
				return;

			names.remove(i);
			asData.remove(i);
		} else {
			a = new AxisSelData(axis, false);
		}

		a.setOrder(order);
		if (order == 0) {
			names.add(name);
			asData.add(a);
		} else {
			orderPredicate.setOrder(order);
			int j = asData.indexOf(CollectionUtils.find(asData, orderPredicate));
			if (j < 0) {
				names.add(name);
				asData.add(a);
			} else {
				names.add(j, name);
				asData.add(j, a);
			}
		}
	}

	/**
	 * @param name
	 * @return true if name is one of possible selections
	 */
	public boolean containsAxis(String name) {
		return names.contains(name);
	}

	/**
	 * Select an axis with given name
	 * @param name
	 * @param fire
	 */
	public void selectAxis(String name, boolean fire) {
		int i = names.indexOf(name);
		if (i < 0)
			return;

		String oldName = getSelectedName();
		for (AxisSelData d: asData)
			d.setSelected(false);

		AxisSelData a = asData.get(i);
		a.setSelected(true);

		if (fire)
			fire(new PropertyChangeEvent(this, propName, oldName, name));
	}

	/**
	 * Select an axis with given index
	 * @param index 
	 */
	public void selectAxis(int index) {
		selectAxis(index, false);
	}

	/**
	 * Select an axis with given index
	 * @param index 
	 * @param fire
	 */
	public void selectAxis(int index, boolean fire) {
		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
		String oldName = null;
		if (a != null) {
			a.setSelected(false);
			oldName = a.getData().getName();
		}
		a = asData.get(index);
		a.setSelected(true);
		if (fire)
			fire(new PropertyChangeEvent(this, propName, oldName, a.getData().getName()));
	}

	/**
	 * @param index 
	 * @return axis name of given index
	 */
	public String getName(int index) {
		return names.get(index);
	}

	/**
	 * @return axis names
	 */
	public List<String> getNames() {
		return names;
	}

	/**
	 * @param index 
	 * @return axis order of given index
	 */
	public int getOrder(int index) {
		AxisSelData a = asData.get(index); 
		return a == null ? -1 : a.getOrder();
	}

	/**
	 * @param index 
	 * @return axis choice of given index
	 */
	public AxisChoice getAxis(int index) {
		AxisSelData a = asData.get(index); 
		return a == null ? null : a.getData();
	}

	/**
	 * @param name
	 * @return axis choice of given name
	 */
	public AxisChoice getAxis(String name) {
		int i = names.indexOf(name);
		return i < 0 ? null : asData.get(i).getData();
	}

	/**
	 * Remove axis choice of given index
	 * @param index
	 */
	public void removeChoice(int index) {
		names.remove(index);
		asData.remove(index);
	}

	/**
	 * Remove axis choice of given name
	 * @param name
	 */
	public void removeChoice(String name) {
		int i = names.indexOf(name);
		if (i < 0)
			return;
		removeChoice(i);
	}

	/**
	 * @return number of choices
	 */
	public int size() {
		return names.size();
	}

	/**
	 * @param index
	 * @return selection status
	 */
	public boolean isSelected(int index) {
		AxisSelData a = asData.get(index); 
		return (a == null) ? false : a.isSelected();
	}

	/**
	 * Get name of selected axis
	 * @return name or null if nothing selected
	 */
	public String getSelectedName() {
		int i = getSelectedIndex();
		return i < 0 ? null : names.get(i);
	}

	/**
	 * Get index of selected axis
	 * @return index or -1 if nothing selected
	 */
	public int getSelectedIndex() {
		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
		return asData.indexOf(a);
	}

	/**
	 * @return selected dimensions
	 */
	public int[] getSelectedIndexMapping() {
		AxisChoice choice = getSelectedAxis();
		if (choice != null)
			return choice.getIndexMapping();	
		return null;
	}

	/**
	 * @return selected choice
	 */
	public AxisChoice getSelectedAxis() {
		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
		return (a == null) ? null : a.getData();
	}

	/**
	 * Call this once finished adding choices to 
	 */
	public void reorderChoices() {

//		Collections.sort(asData);

	}

	/**
	 * @return maximum order
	 */
	@SuppressWarnings({ "unchecked" })
	public int getMaxOrder() {
		List<Integer> orders = (List<Integer>) CollectionUtils.collect(asData, orderTransformer);
		return orders.size() > 0 ? (Integer) Collections.max(orders) : 0;
	}

	/**
	 * @return true if names and axis datasets have same values
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other instanceof AxisSelection) {
			AxisSelection that = (AxisSelection) other;
			
			if (!that.names.equals(names))
				return false;
			
			if (!CollectionUtils.isEqualCollection(asData, that.asData))
				return false;
			
			return true;
			
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = length;
		for (String n : names)
				hash = hash * 17 + n.hashCode();
		for (AxisSelData n : asData)
				hash = hash * 17 + n.hashCode();
		return hash;
	}

	/**
	 * Clone everything but axis choice values
	 */
	@Override
	public AxisSelection clone() {
		AxisSelection selection = new AxisSelection(length, dim);
		for (int i = 0, imax = asData.size(); i < imax; i++) {
			AxisSelData a = asData.get(i);
			selection.addChoice(names.get(i), a.getData().clone(), a.getOrder());
			if (a.isSelected())
				selection.selectAxis(i);
		}
		
		return selection;
	}

	/**
	 * @return iterator over names
	 */
	@Override
	public Iterator<String> iterator() {
		return names.iterator();
	}
}
