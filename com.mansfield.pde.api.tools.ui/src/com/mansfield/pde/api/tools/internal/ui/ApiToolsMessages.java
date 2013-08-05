/*******************************************************************************
 * Copyright (c) 2013 Joseph Carroll and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Joseph Carroll <jdsalingerjr@gmail.com> - initial API and implementation
 ******************************************************************************/
package com.mansfield.pde.api.tools.internal.ui;

import org.eclipse.osgi.util.NLS;

public class ApiToolsMessages extends NLS {

	private static final String BUNDLE_NAME = "com.mansfield.pde.api.tools.internal.ui.messages";
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ApiToolsMessages.class);
	}

	public static String Preferences_MainPage_Description;
	
}
