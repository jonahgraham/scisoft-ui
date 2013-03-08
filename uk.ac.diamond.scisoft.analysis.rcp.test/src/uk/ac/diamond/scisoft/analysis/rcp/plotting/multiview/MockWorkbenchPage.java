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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.multiview;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;

class MockWorkbenchPage implements IWorkbenchPage {
	@Override
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void removeSelectionListener(ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void removePostSelectionListener(ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public ISelection getSelection(String partId) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public ISelection getSelection() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addSelectionListener(String partId, ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addSelectionListener(ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addPostSelectionListener(ISelectionListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void removePartListener(IPartListener2 listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void removePartListener(IPartListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public IWorkbenchPartReference getActivePartReference() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public IWorkbenchPart getActivePart() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addPartListener(IPartListener2 listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void addPartListener(IPartListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void zoomOut() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void toggleZoom(IWorkbenchPartReference ref) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public IViewPart showView(String viewId, String secondaryId, int mode) throws PartInitException {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewPart showView(String viewId) throws PartInitException {
		return showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
		
	}

	@Override
	public void showEditor(IEditorReference ref) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void showActionSet(String actionSetID) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void setWorkingSets(IWorkingSet[] sets) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void setPerspective(IPerspectiveDescriptor perspective) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void setPartState(IWorkbenchPartReference ref, int state) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void setEditorReuseThreshold(int openEditors) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void setEditorAreaVisible(boolean showEditorArea) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void savePerspectiveAs(IPerspectiveDescriptor perspective) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void savePerspective() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public boolean saveEditor(IEditorPart editor, boolean confirm) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean saveAllEditors(boolean confirm) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public void reuseEditor(IReusableEditor editor, IEditorInput input) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void resetPerspective() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs, int matchFlags)
			throws MultiPartInitException {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate, int matchFlags)
			throws PartInitException {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate) throws PartInitException {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart openEditor(IEditorInput input, String editorId) throws PartInitException {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean isPartVisible(IWorkbenchPart part) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean isPageZoomed() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean isEditorPinned(IEditorPart editor) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean isEditorAreaVisible() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public void hideView(IViewReference view) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void hideView(IViewPart view) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void hideEditor(IEditorReference ref) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void hideActionSet(String actionSetID) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public IWorkingSet[] getWorkingSets() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IWorkingSet getWorkingSet() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewPart[] getViews() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewPart[] getViewStack(IViewPart part) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewReference[] getViewReferences() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IPerspectiveDescriptor[] getSortedPerspectives() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public String[] getShowViewShortcuts() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IWorkbenchPartReference getReference(IWorkbenchPart part) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public String[] getPerspectiveShortcuts() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IPerspectiveDescriptor getPerspective() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public int getPartState(IWorkbenchPartReference ref) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IPerspectiveDescriptor[] getOpenPerspectives() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public String[] getNewWizardShortcuts() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public INavigationHistory getNavigationHistory() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public String getLabel() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IAdaptable getInput() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IExtensionTracker getExtensionTracker() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart[] getEditors() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public int getEditorReuseThreshold() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorReference[] getEditorReferences() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart[] getDirtyEditors() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IWorkingSet getAggregateWorkingSet() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart getActiveEditor() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewReference findViewReference(String viewId, String secondaryId) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewReference findViewReference(String viewId) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IViewPart findView(String viewId) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public IEditorPart findEditor(IEditorInput input) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public boolean closeEditors(IEditorReference[] editorRefs, boolean save) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public boolean closeEditor(IEditorPart editor, boolean save) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
		
	}

	@Override
	public void closeAllPerspectives(boolean saveEditors, boolean closePage) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public boolean closeAllEditors(boolean save) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public boolean close() {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	@Override
	public void bringToTop(IWorkbenchPart part) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}

	@Override
	public void activate(IWorkbenchPart part) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");

	}
	
	public IMemento[] getEditorState(IEditorReference[] editorRefs,
            boolean includeInputState) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}

	public IEditorReference[] openEditors(IEditorInput[] iei, String[] sa, IMemento[] memos, int i) {
		throw new AssertionFailedError("Methods in MockWorkbenchPage should not be called");
	}
}
