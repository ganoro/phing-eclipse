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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/*
 * The page for setting the Ant editor code assist options.
 */
public class AntCodeAssistPreferencePage extends AbstractAntEditorPreferencePage {
		
	private Control fAutoInsertDelayText;
	private Control fAutoInsertTriggerText;
	private Label fAutoInsertDelayLabel;
	private Label fAutoInsertTriggerLabel;
	
	protected OverlayPreferenceStore createOverlayStore() {
		
		ArrayList overlayKeys= new ArrayList();
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS));
	
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}
	
	private Control createContentAssistPage(Composite parent) {
		Font font= parent.getFont();
		Composite contentAssistComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		contentAssistComposite.setLayout(layout);
		contentAssistComposite.setFont(font);

		String text= AntPreferencesMessages.AntCodeAssistPreferencePage_Insert;
		addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT, 0);

		text= AntPreferencesMessages.AntCodeAssistPreferencePage_0;
		addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS, 0);
		
		text= AntPreferencesMessages.AntCodeAssistPreferencePage__Enable_auto_activation_2;
		final Button autoactivation= addCheckBox(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION, 0);
		autoactivation.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				updateAutoactivationControls();
			}
		});
		
		Control[] labelledTextField;
		text= AntPreferencesMessages.AntCodeAssistPreferencePage_Auto_activation__delay__3;
		String[] errorMessages= new String[]{AntPreferencesMessages.AntCodeAssistPreferencePage_empty_input_auto_activation, AntPreferencesMessages.AntCodeAssistPreferencePage_invalid_input_auto_activation};
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY, 4, 20, errorMessages);
		fAutoInsertDelayLabel= getLabelControl(labelledTextField);
		fAutoInsertDelayText= getTextControl(labelledTextField);
		
		text= AntPreferencesMessages.AntCodeAssistPreferencePage_Auto_activation_tri_ggers__4;
		labelledTextField= addLabelledTextField(contentAssistComposite, text, AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS, 4, 20, null);
		fAutoInsertTriggerLabel= getLabelControl(labelledTextField);
		fAutoInsertTriggerText= getTextControl(labelledTextField);
		updateAutoactivationControls();
		return contentAssistComposite;
	}
	
	private void updateAutoactivationControls() {
	   boolean autoactivation= getOverlayStore().getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
	   fAutoInsertDelayText.setEnabled(autoactivation);
	   fAutoInsertDelayLabel.setEnabled(autoactivation);

	   fAutoInsertTriggerText.setEnabled(autoactivation);
	   fAutoInsertTriggerLabel.setEnabled(autoactivation);
   }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		getOverlayStore().load();
		getOverlayStore().start();
		
		Composite control= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		control.setLayout(layout);
		createContentAssistPage(control);
				
		initialize();
		
		applyDialogFont(control);
		//TODO set help context
		//WorkbenchHelp.setHelp(control, IAntUIHelpContextIds.ANT_CONTENTASSIST_PREFERENCE_PAGE);
		return control;
	}
	
	private void initialize() {
		initializeFields();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AbstractAntEditorPreferencePage#handleDefaults()
	 */
	protected void handleDefaults() {
		updateAutoactivationControls();
	}
}
