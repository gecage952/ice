/*******************************************************************************
 * Copyright (c) 2020- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Walsh - Initial implementation
 *    Daniel Bluhm - Modifications
 *******************************************************************************/

package org.eclipse.ice.dev.annotations.processors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

/**
 * Get list of generated file writers for DataElement annotations.
 *
 * @author Michael Walsh
 * @author Daniel Bluhm
 */
@AllArgsConstructor
public class DataElementWriterGenerator implements WriterGenerator {

	/**
	 * Logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(DataElementWriterGenerator.class);

	/**
	 * Data from which FileWriters are generated.
	 */
	private DataElementMetadata data;

	@Override
	public List<GeneratedFileWriter> generate() {
		List<GeneratedFileWriter> writers = new ArrayList<>();
		writers.add(new InterfaceWriter(data));
		writers.add(new ImplementationWriter(data));
		try {
			writers.add(new TypeScriptWriter(data));
		} catch (UnsupportedOperationException e) {
			logger.info("Failed to create typescript writer for element:", e);
		}

		return writers;
	}

}
