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

package uk.ac.diamond.sda.navigator.views;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

public class OpenFileNavigatorHandler extends AbstractHandler {

	private static int secondaryId = 1;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
			//final FileView parent   = (FileView)EclipseUtils.getActivePage().getActivePart();	
			final IViewPart view = EclipseUtils.getActivePage().showView(FileView.ID, FileView.ID+secondaryId, IWorkbenchPage.VIEW_CREATE);
			final IFileView fileView = (IFileView)view;
			secondaryId++;
			//TODO fileView.setRoot(parent.getSelectedFile());
			EclipseUtils.getActivePage().activate(view);
	        return Boolean.TRUE;
        } catch (Exception ne) {
        	throw new ExecutionException("Cannot open file navigator part!", ne);
        }
	}

}
