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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import java.io.Serializable;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * The <code>IGuiInfoManager</code> interface provides protocol
 * for saving and restoring GUI state typically between a client and multiple
 * subscribers. 
 * 
 * A concrete gui update manager implementation can be found,
 *  {@link uk.ac.diamond.scisoft.analysis.rcp.views.PlotView <code>PlotView</code>}
 * 
 */
public interface IGuiInfoManager {

	/**
	 * This method allows for interested parties to get relevant GUI information
	 * from the client
	 * 
	 * @return The data which specifies the gui state
	 */
	public abstract GuiBean getGUIInfo();

	/**
	 * This method allows interested parties to push relevant GUI information to the 
	 * client. Information can later be retrieved using <code>getGuiState</code>
	 * 
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 */
	public abstract void putGUIInfo(GuiParameters key, Serializable value);

	/**
	 * This method allows interested parties to remove GUI information from the client
	 *  
	 * @param key key with which the specified value is to be associated
	 */
	public abstract void removeGUIInfo(GuiParameters key);

	/**
	 * Mute client so no GUI information is pushed
	 */
	public void mute();

	/**
	 * Un-mute client and push latest information
	 */
	public void unmute();
	
}
