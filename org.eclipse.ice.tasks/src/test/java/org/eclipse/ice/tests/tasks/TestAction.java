/******************************************************************************
 * Copyright (c) 2020- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jay Jay Billings
 *****************************************************************************/
package org.eclipse.ice.tests.tasks;

import org.eclipse.ice.tasks.Action;
import org.eclipse.ice.tasks.ActionType;

/**
 * This is a test action used to test the Task class.
 * @author Jay Jay Billings
 *
 */
public class TestAction<T> implements Action<T> {

	/**
	 * @return a diagnostic action type since this is a test
	 */
	@Override
	public ActionType getType() {
		return ActionType.BASIC.DIAGNOSTIC;
	}

	@Override
	public boolean run(T data) {
		try {
			wait(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
