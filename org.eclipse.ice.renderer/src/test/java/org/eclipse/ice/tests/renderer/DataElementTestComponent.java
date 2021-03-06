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
package org.eclipse.ice.tests.renderer;

import org.eclipse.ice.renderer.DataElement;
import org.eclipse.ice.renderer.Renderer;
import org.eclipse.ice.renderer.RendererModule;

import dagger.Component;

/**
 * @author Jay Jay Billings
 *
 */
@Component(modules = {DataElementTestModule.class, RendererModule.class})
public interface DataElementTestComponent {

	public RendererRunner buildRendererRunner();
	
	public DataElement<?> buildDataElement();
	
	public Renderer<?,?> buildRenderer();
	
}
