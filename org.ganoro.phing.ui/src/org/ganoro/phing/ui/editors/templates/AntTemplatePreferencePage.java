/*******************************************************************************
 * Copyright (c) 2000, 2011  John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	   John-Mason P. Shackelford - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.ganoro.phing.ui.editors.templates;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.PhingUi;
import org.ganoro.phing.ui.editors.formatter.FormattingPreferences;
import org.ganoro.phing.ui.editors.formatter.XmlFormatter;
import org.ganoro.phing.ui.editors.text.AntDocumentSetupParticipant;
import org.ganoro.phing.ui.internal.preferences.AntEditorPreferenceConstants;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class AntTemplatePreferencePage extends TemplatePreferencePage {

	private FormattingPreferences fFormattingPreferences= new FormattingPreferences();
	
    public AntTemplatePreferencePage() {
        setPreferenceStore(PhingUi.getDefault().getPreferenceStore());
        setTemplateStore(AntTemplateAccess.getDefault().getTemplateStore());
        setContextTypeRegistry(AntTemplateAccess.getDefault().getContextTypeRegistry());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
    	  boolean ok = super.performOk();
    	  PhingUi.getDefault().savePluginPreferences();
    	  return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
     */
    protected SourceViewer createViewer(Composite parent) {
    	SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
          
		SourceViewerConfiguration configuration = new AntTemplateViewerConfiguration();        
		IDocument document = new Document();       
		new AntDocumentSetupParticipant().setup(document);
		viewer.configure(configuration);
		viewer.setDocument(document);
		viewer.setEditable(false);	
		Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);    
		        
		return viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
     */
    protected String getFormatterPreferenceKey() {
		return AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#updateViewerInput()
	 */
	protected void updateViewerInput() {
		IStructuredSelection selection= (IStructuredSelection) getTableViewer().getSelection();
		SourceViewer viewer= getViewer();
		
		if (selection.size() == 1 && selection.getFirstElement() instanceof TemplatePersistenceData) {
			TemplatePersistenceData data= (TemplatePersistenceData) selection.getFirstElement();
			Template template= data.getTemplate();
			if (PhingUi.getDefault().getPreferenceStore().getBoolean(getFormatterPreferenceKey())) {
				String formatted= XmlFormatter.format(template.getPattern(), fFormattingPreferences);
				viewer.getDocument().set(formatted);
			} else {
				viewer.getDocument().set(template.getPattern());
			}
		} else {
			viewer.getDocument().set(IPhingCoreConstants.EMPTY_STRING);
		}		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
	 */
	protected boolean isShowFormatterSetting() {
		return false;
	}
}