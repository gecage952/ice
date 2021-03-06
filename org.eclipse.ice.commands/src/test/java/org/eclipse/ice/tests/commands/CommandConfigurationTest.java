/*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jay Jay Billings, Joe Osborn
 *******************************************************************************/
package org.eclipse.ice.tests.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.eclipse.ice.commands.CommandConfiguration;
import org.junit.Test;

/**
 * This class tests {@link org.eclipse.ice.commands.CommandConfiguration}.
 * 
 * @author Joe Osborn
 *
 */
public class CommandConfigurationTest {

	// Create a default instance to test
	CommandConfiguration config = new CommandConfiguration();

	/**
	 * Test method for
	 * {@link org.eclipse.ice.commands.CommandConfiguration#CommandConfiguration()}.
	 * Check some of the getters and setters
	 */
	@Test
	public void testCommandConfiguration() {

		// Set some things
		config.setCommandId(3);
		config.setExecutable("./some_executable.sh");
		config.setErrFileName("errorFile.txt");
		config.setOutFileName("outFile.txt");

		// Assert whether or not things are/aren't set
		assertNotNull(config.getOutFileName());
		assertNotNull(config.getOS());

		// Didn't set working directory
		assertNull(config.getWorkingDirectory());

		// Assert that the default local OS is set
		assertEquals(System.getProperty("os.name"), config.getOS());

		config.setOS("JoeOsbornOS");

		assertEquals("JoeOsbornOS", config.getOS());

	}

	/**
	 * Test method for
	 * {@link org.eclipse.ice.commands.CommandConfiguration#getExecutableName()}
	 */
	@Test
	public void testGetExecutableName() {

		// Test that if one wants to append inputfile, it is appended
		config.setCommandId(1);
		config.setInterpreter("bash");
		config.setExecutable("./test_code_execution.sh");
		config.addInputFile("someInputFile", "someInputFile.txt");
		config.setNumProcs("1");
		config.setAppendInput(true);
		config.setOS(System.getProperty("os.name"));
		String executable = config.getExecutableName();
		assertEquals("bash ./test_code_execution.sh someInputFile.txt", executable);

		// Test that if num processes is more than 1, mpi options are added
		config.setNumProcs("4"); // arbitrary number
		// We can test append input as well when it is false
		config.setAppendInput(false);
		executable = config.getExecutableName();
		assertEquals("mpirun -np 4 bash ./test_code_execution.sh", executable);

	}

	/**
	 * This function tests a command where one wants to add input file(s) and
	 * argument(s)
	 */
	@Test
	public void testArgumentAndInputFileConfiguration() {

		CommandConfiguration configuration = new CommandConfiguration();
		configuration.setCommandId(5);
		configuration.setInterpreter("python");
		configuration.setExecutable("random_python_script.py");
		configuration.addInputFile("someInputFile", "someInputFile.txt");
		configuration.setNumProcs("1");
		configuration.setAppendInput(true);
		configuration.addArgument("some_arg");
		configuration.addArgument("some_other_arg");

		String executable = configuration.getExecutableName();

		assertEquals("python random_python_script.py some_arg some_other_arg someInputFile.txt", executable);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ice.commands.CommandConfiguration#getExecutableName()} and
	 * for testing the split command functionality
	 */
	@Test
	public void testGetExecutableNameSplitCommand() {
		CommandConfiguration splitConfig = new CommandConfiguration();
		splitConfig.setCommandId(2);
		splitConfig.setExecutable(
				"./dummy.sh ${inputfile}; ./next_file.sh ${otherinputfile}; ./other_file.sh ${installDir}");
		// Test if the user falsifies append input whether or not the environment
		// variable is replaced
		splitConfig.setAppendInput(false);
		splitConfig.setNumProcs("1");
		splitConfig.addInputFile("inputfile", "inputfile.txt");
		splitConfig.addInputFile("otherinputfile", "/some/dummy/path/to/an/inputfile.txt");
		splitConfig.setInstallDirectory("~/install_dir");
		splitConfig.setOS(System.getProperty("os.name"));
		String executable = splitConfig.getExecutableName();
		assertEquals(
				"./dummy.sh inputfile.txt; ./next_file.sh /some/dummy/path/to/an/inputfile.txt; ./other_file.sh ~/install_dir/",
				executable);

		ArrayList<String> split = new ArrayList<String>();
		split = splitConfig.getSplitCommand();

		// Create an array list to check the split command against
		ArrayList<String> checkSplit = new ArrayList<String>();
		checkSplit.add("./dummy.sh inputfile.txt");
		checkSplit.add("./next_file.sh /some/dummy/path/to/an/inputfile.txt");
		checkSplit.add("./other_file.sh ~/install_dir/");

		for (int i = 0; i < split.size(); i++) {
			assertEquals(checkSplit.get(i), split.get(i));
		}

	}

}
