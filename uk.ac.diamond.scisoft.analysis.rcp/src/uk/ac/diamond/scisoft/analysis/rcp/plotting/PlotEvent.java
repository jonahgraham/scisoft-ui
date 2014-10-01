/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

public class PlotEvent {

	private GuiBean guiBean;
	private GuiBean stashedGuiBean;
	private String          dataBeanAvailable;

	public GuiBean getGuiBean() {
		return guiBean;
	}
	public String getDataBeanAvailable() {
		return dataBeanAvailable;
	}
	public void setDataBeanAvailable(String dataBeanAvailable) {
		this.dataBeanAvailable = dataBeanAvailable;
	}
	public void setGuiBean(GuiBean guiBean) {
		this.guiBean = guiBean;
	}
	public GuiBean getStashedGuiBean() {
		return stashedGuiBean;
	}
	public void setStashedGuiBean(GuiBean stashedGuiBean) {
		this.stashedGuiBean = stashedGuiBean;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guiBean == null) ? 0 : guiBean.hashCode());
		result = prime * result + ((stashedGuiBean == null) ? 0 : stashedGuiBean.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlotEvent other = (PlotEvent) obj;
		if (guiBean == null) {
			if (other.guiBean != null)
				return false;
		} else if (!guiBean.equals(other.guiBean))
			return false;
		if (stashedGuiBean == null) {
			if (other.stashedGuiBean != null)
				return false;
		} else if (!stashedGuiBean.equals(other.stashedGuiBean))
			return false;
		return true;
	}

}
