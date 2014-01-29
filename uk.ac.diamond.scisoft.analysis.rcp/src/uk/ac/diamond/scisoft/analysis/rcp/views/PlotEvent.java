/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

class PlotEvent {

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
