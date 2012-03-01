/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.editors.outline;

import org.eclipse.jface.action.Action;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.IAntUIConstants;
import org.ganoro.phing.ui.IAntUIPreferenceConstants;
import org.ganoro.phing.ui.PhingUi;
import org.ganoro.phing.ui.editors.PhingEditor;

/**
 * This action toggles whether the Ant Outline page links its selection to the
 * active editor.
 * 
 * @since 3.0
 */
public class ToggleLinkWithEditorAction extends Action {
	
	PhingEditor fEditor;
	
	public ToggleLinkWithEditorAction(PhingEditor editor) {
		super(AntOutlineMessages.ToggleLinkWithEditorAction_0);
		boolean isLinkingEnabled = PhingUi.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR);
		setChecked(isLinkingEnabled);
		fEditor = editor;
		setToolTipText(AntOutlineMessages.ToggleLinkWithEditorAction_1);
		setDescription(AntOutlineMessages.ToggleLinkWithEditorAction_2);
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_LINK_WITH_EDITOR));
	}
	
	public void run() {
		PhingUi.getDefault().getPreferenceStore().setValue(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR, isChecked());
		if (isChecked())
			fEditor.synchronizeOutlinePage(false);
	}
}
