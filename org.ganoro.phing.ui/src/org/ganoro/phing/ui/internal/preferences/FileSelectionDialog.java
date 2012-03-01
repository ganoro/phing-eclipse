/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.ganoro.phing.ui.internal.preferences;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.PhingUi;

public class FileSelectionDialog extends ElementTreeSelectionDialog {
	
	private FileFilter fFilter;
	private String fFilterMessage;
	private boolean fShowAll= false;
	private final static String DIALOG_SETTING= "AntPropertiesFileSelectionDialog.showAll";  //$NON-NLS-1$
	private final static String LAST_CONTAINER= "AntPropertiesFileSelectionDialog.lastContainer";  //$NON-NLS-1$
	
	public FileSelectionDialog(Shell parent, List files, String title, String message, String filterExtension, String filterMessage) {
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		
		setTitle(title);
		setMessage(message);
		fFilter= new FileFilter(files, filterExtension);
		fFilterMessage= filterMessage;
		setInput(ResourcesPlugin.getWorkspace().getRoot());	
        setComparator(new ResourceComparator(ResourceComparator.NAME));
		
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, PhingUi.getUniqueIdentifier(), 0, IPhingCoreConstants.EMPTY_STRING, null); 
				}
				for (int i= 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IFile)) {
						return new Status(IStatus.ERROR, PhingUi.getUniqueIdentifier(), 0, IPhingCoreConstants.EMPTY_STRING, null); 
					}
				}
				return new Status(IStatus.OK, PhingUi.getUniqueIdentifier(), 0, IPhingCoreConstants.EMPTY_STRING, null); 
			}			
		};
		setValidator(validator);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		Composite result= (Composite)super.createDialogArea(parent);
		final Button button = new Button(result, SWT.CHECK);
		button.setText(fFilterMessage);
		button.setFont(parent.getFont());
		
		IDialogSettings settings= PhingUi.getDefault().getDialogSettings();
		fShowAll= settings.getBoolean(DIALOG_SETTING);
		
		String lastPath= settings.get(LAST_CONTAINER);
		if (lastPath != null) {
			IPath path= Path.fromPortableString(lastPath);
			IResource resource= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			setInitialSelection(resource);
		}
		
		fFilter.considerExtension(!fShowAll);
		getTreeViewer().addFilter(fFilter);
		if (!fShowAll) {
			button.setSelection(true);
		}
		
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (button.getSelection()) {
					fShowAll= false;
				} else {
					fShowAll= true;
				}
				fFilter.considerExtension(!fShowAll);
				getTreeViewer().refresh();
			}
		});
		applyDialogFont(result);		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		IDialogSettings settings= PhingUi.getDefault().getDialogSettings();
		settings.put(DIALOG_SETTING, fShowAll);
		
		Object[] result= getResult();
		if (result != null && result.length > 0) {
			settings.put(LAST_CONTAINER, ((IResource)result[0]).getParent().getFullPath().toPortableString());
		}
		return super.close();
	}
}
