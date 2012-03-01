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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.ganoro.phing.core.AntObject;
import org.ganoro.phing.core.IPhingCoreConstants;
import org.ganoro.phing.core.Property;
import org.ganoro.phing.core.Task;
import org.ganoro.phing.ui.AntUIImages;
import org.ganoro.phing.ui.IAntUIConstants;


/**
 * Label provider for type elements
 */
public class AntObjectLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider.
	 */
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex != 0) {
            return null;
        }
		if (element instanceof Property) {
			Property prop= (Property) element;
            if (prop.isDefault() && prop.isEclipseRuntimeRequired()) {
                return AntUIImages.getImage(IAntUIConstants.IMG_ANT_ECLIPSE_RUNTIME_OBJECT);
            } 
            return getPropertyImage();
		} else if (element instanceof AntObject){
            AntObject object= (AntObject) element;
            if (object.isDefault() && object.isEclipseRuntimeRequired()) {
                return AntUIImages.getImage(IAntUIConstants.IMG_ANT_ECLIPSE_RUNTIME_OBJECT);
            }
            if (element instanceof Task) {
                return getTaskImage();
            }
            return getTypeImage();
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}
	
	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Property) {
			return getPropertyText((Property) element, columnIndex);
		} else if (element instanceof AntObject) {	
			AntObject object = (AntObject) element;
             switch (columnIndex) {
                case 0:
                    return object.toString();
                case 1:
                    return object.getClassName();
                case 2:
                   return object.getLibraryEntry().getLabel();
                case 3:
                    return object.getPluginLabel();
            }
		}
		 
		return element.toString();	
	}
	
    public String getPropertyText(Property property, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return property.getName();
            case 1:
                return property.getValue(false);
            case 2:
                if (property.isDefault()) {
                    return property.getPluginLabel();
                }
        }
        return IPhingCoreConstants.EMPTY_STRING;
    }
    
	public static Image getTypeImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_ANT_TYPE);
	}
	
	public static Image getTaskImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
	}

	public static Image getPropertyImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_PROPERTY);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (element instanceof AntObject) {
			if (((AntObject) element).isDefault()) {
				Display display= Display.getCurrent();
				return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);		
			}
		} else if (element instanceof Property) {
			if (((Property) element).isDefault()) {
				Display display= Display.getCurrent();
				return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);		
			}
		}
		return null;
	}
}