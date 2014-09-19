/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.hdf5;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.Metadata;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.hdf5.api.HDF5Attribute;
import org.eclipse.dawnsci.hdf5.api.HDF5Dataset;
import org.eclipse.dawnsci.hdf5.api.HDF5File;
import org.eclipse.dawnsci.hdf5.api.HDF5Group;
import org.eclipse.dawnsci.hdf5.api.HDF5Node;
import org.eclipse.dawnsci.hdf5.api.HDF5NodeLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusHDF5Loader;
import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;

public class HDF5Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(HDF5Utils.class);

	/**
	 * Create a (HDF5) dataset selection from given node link. It defaults the inspector type
	 * to a line and leaves file name null
	 * @param link
	 * @param isAxisFortranOrder in most cases, this should be set to true
	 * @return HDF5 selection
	 */
	public static HDF5Selection createDatasetSelection(HDF5NodeLink link, final boolean isAxisFortranOrder) {
		// two cases: axis and primary or axes
		// iterate through each child to find axes and primary attributes
		HDF5Node node = link.getDestination();
		List<AxisChoice> choices = new ArrayList<AxisChoice>();
		HDF5Group gNode = null;
		HDF5Dataset dNode = null;

		// see if chosen node is a NXdata class
		HDF5Attribute stringAttr = node.getAttribute(HDF5File.NXCLASS);
		String nxClass = stringAttr != null ? stringAttr.getFirstElement() : null;
		if (nxClass == null || nxClass.equals(NexusHDF5Loader.SDS)) {
			if (!(node instanceof HDF5Dataset))
				return null;

			dNode = (HDF5Dataset) node;
			if (!dNode.isSupported())
				return null;

			gNode = (HDF5Group) link.getSource(); // before hunting for axes
		} else if (nxClass.equals(NexusHDF5Loader.NX_DATA)) {
			assert node instanceof HDF5Group;
			gNode = (HDF5Group) node;
			// check if group has signal attribute (is this official?)
			if (gNode.containsAttribute(NexusHDF5Loader.NX_SIGNAL)) {
				HDF5Attribute a = gNode.getAttribute(NexusHDF5Loader.NX_SIGNAL);
				if (a.isString()) {
					String n = a.getFirstElement();
					if (gNode.containsDataset(n)) {
						dNode = gNode.getDataset(n);						
					} else {
						logger.warn("Signal attribute in group points to a missing dataset called {}", n);
					}
				} else {
					logger.warn("Signal attribute in group is not a string");
				}
			}

			if (dNode == null) {
				// find data (@signal=1)
				for (HDF5NodeLink l : gNode) {
					if (l.isDestinationADataset()) {
						dNode = (HDF5Dataset) l.getDestination();
						if (dNode.containsAttribute(NexusHDF5Loader.NX_SIGNAL) && dNode.getAttribute(NexusHDF5Loader.NX_SIGNAL).getFirstElement().equals("1")
								&& dNode.isSupported()) {
							link = l;
							break; // only one signal per NXdata item
						}
						dNode = null;
					}
				}
			}

			if (dNode == null && gNode.containsDataset(NexusHDF5Loader.DATA)) {
				// fallback to "data" when no signal attribute is found
				dNode = gNode.getDataset(NexusHDF5Loader.DATA);
			}
		}

		if (dNode == null || gNode == null) return null;
		ILazyDataset cData = dNode.getDataset(); // chosen dataset

		if (cData == null || cData.getSize() == 0) {
			logger.warn("Chosen data {}, has zero size", dNode);
			return null;
		}

		if (cData.getSize() == 1) {
			logger.warn("Chosen data {}, has only one value", dNode);
			return null;
		}

		// find possible @long_name
		stringAttr = dNode.getAttribute(NexusHDF5Loader.NX_NAME);
		if (stringAttr != null && stringAttr.isString()) {
			String n = stringAttr.getFirstElement();
			if (n != null && n.length() > 0)
				cData.setName(n);
		}

		// Fix to http://jira.diamond.ac.uk/browse/DAWNSCI-333. We put the path in the meta
		// data in order to put a title containing the file in the plot.
		if (link.getFile() != null && link.getFile().getFilename() != null) {
			final Metadata meta = new Metadata();
			meta.setFilePath(link.getFile().getFilename());
			cData.setMetadata(meta);
			// TODO Maybe	dNode.getAttributeNameIterator()
		}

		// add errors
		ILazyDataset eData = null;
		String cName = cData.getName();
		String eName;
		if (!NexusHDF5Loader.NX_ERRORS.equals(cName)) { 
			eName = cName + NexusHDF5Loader.NX_ERRORS_SUFFIX;
			if (!gNode.containsDataset(eName) && !cName.equals(link.getName())) {
				eName = link.getName() + NexusHDF5Loader.NX_ERRORS_SUFFIX;
			}
			if (gNode.containsDataset(eName)) {
				eData = gNode.getDataset(eName).getDataset();
				eData.setName(eName);
			} else if (gNode.containsDataset(NexusHDF5Loader.NX_ERRORS)) { // fall back
				eData = gNode.getDataset(NexusHDF5Loader.NX_ERRORS).getDataset();
				eData.setName(NexusHDF5Loader.NX_ERRORS);
			}
		}
		if (eData != null && !AbstractDataset.areShapesCompatible(cData.getShape(), eData.getShape(), -1)) {
			eData = null;
		}
		cData.setError(eData);

		// remove extraneous dimensions
		cData.squeeze(true);
		
		// set up slices
		int[] shape = cData.getShape();
		int rank = shape.length;

		// scan children for SDS as possible axes (could be referenced by @axes)
		for (HDF5NodeLink l : gNode) {
			if (l.isDestinationADataset()) {
				HDF5Dataset d = (HDF5Dataset) l.getDestination();
				if (!d.isSupported() || d.isString() || dNode == d)
					continue;
				if (d.containsAttribute(NexusHDF5Loader.NX_SIGNAL) && d.getAttribute(NexusHDF5Loader.NX_SIGNAL).getFirstElement().equals("1"))
					continue;

				ILazyDataset a = d.getDataset();

				try {
					int[] s = a.getShape();
					s = AbstractDataset.squeezeShape(s, true);

					if (s.length == 0 || a.getSize() == 1)
						continue;  // don't make a 0D dataset as the data has already been squeezed
	
					a.squeeze(true);

					int[] ashape = a.getShape();

					AxisChoice choice = new AxisChoice(a);
					stringAttr = d.getAttribute(NexusHDF5Loader.NX_NAME);
					if (stringAttr != null && stringAttr.isString())
						choice.setLongName(stringAttr.getFirstElement());

					// add errors
					cName = choice.getName();
					if (cName == null)
						cName = l.getName();
					eName = cName + NexusHDF5Loader.NX_ERRORS_SUFFIX;
					if (!gNode.containsDataset(eName) && !cName.equals(l.getName())) {
						eName = l.getName() + NexusHDF5Loader.NX_ERRORS_SUFFIX;
					}
					if (gNode.containsDataset(eName)) {
						eData = gNode.getDataset(eName).getDataset();
						eData.setName(eName);
						if (s.length != 0) // don't make a 0D dataset
							eData.squeeze(true);

						if (AbstractDataset.areShapesCompatible(ashape, eData.getShape(), -1)) {
							a.setError(eData);
						}
					}

					HDF5Attribute attr;
					attr = d.getAttribute(NexusHDF5Loader.NX_PRIMARY);
					if (attr != null) {
						if (attr.isString()) {
							Integer intPrimary = Integer.parseInt(attr.getFirstElement());
							choice.setPrimary(intPrimary);
						} else {
							Dataset attrd = attr.getValue();
							choice.setPrimary(attrd.getInt(0));
						}
					}

					int[] intAxis = null;
					HDF5Attribute attrLabel = null;
					String indAttr = l.getName() + NexusHDF5Loader.NX_INDICES_SUFFIX;
					if (gNode.containsAttribute(indAttr)) {
						// deal with index mapping from @*_indices
						attr = gNode.getAttribute(indAttr);
						if (attr.isString()) {
							String[] str = parseString(attr.getFirstElement());
							intAxis = new int[str.length];
							for (int i = 0; i < str.length; i++) {
								intAxis[i] = Integer.parseInt(str[i]) - 1;
							}
							choice.setPrimary(1);
						}
					}

					if (intAxis == null) {
						attr = d.getAttribute(NexusHDF5Loader.NX_AXIS);
						attrLabel = d.getAttribute(NexusHDF5Loader.NX_LABEL);
						if (attr != null) {
							if (attr.isString()) {
								String[] str = attr.getFirstElement().split(",");
								if (str.length == ashape.length) {
									intAxis = new int[str.length];
									for (int i = 0; i < str.length; i++) {
										int j = Integer.parseInt(str[i]) - 1;
										intAxis[i] = isAxisFortranOrder ? j : rank - 1 - j; // fix C (row-major) dimension
									}
								}
							} else {
								Dataset attrd = attr.getValue();
								if (attrd.getSize() == ashape.length) {
									intAxis = new int[attrd.getSize()];
									IndexIterator it = attrd.getIterator();
									int i = 0;
									while (it.hasNext()) {
										int j = (int) attrd.getElementLongAbs(it.index) - 1;
										intAxis[i++] = isAxisFortranOrder ? j : rank - 1 - j; // fix C (row-major) dimension
									}
								}
							}

							if (intAxis == null) {
								logger.warn("Axis attribute {} does not match rank", a.getName());
							} else {
								// check that @axis matches data dimensions
								for (int i = 0; i < intAxis.length; i++) {
									int al = ashape[i];
									int il = intAxis[i];
									if (il < 0 || il >= rank || al != shape[il]) {
										intAxis = null;
										logger.warn("Axis attribute {} does not match shape", a.getName());
										break;
									}
								}
							}
						}

					}

					if (intAxis == null) {
						// remedy bogus or missing @axis by simply pairing matching dimension
						// lengths to the signal dataset shape (this may be wrong as transposes in
						// common dimension lengths can occur)
						logger.warn("Creating index mapping from axis shape");
						Map<Integer, Integer> dims = new LinkedHashMap<Integer, Integer>();
						for (int i = 0; i < rank; i++) {
							dims.put(i, shape[i]);
						}
						intAxis = new int[ashape.length];
						for (int i = 0; i < intAxis.length; i++) {
							int al = ashape[i];
							intAxis[i] = -1;
							for (int k : dims.keySet()) {
								if (al == dims.get(k)) { // find first signal dimension length that matches
									intAxis[i] = k;
									dims.remove(k);
									break;
								}
							}
							if (intAxis[i] == -1)
								throw new IllegalArgumentException(
										"Axis dimension does not match any data dimension");
						}
					}

					choice.setIndexMapping(intAxis);
					if (attrLabel != null) {
						if (attrLabel.isString()) {
							int j = Integer.parseInt(attrLabel.getFirstElement()) - 1;
							choice.setAxisNumber(isAxisFortranOrder ? j : rank - 1 - j); // fix C (row-major) dimension
						} else {
							int j = attrLabel.getValue().getInt(0) - 1;
							choice.setAxisNumber(isAxisFortranOrder ? j : rank - 1 - j); // fix C (row-major) dimension
						}
					} else {
						choice.setAxisNumber(intAxis[intAxis.length-1]);
					}

					choices.add(choice);
				} catch (Exception e) {
					logger.warn("Axis attributes in {} are invalid - {}", a.getName(), e.getMessage());
					continue;
				}
			}
		}

		List<String> aNames = new ArrayList<String>();
		HDF5Attribute axesAttr = dNode.getAttribute(NexusHDF5Loader.NX_AXES);
		if (axesAttr == null) { // cope with @axes being in group
			axesAttr = gNode.getAttribute(NexusHDF5Loader.NX_AXES);
			if (axesAttr != null)
				logger.warn("Found @{} tag in group (not in '{}' dataset)", new Object[] {NexusHDF5Loader.NX_AXES, gNode.findLinkedNodeName(dNode)});
		}

		if (axesAttr != null) { // check axes attribute for list axes
			// check if axes referenced by data's @axes tag exists
			String[] names = parseString(axesAttr.getFirstElement());
			for (String s : names) {
				boolean flg = false;
				for (AxisChoice c : choices) {
					if (c.equals(s)) {
						flg = true;
						break;
					}
				}
				if (flg) {
					aNames.add(s);
				} else {
					logger.warn("Referenced axis {} does not exist in tree node {}", s, node);
					aNames.add(null);
				}
			}
		}

		// build up list of choice per dimension
		List<AxisSelection> axes  = new ArrayList<AxisSelection>(); // list of axes for each dimension

		for (int i = 0; i < rank; i++) {
			int len = shape[i];
			AxisSelection aSel = new AxisSelection(len, i);
			axes.add(aSel);
			for (AxisChoice c : choices) {
				if (c.getAxisNumber() == i) {
					// add if choice has been designated as for this dimension
					aSel.addChoice(c, c.getPrimary());
				} else if (c.isDimensionUsed(i)) {
					// add if axis index mapping refers to this dimension
					aSel.addChoice(c, 0);
				} else if (aNames.contains(c.getName())) {
					// assume order of axes names FIXME
					// add if name is in list of axis names
					if (aNames.indexOf(c.getName()) == i && ArrayUtils.contains(c.getValues().getShape(), len))
						aSel.addChoice(c, 1);
				}
			}

			// add in an automatically generated axis with top order so it appears after primary axes
			Dataset axis = DatasetFactory.createRange(len, Dataset.INT32);
			axis.setName(AbstractExplorer.DIM_PREFIX + (i + 1));
			AxisChoice newChoice = new AxisChoice(axis);
			newChoice.setAxisNumber(i);
			aSel.addChoice(newChoice, aSel.getMaxOrder() + 1);
		}

		InspectorType itype = InspectorType.LINE;
		if (shape.length > 1) {
			if (shape[shape.length - 2] > 1) // only set image type in this case
				itype = InspectorType.IMAGE;
		}
		return new HDF5Selection(itype, link.getFile().getFilename(), link.getFullName(), axes, cData);
	}

	private static String[] parseString(String s) {
		s = s.trim();
		if (s.startsWith("[")) { // strip opening and closing brackets
			s = s.substring(1, s.length() - 1);
		}

		return s.split("[:,]");
	}
}
