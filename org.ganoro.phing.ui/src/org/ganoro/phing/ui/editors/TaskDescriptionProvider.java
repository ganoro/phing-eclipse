/*******************************************************************************
 * Copyright (c) 2002, 2011 GEBIT Gesellschaft fuer EDV-Beratung
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

package org.ganoro.phing.ui.editors;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.ui.PhingUi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The <code>TaskDescriptionProvider</code> provides the additional descriptions
 * for tasks and attributes for the code assist.
 * <P>
 * Descriptions for task are originally provided with the XML file
 * <code>TASKS_DESCRIPTION_XML_FILE_NAME</code>. This file is parsed by the
 * provider and requested descriptions are returned.
 * <P>
 */
public class TaskDescriptionProvider {

    /**
     * The file that contains all task descriptions.
     */
    public static final String TASKS_DESCRIPTION_XML_FILE_NAME = "/org/eclipse/ant/internal/ui/editor/anttasks_1.6.0.xml"; //$NON-NLS-1$

    public static final String XML_TAG_TASKS = "tasks"; //$NON-NLS-1$
    public static final String XML_TAG_TASK = "task"; //$NON-NLS-1$
    public static final String XML_TAG_ELEMENTS = "elements"; //$NON-NLS-1$
    public static final String XML_TAG_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    public static final String XML_TAG_ATTRIBUTES = "attributes"; //$NON-NLS-1$
    public static final String XML_TAG_ELEMENT = "element"; //$NON-NLS-1$
    public static final String XML_TAG_STRUCTURE = "structure"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_REQUIRED = "required"; //$NON-NLS-1$

    /**
     * Class to avoid holding on to DOM element handles
     * @since 3.5
     */
    class ProposalNode {
    	String desc = null;
    	String required = null;
    	HashMap nodes = null;
    	
    	ProposalNode(String desc, String required) {
    		this.desc = desc;
    		this.required = required;
    	}
    	
    	void addChild(String name, ProposalNode node) {
    		if(nodes == null) {
    			nodes = new HashMap(9);
    		}
    		nodes.put(name, node);
    	}
    	
    	ProposalNode getChild(String name) {
    		if(nodes != null) {
    			return (ProposalNode) nodes.get(name);
    		}
    		return null;
    	}
    }
    
    private static TaskDescriptionProvider fgDefault;

    /**
     * Mapping of {@link String} to {@link ProposalNode}
     * <br><br>
     * <code>Map&lt;String, ProposalNode&gt;</code>
     */
    private Map taskNodes = null;
    
    /**
     * Meant to be a singleton
     */
    private TaskDescriptionProvider() {
    }
    
    public static TaskDescriptionProvider getDefault() {
    	if (fgDefault == null) {
    		fgDefault= new TaskDescriptionProvider();
    		IRunnableWithProgress runnable= new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {					
					fgDefault.initialize();
				}
			};
			
			IProgressService service= PlatformUI.getWorkbench().getProgressService();
			try {
				service.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
    	}
    	return fgDefault;
    }

    /**
     * Parses the task description XML file and stores the information.
     */
    protected void initialize() {
    	taskNodes = new HashMap();
        Document doc = parseFile(TASKS_DESCRIPTION_XML_FILE_NAME);
        Node root = doc.getDocumentElement();
        NodeList tasks = root.getChildNodes();
        Node node = null;
        for(int i=0; i < tasks.getLength(); i++) {
            node = tasks.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                if(XML_TAG_TASK.equals(node.getNodeName())) {
                	Element task = (Element) node;
                	String name = task.getAttribute(IPhingCoreConstants.NAME);
                    if(name != null) {
                    	ProposalNode tasknode = new ProposalNode(getDescription(task), null);
                    	taskNodes.put(name, tasknode);
                    	NodeList nodes = task.getElementsByTagName(XML_TAG_ATTRIBUTE);
                    	Element e = null;
                    	for (int j = 0; j < nodes.getLength(); j++) {
							e = (Element) nodes.item(j);
							addNode(e, tasknode);
						}
                    	nodes = task.getElementsByTagName(XML_TAG_ELEMENT);
                    	for (int j = 0; j < nodes.getLength(); j++) {
							e = (Element) nodes.item(j);
							addNode(e, tasknode);
						}
                    }
                }
            }
        }
    }
    
    /**
     * Adds a new child {@link ProposalNode} to the given parent node
     * 
     * @param element
     * @param node
     * @since 3.5
     */
    void addNode(Element element, ProposalNode node) {
    	String name = element.getAttribute(IPhingCoreConstants.NAME);
    	if(name != null) {
    		node.addChild(name, new ProposalNode(getDescription(element), element.getAttribute(XML_ATTRIBUTE_REQUIRED)));
    	}
    }
    
    /**
     * Recursively find the description text for the parent {@link Element} 
     * @param element
     * @return the description element text or <code>null</code>
     * @since 3.5
     */
    String getDescription(Element element) {
    	NodeList nodes = element.getChildNodes();
    	for (int i = 0; i < nodes.getLength(); i++) {
    		Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE && IPhingCoreConstants.DESCRIPTION.equals(node.getNodeName())) {
				node = node.getFirstChild();
				if(node != null) {
					return node.getNodeValue();
				}
			}
		}
    	return null;
    }
    
    /**
     * Returns the (DOM) document as a result of parsing the file with the 
     * specified file name.
     * <P>
     * The file will be loaded as resource, thus must begin with '/' and must
     * be relative to the classpath.
     */
    private Document parseFile(String aFileName) {
        Document tempDocument = null;

        DocumentBuilderFactory tempFactory = DocumentBuilderFactory.newInstance();
        tempFactory.setIgnoringComments(true);
        tempFactory.setIgnoringElementContentWhitespace(true);
        tempFactory.setCoalescing(true);

        try {
            DocumentBuilder tempDocBuilder = tempFactory.newDocumentBuilder();
            tempDocBuilder.setErrorHandler(new DefaultHandler());
            URL tempURL = getClass().getResource(aFileName);
            InputSource tempInputSource = new InputSource(tempURL.toExternalForm());
            tempDocument = tempDocBuilder.parse(tempInputSource);
        } catch (ParserConfigurationException e) {
			PhingUi.log(e);
        }
        catch (IOException ioException) {
			PhingUi.log(ioException);
        }
        catch (SAXException saxException) {
			PhingUi.log(saxException);
        }

        return tempDocument;
    }

    /**
     * Returns the description string for the specified task.
     * 
     * @return description string or <code>null</code> if task not known or
     * no description available.
     */
    public String getDescriptionForTask(String aTaskName) {
    	ProposalNode task = (ProposalNode) taskNodes.get(aTaskName);
    	if(task != null) {
    		return task.desc;
    	}
        return null;
    }

    /**
     * Returns the description string for the specified attribute of the 
     * specified task.
     * 
     * @return description string or <code>null</code> if task or attribute 
     * not known or no description available.
     */
    public String getDescriptionForTaskAttribute(String aTaskName, String anAttributeName) {
        ProposalNode task = (ProposalNode) taskNodes.get(aTaskName);
        if(task != null) {
        	ProposalNode att = task.getChild(anAttributeName);
        	if(att != null) {
        		return att.desc;
        	}
        }
        return null;
    }
    
	/**
     * Returns the required string value for the specified attribute of the
     * specified task.
     * 
     * @return required string or <code>null</code> if task or attribute not 
     * known or no description available.
     */
    public String getRequiredAttributeForTaskAttribute(String aTaskName, String anAttributeName) {
    	ProposalNode task = (ProposalNode) taskNodes.get(aTaskName);
        if(task != null) {
        	ProposalNode att = task.getChild(anAttributeName);
        	if(att != null) {
        		return att.required;
        	}
        }
        return null;
    }
    
    /**
     * Returns the {@link ProposalNode} for the given task name or <code>null</code> if one does not exist
     * @param aTaskName
     * @return the {@link ProposalNode} for the given name or <code>null</code>
     * @since 3.5
     */
    ProposalNode getTaskNode(String aTaskName) {
    	return (ProposalNode) taskNodes.get(aTaskName);
    }
    
    protected static void reset() {
    	if(fgDefault != null && fgDefault.taskNodes != null) {
    		fgDefault.taskNodes.clear();
    	}
    	fgDefault= null;
    }
}