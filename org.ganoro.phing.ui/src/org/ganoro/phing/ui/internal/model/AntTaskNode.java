/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ganoro.phing.ui.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jface.resource.ImageDescriptor;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.IPhingUIConstants;
import org.ganoro.phing.ui.internal.preferences.AntEditorPreferenceConstants;


public class AntTaskNode extends AntElementNode {

	private Task fTask= null;
	protected String fBaseLabel= null;
	protected String fLabel;
	private String fId= null;
	protected boolean fConfigured= false;
	
	public AntTaskNode(Task task) {
		super(task.getTaskName());
		fTask= task;
	}
	
	public AntTaskNode(Task task, String label) {
		super(task.getTaskName());
		fTask= task;
		fBaseLabel= label;
	}	
	
	public String getLabel() {
	    if (fLabel == null) {
			StringBuffer label= new StringBuffer();
			if (fBaseLabel != null) {
				label.append(fBaseLabel);
			} else if (fId != null) {
				label.append(fId);
			} else {
				label.append(fTask.getTaskName());
			}
			if (isExternal()) {
				appendEntityName(label);
			}
			fLabel= label.toString();
	    }
	    return fLabel;
	}
	
	public void setBaseLabel(String label) {
		fBaseLabel= label;
	}
	
	public Task getTask() {
		return fTask;
	}
	
	public void setTask(Task task) {
		fTask= task;
	}
	
	protected ImageDescriptor getBaseImageDescriptor() {
		if (fId != null) {
			return AntUIImages.getImageDescriptor(IPhingUIConstants.IMG_ANT_TYPE);
		}
		
		return super.getBaseImageDescriptor();
	}

	/**
	 * The reference id for this task
	 * @param id The reference id for this task
	 */
	public void setId(String id) {
		fId= id;
	}
	
	/**
	 * Returns the reference id for this task or <code>null</code>
	 * if it has no reference id.
	 * @return The reference id for this task
	 */
	public String getId() {
		return fId;
	}
	
	/**
	 * Configures the associated task if required.
	 * Allows subclasses to do specific configuration (such as executing the task) by
	 * calling <code>nodeSpecificConfigure</code>
	 * 
	 * @return whether the configuration of this node could have impact on other nodes
	 */
	public boolean configure(boolean validateFully) {
		if (getId() != null) {
			//ensure that references are set...new for Ant 1.7
			try {
				getProjectNode().getProject().getReference(getId());
			} catch (BuildException e) {
				handleBuildException(e, AntEditorPreferenceConstants.PROBLEM_TASKS);
			}
		} 
		if (!validateFully || (getParentNode() instanceof AntTaskNode)) {
			return false;
		}
		if (fConfigured) {
			return false;
		}
		int severity= AntModelProblem.getSeverity(AntEditorPreferenceConstants.PROBLEM_TASKS);
		if (severity != AntModelProblem.NO_PROBLEM) {
		    //only configure if the user cares about the problems
			getTask().maybeConfigure();
			fConfigured= true;
			return true;
		}
		return false;
	}

	protected void handleBuildException(BuildException be, String preferenceKey) {
		int severity= AntModelProblem.getSeverity(preferenceKey);
		if (severity != AntModelProblem.NO_PROBLEM) {
			getAntModel().handleBuildException(be, this, severity);
		}
	}
	
	public boolean containsOccurrence(String identifier) {
		return false;
	}

	public List computeIdentifierOffsets(String identifier) {
        String textToSearch= getAntModel().getText(getOffset(), getLength());
        if (textToSearch == null || textToSearch.length() == 0 || identifier.length() ==0) {
        	return null;
        }
        List results= new ArrayList();
        return results;
    }
}
