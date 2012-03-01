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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.ganoro.phing.launching.PhingLaunching;
import org.ganoro.phing.ui.IAntUIPreferenceConstants;
import org.ganoro.phing.ui.PhingUi;

import com.ibm.icu.text.MessageFormat;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Allows us to override the default behavior of the default {@link IntegerFieldEditor} to work
	 * on a core preference context
	 * 
	 * @since 3.6
	 */
	class AntIntegerFieldEditor extends IntegerFieldEditor {
		String node = null, key = null;
		int defaultvalue = -1;
		
		/**
		 * Constructor
		 * @param node the identifier of the node we want to set the preference in, i.e. org.eclipse.ant.launching
		 * @param key the preference key to map the value to
		 * @param title the title of the field editor
		 * @param parent the parent to add the field editor to
		 * @param defaultvalue the default value to return when looking up stored values
		 */
		public AntIntegerFieldEditor(String node, String key, String title, Composite parent, int defaultvalue) {
			super(key, title, parent);
			this.node = node;
			this.key = key;
			this.defaultvalue = defaultvalue;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.IntegerFieldEditor#doStore()
		 */
		protected void doStore() {
			InstanceScope.INSTANCE.getNode(node).putInt(key, Integer.parseInt(getStringValue()));
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.FieldEditor#load()
		 */
		public void load() {
			setStringValue(Integer.toString(Platform.getPreferencesService().getInt(node, key, defaultvalue, null)));
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.FieldEditor#loadDefault()
		 */
		public void loadDefault() {
			setStringValue(Integer.toString(DefaultScope.INSTANCE.getNode(PhingLaunching.getUniqueIdentifier()).getInt(key, defaultvalue)));
		}
	}
	
	private List fConsoleColorList;
	private ColorEditor fConsoleColorEditor;
	private IntegerFieldEditor timeout;
	private BooleanFieldEditor workspacejre = null;

	private BooleanFieldEditor fToolsWarningEditor= null;
	
	// Array containing the message to display, the preference key, and the 
	// default value (initialized in storeInitialValues()) for each color preference
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.AntPreferencePage__Error__2, IAntUIPreferenceConstants.CONSOLE_ERROR_COLOR, null},
		{AntPreferencesMessages.AntPreferencePage__Warning__3, IAntUIPreferenceConstants.CONSOLE_WARNING_COLOR, null},
		{AntPreferencesMessages.AntPreferencePage_I_nformation__4, IAntUIPreferenceConstants.CONSOLE_INFO_COLOR, null},
		{AntPreferencesMessages.AntPreferencePage_Ve_rbose__5, IAntUIPreferenceConstants.CONSOLE_VERBOSE_COLOR, null},
		{AntPreferencesMessages.AntPreferencePage_Deb_ug__6, IAntUIPreferenceConstants.CONSOLE_DEBUG_COLOR, null},
	};

	/**
 	 * Create the Ant page.
     */
	public AntPreferencePage() {
		super(GRID);
		setDescription(AntPreferencesMessages.AntPreferencePage_General);
		setPreferenceStore(PhingUi.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		storeAppliedValues();

		Font font= getFieldEditorParent().getFont();
		Label label= new Label(getFieldEditorParent(), SWT.WRAP);
		label.setText(AntPreferencesMessages.AntPreferencePage_Enter);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		gd.widthHint= convertWidthInCharsToPixels(60);
		label.setLayoutData(gd);
		label.setLayoutData(gd);
		label.setFont(font);
		
		StringFieldEditor editor = new StringFieldEditor(IAntUIPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, AntPreferencesMessages.AntPreferencePage__Names__3, getFieldEditorParent());
		addField(editor);

		timeout = new AntIntegerFieldEditor(PhingLaunching.getUniqueIdentifier(), 
				"ANT_COMMUNICATION_TIMEOUT", 
				AntPreferencesMessages.AntPreferencePage_13, 
				getFieldEditorParent(), 2000
				);
        int minValue = 20000;
        int maxValue = 1200000;
        timeout.setValidRange(minValue, maxValue);
        timeout.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        timeout.setErrorMessage(MessageFormat.format(AntPreferencesMessages.AntPreferencePage_14, new Object[] {new Integer(minValue), new Integer(maxValue)}));
        addField(timeout);
        
        editor = new URLFieldEditor(IAntUIPreferenceConstants.DOCUMENTATION_URL, AntPreferencesMessages.AntPreferencePage_2, getFieldEditorParent());
		addField(editor);
		
		workspacejre = new BooleanFieldEditor(IAntUIPreferenceConstants.USE_WORKSPACE_JRE, AntPreferencesMessages.always_run_in_workspace_jre, getFieldEditorParent());
	    workspacejre.fillIntoGrid(getFieldEditorParent(), 3);
	    addField(workspacejre);
	      
	    createSpace();
		
		if (!PhingUi.isMacOS()) {
			//the mac does not have a tools.jar Bug 40778
		    label= new Label(getFieldEditorParent(), SWT.WRAP);
			label.setText(AntPreferencesMessages.AntPreferencePage_0);
			gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan= 3;
			gd.widthHint= convertWidthInCharsToPixels(60);
			label.setLayoutData(gd);
			label.setFont(font);
			fToolsWarningEditor= new BooleanFieldEditor(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, AntPreferencesMessages.AntPreferencePage_1, getFieldEditorParent());
			addField(fToolsWarningEditor);
			createSpace();
		}
		
		addField(new BooleanFieldEditor(IAntUIPreferenceConstants.ANT_ERROR_DIALOG, AntPreferencesMessages.AntPreferencePage_12, getFieldEditorParent()));
		createSpace();
		
		addField(new BooleanFieldEditor(IAntUIPreferenceConstants.ANT_CREATE_MARKERS, AntPreferencesMessages.AntPreferencePage_15, getFieldEditorParent()));
		label= new Label(getFieldEditorParent(), SWT.WRAP);
		label.setText(AntPreferencesMessages.AntPreferencePage_16);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		gd.widthHint= convertWidthInCharsToPixels(60);
		label.setLayoutData(gd);
		label.setFont(font);
		
        createSpace();
		createColorComposite();
		getPreferenceStore().addPropertyChangeListener(this);
	}
	
	private void createSpace() {
		Label label= new Label(getFieldEditorParent(), SWT.NONE);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		label.setLayoutData(gd);
	}

	/**
	 * Stores the initial values of the color preferences. The preference values are updated 
	 * on the fly as the user edits them (instead of only when they press "Apply"). We need
	 * to store the old values so that we can reset them when the user chooses "Cancel".
	 */
	private void storeAppliedValues() {
		IPreferenceStore store= getPreferenceStore();
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String preference = fAppearanceColorListModel[i][1];
			fAppearanceColorListModel[i][2]= store.getString(preference);
		}
	}
	
	private void createColorComposite() {
		Font font= getFieldEditorParent().getFont();
		Label label= new Label(getFieldEditorParent(), SWT.LEFT);
		label.setText(AntPreferencesMessages.AntPreferencePage_Ant_Color_Options__6); 
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
				
		Composite editorComposite= new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		editorComposite.setFont(font);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fConsoleColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(8);
		fConsoleColorList.setLayoutData(gd);
		fConsoleColorList.setFont(font);
				
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stylesComposite.setFont(font);

		label= new Label(stylesComposite, SWT.LEFT);
		label.setText(AntPreferencesMessages.AntPreferencePage_Color__7);
		label.setFont(font);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fConsoleColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fConsoleColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		foregroundColorButton.setFont(font);

		fConsoleColorList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i= fConsoleColorList.getSelectionIndex();
				if (i == -1) { //bug 85590
					return;
				}
				String key= fAppearanceColorListModel[i][1];
				PreferenceConverter.setValue(getPreferenceStore(), key, fConsoleColorEditor.getColorValue());
			}
		});
	}
	
	/**
	 * Restore all color preferences to their values when the page was opened.
	 */
	public boolean performCancel() {
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String preference = fAppearanceColorListModel[i][1];
			PreferenceConverter.setValue(getPreferenceStore(), preference, StringConverter.asRGB(fAppearanceColorListModel[i][2]));
		}
		return super.performCancel();
	}
	
	/**
	 * When the user applies the preferences, update the set of stored
	 * preferences so that we will fall back to the applied values on Cancel.
	 */
	public boolean performOk() {
		storeAppliedValues();
		return super.performOk();
	}
	
	private void handleAppearanceColorListSelection() {	
		int i= fConsoleColorList.getSelectionIndex();
		if (i == -1) { //bug 85590
			return;
		}
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), key);
		fConsoleColorEditor.setColorValue(rgb);		
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fConsoleColorList.add(fAppearanceColorListModel[i][0]);
		}
		fConsoleColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fConsoleColorList != null && !fConsoleColorList.isDisposed()) {
					fConsoleColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
	}
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String key= fAppearanceColorListModel[i][1];
			PreferenceConverter.setValue(getPreferenceStore(), key, PreferenceConverter.getDefaultColor(getPreferenceStore(), key));
		}
		handleAppearanceColorListSelection();
		super.performDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING)) {
			if (fToolsWarningEditor != null) {
				fToolsWarningEditor.load();
			}
		} else {
			super.propertyChange(event);
		}
	}
}
