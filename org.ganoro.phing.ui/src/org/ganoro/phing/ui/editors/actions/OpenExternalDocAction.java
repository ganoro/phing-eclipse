/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.ganoro.phing.ui.editors.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.IHandlerService;
import org.ganoro.phing.ui.AntUtil;
import org.ganoro.phing.ui.IAntUIPreferenceConstants;
import org.ganoro.phing.ui.PhingUi;
import org.ganoro.phing.ui.editors.PhingEditor;
import org.ganoro.phing.ui.internal.model.AntElementNode;
import org.ganoro.phing.ui.internal.model.AntModel;
import org.ganoro.phing.ui.internal.model.AntProjectNode;
import org.ganoro.phing.ui.internal.model.AntTargetNode;
import org.ganoro.phing.ui.internal.model.AntTaskNode;

/**
 * This action opens the selected tasks manual page in an external 
 * browser. 
 */
public class OpenExternalDocAction extends Action implements IEditorActionDelegate {
		
	private static final String COMMAND_ID = "org.ganoro.phing.ui.editors.actions.openExternalDoc"; //$NON-NLS-1$
    private PhingEditor fEditor;
	
	public OpenExternalDocAction() {
	}
	
	public OpenExternalDocAction(PhingEditor antEditor) {
		fEditor= antEditor;
		setActionDefinitionId(COMMAND_ID);
        IHandlerService handlerService= (IHandlerService) antEditor.getSite().getService(IHandlerService.class);
        handlerService.activateHandler(COMMAND_ID, new ActionHandler(this));
		setText(AntEditorActionMessages.getString("OpenExternalDocAction.1")); //$NON-NLS-1$
		setDescription(AntEditorActionMessages.getString("OpenExternalDocAction.2")); //$NON-NLS-1$
		setToolTipText(AntEditorActionMessages.getString("OpenExternalDocAction.2")); //$NON-NLS-1$
	}
	
    private Shell getShell() {
       return fEditor.getEditorSite().getShell();
    }
    
	private void doAction(AntElementNode node) {
		Shell shell= getShell();
		try {
			URL url= getExternalLocation(node);
			if (url != null) {
				AntUtil.openBrowser(url.toString(), shell, getTitle());
			} 		
		} catch (MalformedURLException e) {
           PhingUi.log(e);
        }
	}
	
	public URL getExternalLocation(AntElementNode node) throws MalformedURLException {
		URL baseLocation= getBaseLocation();
		if (baseLocation == null) {
			return null;
		}

		String urlString= baseLocation.toExternalForm();

		StringBuffer pathBuffer= new StringBuffer(urlString);
		if (!urlString.endsWith("/")) { //$NON-NLS-1$
			pathBuffer.append('/');
		}

		if (node instanceof AntProjectNode) {
			pathBuffer.append("using.html#projects"); //$NON-NLS-1$
		} else if (node instanceof AntTargetNode) {
			pathBuffer.append("using.html#targets"); //$NON-NLS-1$
		} else if (node instanceof AntTaskNode) {
			AntTaskNode taskNode= (AntTaskNode) node;
			if (fEditor.getAntModel().getDefininingTaskNode(taskNode.getTask().getTaskName()) == null) {
				//not a user defined task
				appendTaskPath(taskNode, pathBuffer);
			}
		} 

		try {
			return new URL(pathBuffer.toString());
		} catch (MalformedURLException e) {
			PhingUi.log(e);
		}
		return null;
	}

	private void appendTaskPath(AntTaskNode node, StringBuffer buffer) {
	    String taskName= node.getTask().getTaskName();
	    String taskPart= null;
	    if (taskName.equalsIgnoreCase("path")) {  //$NON-NLS-1$
	        buffer.append("using.html#path"); //$NON-NLS-1$
	        return;
	    } 
	    taskPart= getTaskTypePart(node);
        if (taskPart == null) {
            return;
        }
		buffer.append(taskPart);
		buffer.append('/');
		buffer.append(taskName);
		buffer.append(".html"); //$NON-NLS-1$	
	}

	private URL getBaseLocation() throws MalformedURLException {
		IPreferenceStore prefs = PhingUi.getDefault().getPreferenceStore();
		String base= prefs.getString(IAntUIPreferenceConstants.DOCUMENTATION_URL);
		return new URL(base);
	}
	
	private String getTitle() {
		return AntEditorActionMessages.getString("OpenExternalDocAction.0"); //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fEditor = (PhingEditor) targetEditor;
        if (fEditor != null) {
            IHandlerService handlerService= (IHandlerService) fEditor.getSite().getService(IHandlerService.class);
            handlerService.activateHandler(COMMAND_ID, new ActionHandler(this));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        ISelection selection= fEditor.getSelectionProvider().getSelection();
		AntElementNode node= null;
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)selection;
			int textOffset= textSelection.getOffset();
			AntModel model= fEditor.getAntModel();
			if (model != null) {
				node= model.getNode(textOffset, false);
			}
			if (node != null) {
				doAction(node);
			}
		}
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {        
    }
    
    
    private String getTaskTypePart(AntTaskNode node) {
		AntProjectNode projectNode= node.getProjectNode();
    	if (projectNode != null) {
    		Project antProject= projectNode.getProject();
    		AntTypeDefinition definition= ComponentHelper.getComponentHelper(antProject).getDefinition(node.getTask().getTaskName());
    		if (definition == null) {
    			return null;
    		}
    		String className= definition.getClassName();
    		if (className.indexOf("taskdef") != -1) { //$NON-NLS-1$
    		    if (className.indexOf("optional") != -1) { //$NON-NLS-1$
    		        return "OptionalTasks"; //$NON-NLS-1$
    		    } 
    		    return "CoreTasks"; //$NON-NLS-1$
    		} else if (className.indexOf("optional") != -1) { //$NON-NLS-1$
    		    return "OptionalTypes"; //$NON-NLS-1$
    		} else {
    		    return "CoreTypes"; //$NON-NLS-1$
    		}
    	}
    	
        return null;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		run(null);
	}
}
