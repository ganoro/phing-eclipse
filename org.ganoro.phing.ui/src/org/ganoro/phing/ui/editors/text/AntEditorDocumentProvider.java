/*******************************************************************************
 * Copyright (c) 2002, 2007 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.ganoro.phing.ui.editors.text;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.ganoro.phing.ui.internal.model.AntModel;
import org.ganoro.phing.ui.internal.model.IProblemRequestor;
import org.ganoro.phing.ui.internal.model.LocationProvider;

public class AntEditorDocumentProvider extends TextFileDocumentProvider {

	private final AntDocumentSetupParticipant fAntDocumentSetupParticipant;

    /**
	 * Remembers a Ant document model for each element.
	 */
	protected class AntFileInfo extends FileInfo {
		
		public AntModel fAntModel;
		
		public AntFileInfo() {
		}
	}
	
	public AntEditorDocumentProvider() {
		IDocumentProvider provider= new TextFileDocumentProvider(new AntStorageDocumentProvider());
		setParentDocumentProvider(provider);
        fAntDocumentSetupParticipant = new AntDocumentSetupParticipant();
	}

    public AntModel getAntModel(Object element) {
	    FileInfo info= getFileInfo(element);
	    if (info instanceof AntFileInfo) {
		    AntFileInfo xmlInfo= (AntFileInfo) info;
		    return xmlInfo.fAntModel;
	    }
	    return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
     */
    protected IAnnotationModel createAnnotationModel(IFile file) {
		   return new AntAnnotationModel(file);
    }

    protected AntModel createAntModel(Object element, IDocument document, IAnnotationModel annotationModel) {
	    IProblemRequestor requestor= annotationModel instanceof IProblemRequestor ? (IProblemRequestor) annotationModel : null;
	    return new AntModel(document, requestor, new LocationProvider(element instanceof IEditorInput ? (IEditorInput) element : null));
    }
    
    /*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info= super.createFileInfo(element);
		if (!(info instanceof AntFileInfo)) {
			return null;
		}
	
		//This is a required workaround for the disconnect between workbench file associations
		//and content types based document setup and creation
		//This ensures that a workbench file association for the AntEditor will have a document
		//that is setup with the correct document setup participant since it was "missed" by the 
		//document setup extensions (bug 72598).
		IDocument document= info.fTextFileBuffer.getDocument();
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(AntDocumentSetupParticipant.ANT_PARTITIONING) == null)
			    fAntDocumentSetupParticipant.setup(document);
		}
		
		//Check if the annotation model has been set by the annotation model factory extension for Ant UI
		//and is an annotation model that was specified by the extension (all are IProblemRequestors).
		//If we do not have an annotation model or not a correct annotation model, defer to the annotation model
		//created from this document provider. The document provider is only queried for an annotation model for workspace files.
		//Therefore if the annotation model is still null we are dealing with an external file that is associated with
		//the Ant editor from a user preference setting.
		//In all cases the determined annotation model is set for the file info to be used in the editor. 
		AntFileInfo xmlInfo= (AntFileInfo) info;
		IAnnotationModel annotationModel= xmlInfo.fTextFileBuffer.getAnnotationModel();
		if (annotationModel instanceof IProblemRequestor) {
		    xmlInfo.fModel= annotationModel;
		} else {
		    annotationModel= xmlInfo.fModel;
		}
		
		if (annotationModel == null) {
            annotationModel= new AntExternalAnnotationModel();
            xmlInfo.fModel= annotationModel;       
        }
		 
		AntModel antModel= createAntModel(element, document, annotationModel);
		antModel.install();
		xmlInfo.fAntModel= antModel;
		
		return xmlInfo;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object, org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
     */
    protected void disposeFileInfo(Object element, FileInfo info) {
	    if (info instanceof AntFileInfo) {
		    AntFileInfo xmlInfo= (AntFileInfo) info;
		    if (xmlInfo.fAntModel != null) {
			    IDocument doc=  xmlInfo.fTextFileBuffer.getDocument();
			    Object lock= null;
			    if (doc instanceof ISynchronizable) {
			    	lock= ((ISynchronizable) doc).getLockObject();
			    } else {
			    	lock= xmlInfo.fAntModel;
			    }
                if (lock == null) {
                    xmlInfo.fAntModel.dispose();
                    xmlInfo.fAntModel= null;
                } else {
                    synchronized (lock) {           
                        xmlInfo.fAntModel.dispose();
                        xmlInfo.fAntModel= null;
                    }
                }
		    }
	    }
	    super.disposeFileInfo(element, info);	
    }
    
    /*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new AntFileInfo();
	}
}