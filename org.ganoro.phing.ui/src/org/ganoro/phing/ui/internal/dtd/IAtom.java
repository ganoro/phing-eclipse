/*******************************************************************************
 * Copyright (c) 2002, 2005 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.ganoro.phing.ui.internal.dtd;

/**
 * Schema atom interface.
 * @author Bob Foster
 */
public interface IAtom {

	public static final int ELEMENT = 0;
	public static final int ATTRIBUTE = 1;
	
	/**
	 * Return the atom name.
	 */
	public String getName();
	
	/**
	 * Return the atom name.
	 */
	public String toString();
}
