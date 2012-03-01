/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.ganoro.phing.ui.internal.preferences;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.ganoro.phing.core.AntCorePreferences;
import org.ganoro.phing.core.PhingCore;
import org.ganoro.phing.core.Property;
import org.ganoro.phing.core.Task;
import org.ganoro.phing.core.Type;
import org.ganoro.phing.ui.PhingUi;

/**
 * Ant preference page to set the classpath, tasks, types and properties.
 */
public class AntRuntimePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private AntTasksPage tasksPage;
	private AntTypesPage typesPage;
	private AntPropertiesPage propertiesPage;
	
	/**
	 * Creates the preference page
	 */
	public AntRuntimePreferencePage() {
		setDescription(AntPreferencesMessages.AntPreferencePage_description);
		setPreferenceStore(PhingUi.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setFont(parent.getFont());

		tasksPage = new AntTasksPage(this);
		tasksPage.createTabItem(folder);
		
		typesPage = new AntTypesPage(this);
		typesPage.createTabItem(folder);

		propertiesPage= new AntPropertiesPage(this);
		propertiesPage.createTabItem(folder);
		
		tasksPage.initialize();
		typesPage.initialize();
		propertiesPage.initialize();

		return folder;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		
		AntCorePreferences prefs = PhingCore.getPlugin().getPreferences();
		tasksPage.setInput(prefs.getDefaultTasks());
		typesPage.setInput(prefs.getDefaultTypes());
		propertiesPage.performDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		AntCorePreferences prefs = PhingCore.getPlugin().getPreferences();
		IDialogSettings settings = PhingUi.getDefault().getDialogSettings();
		
		List contents = tasksPage.getContents(false);
		if (contents != null) {
			Task[] tasks = (Task[]) contents.toArray(new Task[contents.size()]);
			prefs.setCustomTasks(tasks);
		}
		
		tasksPage.saveColumnSettings(settings);
		
		contents = typesPage.getContents(false);
		if (contents != null) {
			Type[] types = (Type[]) contents.toArray(new Type[contents.size()]);
			prefs.setCustomTypes(types);
		}
		
		typesPage.saveColumnSettings(settings);
		
		contents = propertiesPage.getProperties();
		if (contents != null) {
			Property[] properties = (Property[]) contents.toArray(new Property[contents.size()]);
			prefs.setCustomProperties(properties);
		}
		
		String[] files = propertiesPage.getPropertyFiles();
		prefs.setCustomPropertyFiles(files);
		
		propertiesPage.saveAdditionalSettings();
		
		prefs.updatePluginPreferences();
	
		return super.performOk();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected GridData setButtonLayoutData(Button button) {
		return super.setButtonLayoutData(button);
	}
	
	protected List getLibraryEntries() {
		List urls= new ArrayList();
		return urls;
	}
}
