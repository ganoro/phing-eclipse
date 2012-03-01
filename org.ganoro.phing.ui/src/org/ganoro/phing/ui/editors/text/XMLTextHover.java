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
package org.ganoro.phing.ui.editors.text;


import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.editors.AntEditorSourceViewerConfiguration;
import org.ganoro.phing.ui.editors.PhingEditor;
import org.ganoro.phing.ui.internal.model.AntModel;


public class XMLTextHover implements ITextHover, ITextHoverExtension, IInformationProviderExtension2 {

	private PhingEditor fEditor;
	
	public XMLTextHover(PhingEditor editor) {
		super();
		fEditor = editor;
	}
	
	/*
	 * Formats a message as HTML text.
	 * Expects the message to already be properly escaped
	 */
	private String formatMessage(String message) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, message);
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatPathMessage(String[] list) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.XMLTextHover_4);
		HTMLPrinter.startBulletList(buffer);
		for (int i = 0; i < list.length; i++) {
			HTMLPrinter.addBullet(buffer, list[i]);
		}
		HTMLPrinter.endBulletList(buffer);
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		if (!(textViewer instanceof ISourceViewer)) {
			return null;
		}
		
		ISourceViewer sourceViewer= (ISourceViewer) textViewer;
		IAnnotationModel model= sourceViewer.getAnnotationModel();
		
		if (model != null) {
			String message= getAnnotationModelHoverMessage(model, hoverRegion);
			if (message != null) {
				return message;
			}
		}
        
		AntModel antModel= fEditor.getAntModel();
		if (antModel == null) { //the ant model has not been created yet
			return null;
		}
		
		return getAntModelHoverMessage(antModel, hoverRegion, textViewer);
		
	}
	
	private String getAntModelHoverMessage(AntModel antModel, IRegion hoverRegion, ITextViewer textViewer){
		return null;
	}
	
	private String getAnnotationModelHoverMessage(IAnnotationModel model, IRegion hoverRegion) {
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (a instanceof XMLProblemAnnotation) {
				Position p= model.getPosition(a);
				if (p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && msg.trim().length() > 0) {
						return formatMessage(msg);
					}
				}
			}
		}
		return null;
	}
	
	private String formatSetMessage(String[] includes, String[] excludes) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		if (includes != null && includes.length > 0) {
			HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.XMLTextHover_5);
			for (int i = 0; i < includes.length; i++) {
				HTMLPrinter.addBullet(buffer, includes[i]);
			}
		}
		HTMLPrinter.addParagraph(buffer, IPhingCoreConstants.EMPTY_STRING);
		HTMLPrinter.addParagraph(buffer, IPhingCoreConstants.EMPTY_STRING);
		if (excludes != null && excludes.length > 0) {
			HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.XMLTextHover_6);
			for (int i = 0; i < excludes.length; i++) {
				HTMLPrinter.addBullet(buffer, excludes[i]);
			}
		}
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (textViewer != null) {
			return getRegion(textViewer, offset);
		}
		return null;
	}

	public static IRegion getRegion(ITextViewer textViewer, int offset) {
		IDocument document= textViewer.getDocument();
		
		int start= -1;
		int end= -1;
	    IRegion region= null;
		try {
			int pos= offset;
			char c;
            
            if (document.getChar(pos) == '"') {
                pos--;
            }
			while (pos >= 0) {
				c= document.getChar(pos);
				if (c != '.' && c != '-' && c != '/' &&  c != '\\' && c != ' ' && c != ')' && c != '('&& c != ':' && !Character.isJavaIdentifierPart(c) && pos != offset)
					break;
				--pos;
			}
			
			start= pos;
			
			pos= offset;
			int length= document.getLength();
			
			while (pos < length) {
				c= document.getChar(pos);
				if (c != '.' && c != '-' && c != '/' &&  c != '\\' && c != ' ' && c != ')' && c != '('&& c != ':' && !Character.isJavaIdentifierPart(c))
					break;
                if (c == '/' && (document.getLength() - 1) > (pos + 1) && document.getChar(pos + 1) == '>') {
                   //e.g. <name/>
                    break;
                }
				++pos;
			}
			
			end= pos;
			
		} catch (BadLocationException x) {
		}
		
		if (start > -1 && end > -1) {
			if (start == offset && end == offset) {
				return new Region(offset, 0);
			} else if (start == offset) {
				return new Region(start, end - start);
			} else {
                try { //correct for spaces at beginning or end
                    while(document.getChar(start + 1) == ' ') {
                        start++;
                    }
                    while(document.getChar(end - 1) == ' ') {
                        end--;
                    }
                } catch (BadLocationException e) {
                }
                region= new Region(start + 1, end - start - 1);
            }
        }
        
        if (region != null) {
            try {
                char c= document.getChar(region.getOffset() - 1);
				if (c == '"') {
					if (document.get(offset, region.getLength()).indexOf(',') != -1) {
						region = cleanRegionForNonProperty(offset, document, region);
					}
				} else if (c != '{') {
                	region = cleanRegionForNonProperty(offset, document, region);
                }
            } catch (BadLocationException e) {
            }
        }
            
		return region;
	}

	private static IRegion cleanRegionForNonProperty(int offset, IDocument document, IRegion region) throws BadLocationException {
		//do not allow spaces in region that is not a property
		IRegion r = region;
		String text= document.get(r.getOffset(), r.getLength());
		if (text.startsWith("/")) { //$NON-NLS-1$
			text= text.substring(1);
			r= new Region(r.getOffset() + 1, r.getLength() - 1);
		}
		StringTokenizer tokenizer= new StringTokenizer(text, " "); //$NON-NLS-1$
		if (tokenizer.countTokens() != 1) {
		    while(tokenizer.hasMoreTokens()) {
		        String token= tokenizer.nextToken();
		        int index= text.indexOf(token);
		        if (r.getOffset() + index <= offset && r.getOffset() + index + token.length() >= offset) {
		            r= new Region(r.getOffset() + index, token.length());
		            break;
		        }
		    }
		}
		
		return r;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}
    
	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.3
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return AntEditorSourceViewerConfiguration.getInformationPresenterControlCreator();
	}
}