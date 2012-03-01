/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.internal.model;

import org.eclipse.jface.preference.IPreferenceStore;
import org.ganoro.phing.ui.PhingUi;
import org.ganoro.phing.ui.internal.preferences.AntEditorPreferenceConstants;

public class AntCommentNode extends AntElementNode {
	public AntCommentNode() {
		super("AntComment"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#isStructuralNode()
	 */
	public boolean isStructuralNode() {
		return false;
	}
	
	public boolean collapseProjection() {
		IPreferenceStore store= PhingUi.getDefault().getPreferenceStore();		
		if (store.getBoolean(AntEditorPreferenceConstants.EDITOR_FOLDING_COMMENTS)) {
			return true;
		}
		return false;
	}
}
