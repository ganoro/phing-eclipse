/*******************************************************************************
 * Copyright (c) 2002, 2008 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH,
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *     John-Mason P. Shackelford - bug 40255
 *******************************************************************************/

package org.ganoro.phing.ui.editors;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.php.internal.ui.actions.IPHPEditorActionDefinitionIds;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.ganoro.phing.ui.editors.actions.OpenExternalDocAction;
import org.ganoro.phing.ui.editors.actions.ToggleAutoReconcileAction;
import org.ganoro.phing.ui.editors.actions.TogglePresentationAction;

/**
 * Contributes interesting Ant Editor actions to the desktop's Edit menu and the
 * toolbar.
 * 
 */
public class AntEditorActionContributor extends TextEditorActionContributor {

	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentFormat;
	private TogglePresentationAction fTogglePresentation;
	private OpenExternalDocAction fOpenExternalDocAction;
	private ToggleAutoReconcileAction fToggleAutoReconcileAction;

	public AntEditorActionContributor() {
		super();
		ResourceBundle bundle = AntEditorMessages.getResourceBundle();
		fContentAssistProposal = new RetargetTextEditorAction(bundle,
				"ContentAssistProposal."); //$NON-NLS-1$
		fContentAssistProposal
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentFormat = new RetargetTextEditorAction(bundle, "ContentFormat."); //$NON-NLS-1$
		fContentFormat
				.setActionDefinitionId(IPHPEditorActionDefinitionIds.FORMAT);
		fTogglePresentation = new TogglePresentationAction();
		fToggleAutoReconcileAction = new ToggleAutoReconcileAction();

	}

	protected void initializeActions(PhingEditor editor) {
		fOpenExternalDocAction = new OpenExternalDocAction(editor);
	}

	private void doSetActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor editor = null;
		if (part instanceof ITextEditor) {
			editor = (ITextEditor) part;
		}

		fContentAssistProposal.setAction(getAction(editor,
				ITextEditorActionConstants.CONTENT_ASSIST));
		fContentFormat.setAction(getAction(editor, "ContentFormat")); //$NON-NLS-1$

		if (editor instanceof PhingEditor) {
			PhingEditor antEditor = (PhingEditor) part;
			if (fOpenExternalDocAction != null) {
				fOpenExternalDocAction.setActiveEditor(null, editor);
			}
		}

		if (fTogglePresentation != null) {
			fTogglePresentation.setEditor(editor);
		}
		if (fToggleAutoReconcileAction != null) {
			fToggleAutoReconcileAction.setEditor(editor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse
	 * .jface.action.IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(org.eclipse.ui.
	 * IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);

		IMenuManager menuManager = bars.getMenuManager();
		IMenuManager editMenu = menuManager
				.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(fContentAssistProposal);
			editMenu.add(fContentFormat);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse
	 * .ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		doSetActiveEditor(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionBarContributor#init(org.eclipse.ui.IActionBars
	 * , org.eclipse.ui.IWorkbenchPage)
	 */
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);
		bars.setGlobalActionHandler(
				ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY,
				fTogglePresentation);
		bars.setGlobalActionHandler(
				"org.eclipse.ant.ui.toggleAutoReconcile", fToggleAutoReconcileAction); //$NON-NLS-1$
	}
}
