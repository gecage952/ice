/*******************************************************************************
 * Copyright (c) 2020- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jay Jay Billings
 *******************************************************************************/
package org.eclipse.ice.renderer;

import dagger.Module;
import dagger.Provides;

/**
 * A Dagger Module binding for the renderer package.
 * @author Jay Jay Billings
 *
 */
@Module
public class RendererModule {

	@Provides
	public DataElement<?> getDataElement() {
		return new DataElement();
	}
	
	@Provides Renderer<?,?> getRenderer() {
		return new Renderer();
	}
	
}
