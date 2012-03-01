/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
package org.ganoro.phing.ui.editors.text;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * The document setup participant for Ant.
 */
public class AntDocumentSetupParticipant  implements IDocumentSetupParticipant {

	/**
	 * The name of the Ant partitioning.
	 * @since 3.0
	 */
	public final static String ANT_PARTITIONING= "org.ganoro.phing.ui.antPartitioning";  //$NON-NLS-1$
	
	public AntDocumentSetupParticipant() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			IDocumentPartitioner partitioner = createDocumentPartitioner();
			extension3.setDocumentPartitioner(ANT_PARTITIONING, partitioner);
			partitioner.connect(document);
		} 
	}
	
	private IDocumentPartitioner createDocumentPartitioner() {
		return new FastPartitioner(
				new AntEditorPartitionScanner(), new String[]{
						AntEditorPartitionScanner.XML_TAG,
						AntEditorPartitionScanner.XML_COMMENT, 
						AntEditorPartitionScanner.XML_CDATA,
						AntEditorPartitionScanner.XML_DTD});
	}
}
