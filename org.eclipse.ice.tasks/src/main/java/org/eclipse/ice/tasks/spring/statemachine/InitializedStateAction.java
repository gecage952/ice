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
package org.eclipse.ice.tasks.spring.statemachine;

import org.eclipse.ice.tasks.TaskState;
import org.eclipse.ice.tasks.TaskStateData;
import org.eclipse.ice.tasks.TaskTransitionEvents;
import org.springframework.statemachine.StateContext;

/**
 * This is a state machine action that puts the Task state into the INITIALIZED
 * state. The task executes this action *AFTER* the task enters the state.
 * 
 * @author Jay Jay Billings
 *
 */
public class InitializedStateAction<T> extends StateMachineBaseAction<T> {

	/**
	 * Constructor
	 * 
	 * @param taskStateData state data
	 */
	public InitializedStateAction(TaskStateData taskStateData) {
		super(taskStateData);
	}

	@Override
	public void execute(StateContext<TaskState, TaskTransitionEvents> context) {
		TaskState state = TaskState.INITIALIZED;
		stateData.get().setTaskState(state);
		logger.get().info("Task initialized. State = {}",state);
	}

}
