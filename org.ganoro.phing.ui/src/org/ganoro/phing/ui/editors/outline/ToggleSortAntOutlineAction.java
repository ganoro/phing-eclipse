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

/**
 * An action which toggles sorting in the Ant outline page.
 */
public class ToggleSortAntOutlineAction extends Action {

	private AntEditorContentOutlinePage fPage;
	
	public ToggleSortAntOutlineAction(AntEditorContentOutlinePage page) {
		super(AntOutlineMessages.ToggleSortAntOutlineAction_0);
		fPage = page;
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_SORT_OUTLINE));
		setToolTipText(AntOutlineMessages.ToggleSortAntOutlineAction_0);
		setChecked(fPage.isSort());
	}
	
	/**
	 * Toggles the sorting of targets in the Ant outline
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.setSort(isChecked());
	}
	
}
