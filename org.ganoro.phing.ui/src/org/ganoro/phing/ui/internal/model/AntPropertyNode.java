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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.ganoro.phing.core.AntSecurityException;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.IPhingUIConstants;
import org.ganoro.phing.ui.editors.AntEditorCompletionProcessor;
import org.ganoro.phing.ui.internal.preferences.AntEditorPreferenceConstants;
import org.xml.sax.Attributes;

public class AntPropertyNode extends AntTaskNode {
	
	private String fValue= null;
	private String fReferencedName;
    private String fOccurrencesStartingPoint= IPhingCoreConstants.VALUE;
    private String fOccurrencesIdentifier;
    
	/*
	 * The set of properties defined by this node
	 * name-> value mapping
	 */
	private Map fProperties= null;
	
	public AntPropertyNode(Task task, Attributes attributes) {
		super(task);
		 String label = attributes.getValue(IPhingCoreConstants.NAME);
         if (label == null) {
			label = attributes.getValue(IPhingCoreConstants.FILE);
         	if (label != null) {
         		fReferencedName= label;
         		label=  "file="+label; //$NON-NLS-1$
         	} else {	
         		label =  attributes.getValue(IAntModelConstants.ATTR_RESOURCE);
         		if (label != null) {
         			fReferencedName= label;
         			label= "resource="+label; //$NON-NLS-1$
         		} else {
         			label = attributes.getValue(IAntModelConstants.ATTR_ENVIRONMENT);
         			if (label != null) {
         				label= "environment=" + label; //$NON-NLS-1$
         			} else {
         			    label = attributes.getValue("srcFile"); //$NON-NLS-1$
         			    if (label != null) {
         			        fReferencedName= label;
         			        label= "srcFile=" + label; //$NON-NLS-1$
         			    } 
                    }
         		}
         	}
         } else {
         	fValue= attributes.getValue(IPhingCoreConstants.VALUE);
            if (fValue == null) {
                fOccurrencesStartingPoint= IAntModelConstants.ATTR_LOCATION;
                fValue= attributes.getValue(fOccurrencesStartingPoint);
            }
         } 
         setBaseLabel(label);
	}
	
	public String getProperty(String propertyName) {
		if (fProperties != null) {
			return (String)fProperties.get(propertyName);
		} 
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IPhingUIConstants.IMG_PROPERTY);
	}
	
	/**
	 * Sets the properties in the project.
	 */
	public boolean configure(boolean validateFully) {
		if (fConfigured) {
			return false;
		}
		try {
			getProjectNode().setCurrentConfiguringProperty(this);
			getTask().maybeConfigure();
			getTask().execute();
			fConfigured= true;
		} catch (BuildException be) {
			handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_PROPERTIES);
		} catch (LinkageError le) { 
            //A classpath problem with the property task. Known cause: 
            //<property name= "hey" refId= "classFileSetId"/> where
            //classFileSetId refs a ClassFileSet which is an optional type that requires
            //BCEL JAR. Currently it is not possible to set these types of properties within the Ant Editor.
            //see bug 71888
            handleBuildException(new BuildException(AntModelMessages.AntPropertyNode_0), AntEditorPreferenceConstants.PROBLEM_PROPERTIES);
        } catch (AntSecurityException se) {
			//either a system exit or setting of system property was attempted
            handleBuildException(new BuildException(AntModelMessages.AntPropertyNode_1), AntEditorPreferenceConstants.PROBLEM_SECURITY);
        } finally {
			getProjectNode().setCurrentConfiguringProperty(null);
		}
		return false;
	}

	/**
	 * Adds this property name and value as being created by this property node declaration.
	 * @param propertyName the name of the property
	 * @param value the value of the property
	 */
	public void addProperty(String propertyName, String value) {
		if (fProperties == null) {
			fProperties= new HashMap(1);
		}
		fProperties.put(propertyName, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getReferencedElement(int)
	 */
	public String getReferencedElement(int offset) {
		if (fReferencedName != null) {
			String textToSearch= getAntModel().getText(getOffset(), offset - getOffset());
			if (textToSearch != null && textToSearch.length() != 0) {
				String attributeString = AntEditorCompletionProcessor.getAttributeStringFromDocumentStringToPrefix(textToSearch);
				if (IPhingCoreConstants.FILE.equals(attributeString) || IAntModelConstants.ATTR_RESOURCE.equals(attributeString) || "srcFile".equals(attributeString)) {  //$NON-NLS-1$
					return fReferencedName;
				}
			}
        }
        return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#containsOccurrence(java.lang.String)
	 */
	public boolean containsOccurrence(String identifier) {
		if (!getTask().getTaskName().equals("property")) { //$NON-NLS-1$
    		return super.containsOccurrence(identifier);
    	}

		if (fValue != null) {
			return fValue.indexOf(identifier) != -1;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getOccurrencesIdentifier()
	 */
	public String getOccurrencesIdentifier() {
		if (fOccurrencesIdentifier == null) {
			fOccurrencesIdentifier= new StringBuffer("${").append(fBaseLabel).append('}').toString(); //$NON-NLS-1$
		}
		return fOccurrencesIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#isRegionPotentialReference(org.eclipse.jface.text.IRegion)
	 */
	public boolean isRegionPotentialReference(IRegion region) {
		boolean superOK= super.isRegionPotentialReference(region);
		if (!getTask().getTaskName().equals("property") || !superOK) { //$NON-NLS-1$
    		return superOK;
    	}
		
		String textToSearch= getAntModel().getText(getOffset(), getLength());
		if (textToSearch == null) {
			return false;
		}
		int valueOffset= textToSearch.indexOf(fOccurrencesStartingPoint);
		if (valueOffset > -1) {
			valueOffset= textToSearch.indexOf('"', valueOffset);
			if (valueOffset > -1) {			
				boolean inValue= region.getOffset() >= (getOffset() + valueOffset);
				if (inValue) {
					if ("{".equals(getAntModel().getText(region.getOffset() - 1, 1)) || "}".equals(getAntModel().getText(region.getOffset() + region.getLength(), 1))) { //$NON-NLS-1$ //$NON-NLS-2$
						return true;
					}
					//reference is not in the value and not within a property de-reference
					return false;
				} 
				return true;
			}
		}
		return false;
	}
    
    public List computeIdentifierOffsets(String identifier) {
    	if (!getTask().getTaskName().equals("property")) { //$NON-NLS-1$
    		return super.computeIdentifierOffsets(identifier);
    	}
        String textToSearch= getAntModel().getText(getOffset(), getLength());
        if (textToSearch == null || textToSearch.length() == 0 || identifier.length() == 0) {
        	return null;
        }
        List results= new ArrayList();
        if (fBaseLabel != null) {
            if (fBaseLabel.equals(identifier)) {
                int nameOffset= textToSearch.indexOf(IPhingCoreConstants.NAME);
                nameOffset= textToSearch.indexOf(identifier, nameOffset + 1);
                results.add(new Integer(getOffset() + nameOffset));
            }
        }
        if (fValue != null) {
            int valueOffset= textToSearch.indexOf(fOccurrencesStartingPoint);
            int endOffset= getOffset() + getLength();
            while (valueOffset < endOffset) {
                valueOffset= textToSearch.indexOf(identifier, valueOffset);
                if (valueOffset == -1 || valueOffset > endOffset) {
                    break;
                }
                results.add(new Integer(getOffset() + valueOffset));
                valueOffset+= identifier.length();
            }
        }
        return results;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#isFromDeclaration(org.eclipse.jface.text.IRegion)
     */
    public boolean isFromDeclaration(IRegion region) {
    	if (fBaseLabel == null) {
    		return false;
    	}
    	if (fBaseLabel.length() != region.getLength()) {
    		return false;
    	}
    	int offset= getOffset();
    	 String textToSearch= getAntModel().getText(getOffset(), getLength());
         if (textToSearch == null || textToSearch.length() == 0) {
         	return false;
         }
         int nameStartOffset= textToSearch.indexOf(IPhingCoreConstants.NAME);
         nameStartOffset= textToSearch.indexOf("\"", nameStartOffset); //$NON-NLS-1$
         int nameEndOffset= textToSearch.indexOf("\"", nameStartOffset + 1); //$NON-NLS-1$
         nameEndOffset+=offset;
         nameStartOffset+=offset;
         return nameStartOffset <= region.getOffset() && region.getOffset() + region.getLength() <= nameEndOffset;
	}
}
