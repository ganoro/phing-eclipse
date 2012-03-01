/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.editors.templates;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.ganoro.phing.ui.internal.model.IAntModel;

public class AntContext extends DocumentTemplateContext {
	
	private IAntModel fAntModel;
	
	public AntContext(TemplateContextType type, IDocument document, IAntModel model, Position position) {
		super(type, document, position);
		fAntModel= model;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template))
			return null;

		TemplateBuffer templateBuffer= createTemplateBuffer(template);

		if (templateBuffer == null) {
			return null;
		}

		//TODO Not enabled see bug 55356
//		if (false && AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.TEMPLATES_USE_CODEFORMATTER)) {
//			FormattingPreferences prefs = new FormattingPreferences();
//			XmlFormatter.format(templateBuffer, this, prefs);
//		}
		return templateBuffer;
	}

	private TemplateBuffer createTemplateBuffer(Template template) throws BadLocationException, TemplateException {
		String lineDelimiter= TextUtilities.getDefaultLineDelimiter(getDocument());
		IDocument document= new Document(template.getPattern());
		convertLineDelimiters(document, lineDelimiter);

		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(document.get());

		getContextType().resolve(buffer, this);

		return buffer;
	}

	private static void convertLineDelimiters(IDocument document, String defaultLineDelimiter) throws BadLocationException {
		int lines= document.getNumberOfLines();
		for (int line= 0; line < lines; line++) {
			IRegion region= document.getLineInformation(line);
			String lineDelimiter= document.getLineDelimiter(line);
			if (lineDelimiter != null)
				document.replace(region.getOffset() + region.getLength(), lineDelimiter.length(), defaultLineDelimiter);
		}
	}

	/**
	 * @return Returns the AntModel.
	 */
	public IAntModel getAntModel() {
		return fAntModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#getEnd()
	 */
	public int getEnd() {
		int start= getCompletionOffset();
		int length= getCompletionLength();

		IDocument document= getDocument();
		if (start > 0 && document.get().charAt(start - 1) == '<' && document.getLength() > 1) {
			length++;
		}

		int end= getCompletionOffset() + length;

		try {
			while (start != end && Character.isWhitespace(document.getChar(end - 1)))
				end--;
		} catch (BadLocationException e) {
			// Return latest valid end
		}

		return end;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.DocumentTemplateContext#getStart()
	 */
	public int getStart() {
		int replacementOffset= getCompletionOffset();
		if (replacementOffset > 0 && getDocument().get().charAt(replacementOffset - 1) == '<') {
			replacementOffset--;
		}
		return replacementOffset;
	}
}
