/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.internal.model;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.ganoro.phing.core.AntSecurityException;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.AntUtil;
import org.ganoro.phing.ui.IPhingUIConstants;
import org.ganoro.phing.ui.editors.AntEditorCompletionProcessor;
import org.ganoro.phing.ui.internal.preferences.AntEditorPreferenceConstants;
import org.xml.sax.Attributes;

public class AntImportNode extends AntTaskNode {
	
	private String fFile= null;
	
	public AntImportNode(Task task, Attributes attributes) {
		super(task);
         fFile= attributes.getValue(IPhingCoreConstants.FILE);
	}
	
	public String getFile() {
		return fFile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IPhingUIConstants.IMG_ANT_IMPORT);
	}
	
	public String getLabel() {
	    if (fLabel == null) {
	        StringBuffer label= new StringBuffer(getTask().getTaskName());
	        label.append(' ');
	        label.append(fFile);
	        
	        if (isExternal()) {
	            appendEntityName(label);
	        }
	        fLabel= label.toString();
	    }
	    return fLabel;
	}

	/**
	 * Execute the import.
	 * Returns <code>true</code> as the import adds to the Ant model
	 */
	public boolean configure(boolean validateFully) {
		if (fConfigured) {
			return false;
		}
		try {
			getTask().maybeConfigure();
			getTask().execute();
			fConfigured= true;
			return true;
		} catch (BuildException be) {
			handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_IMPORTS);
		} catch (AntSecurityException se) {
			//either a system exit or setting of system property was attempted
            handleBuildException(new BuildException(AntModelMessages.AntImportNode_0), AntEditorPreferenceConstants.PROBLEM_SECURITY);
		}
		return false;
	}
	
	public IFile getIFile() {
		IFile file;
		if (isExternal()) {
			file= AntUtil.getFileForLocation(getFilePath(), null);
		} else {
			String path= getFile();
			file= AntUtil.getFileForLocation(path, getAntModel().getEditedFile().getParentFile());
		}
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getReferencedElement(int)
	 */
	public String getReferencedElement(int offset) {
		if (fFile != null) {
			String textToSearch= getAntModel().getText(getOffset(), offset - getOffset());
			if (textToSearch != null && textToSearch.length() != 0) {
				String attributeString = AntEditorCompletionProcessor.getAttributeStringFromDocumentStringToPrefix(textToSearch);
				if (IPhingCoreConstants.FILE.equals(attributeString)) {
					return fFile;
				}
			}
        }
        return null;
	}
}
