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
import org.eclipse.swt.custom.BusyIndicator;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.IAntUIConstants;

/** 
 * An action which toggles filtering of properties from the Ant outline.
 */
public class FilterPropertiesAction extends Action {
	
	private AntEditorContentOutlinePage fPage;
	
	public FilterPropertiesAction(AntEditorContentOutlinePage page) {
		super(AntOutlineMessages.FilterPropertiesAction_0);
		fPage = page;
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_FILTER_PROPERTIES));
		setToolTipText(AntOutlineMessages.FilterPropertiesAction_0);
		setChecked(fPage.filterProperties());
	}
	
	/**
	 * Toggles the filtering of properties from the Ant outline
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		BusyIndicator.showWhile(fPage.getControl().getDisplay(), new Runnable() {
			public void run() {
				fPage.setFilterProperties(isChecked());
			}
		});
	}
}
