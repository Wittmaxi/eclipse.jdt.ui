/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.template.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.templates.Template;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * <code>CodeTemplates</code> gives access to the available code templates.
 * @since 3.0
 * @deprecated use {@link org.eclipse.jdt.internal.ui.JavaPlugin#getCodeTemplateStore()} instead 
 */
public class CodeTemplates extends org.eclipse.jdt.internal.corext.template.java.TemplateSet {

	private static final String DEFAULT_FILE= "default-codetemplates.xml"; //$NON-NLS-1$
	private static final String TEMPLATE_FILE= "codetemplates.xml"; //$NON-NLS-1$
	private static final ResourceBundle fgResourceBundle= ResourceBundle.getBundle(JavaTemplateMessages.class.getName());

	/** Singleton. */
	private static CodeTemplates fgTemplates;

	public static Template getCodeTemplate(String name) {
		return getInstance().getFirstTemplate(name);
	}

	/**
	 * Returns an instance of templates.
	 */
	public static CodeTemplates getInstance() {
		if (fgTemplates == null)
			fgTemplates= new CodeTemplates();
		
		return fgTemplates;
	}
	
	private CodeTemplates() {
		super("codetemplate", JavaPlugin.getDefault().getCodeTemplateContextRegistry()); //$NON-NLS-1$
		create();
	}
	
	private void create() {
		
		try {
//			addFromStream(getDefaultsAsStream(), false, true, fgResourceBundle);
			File templateFile= getTemplateFile();
			if (templateFile.exists()) {
				addFromFile(templateFile, false, fgResourceBundle);
			}
//			saveToFile(templateFile);

		} catch (CoreException e) {
			JavaPlugin.log(e);
			clear();
		}

	}	
	
	/**
	 * Resets the template set.
	 */
	public void reset() throws CoreException {
		clear();
		addFromFile(getTemplateFile(), false, fgResourceBundle);
	}

	/**
	 * Resets the template set with the default templates.
	 */
	public void restoreDefaults() throws CoreException {
		clear();
		InputStream stream= getDefaultsAsStream();
		try {
			addFromStream(stream, false, true, fgResourceBundle);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException x) {
			}
		}
	}

	/**
	 * Saves the template set.
	 */
	public void save() throws CoreException {					
		saveToFile(getTemplateFile());
	}

	private static InputStream getDefaultsAsStream() {
		return CodeTemplates.class.getResourceAsStream(DEFAULT_FILE);
	}

	private static File getTemplateFile() {
		IPath path= JavaPlugin.getDefault().getStateLocation();
		path= path.append(TEMPLATE_FILE);
		
		return path.toFile();
	}

}
