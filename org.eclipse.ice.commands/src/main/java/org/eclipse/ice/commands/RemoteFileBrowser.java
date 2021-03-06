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
package org.eclipse.ice.commands;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.client.subsystem.sftp.SftpClient.DirEntry;

/**
 * This class allows for remote file and directory browsing on a remote host
 * 
 * @author Joe Osborn
 *
 */
public class RemoteFileBrowser implements FileBrowser {
	/**
	 * Default constructor
	 */
	public RemoteFileBrowser() {
		// Clear the arrays to start fresh for every browser
		fileList.clear();
		directoryList.clear();
	}

	/**
	 * Default constructor with connection and top directory name
	 * 
	 * @param connection   - Connection over which to file browse
	 * @param topDirectory - top most directory to recursively walk through
	 */
	public RemoteFileBrowser(Connection connection, final String topDirectory) {
		// Make sure we start with a fresh list every time the browser is called
		fileList.clear();
		directoryList.clear();

		// Fill the arrays with the relevant file information
		fillArrays(topDirectory, connection.getSftpChannel());
	}

	/**
	 * This function uses the ChannelSftp to look in the directory structure on the
	 * remote host and fill the member variable arrays with the files and
	 * directories
	 * 
	 * @param channel      - sftp channel to walk the file tree with
	 * @param topDirectory - top most directory under which to browse
	 */
	protected void fillArrays(String topDirectory, SftpClient channel) {
		try {
			// Make sure the top directory ends with the appropriate separator
			String separator = "/";
			// If the remote directory to search contains a \\, then it must be windows
			// and thus we should set the separator as such
			if (topDirectory.contains("\\"))
				separator = "\\";

			// Now check the path name
			if (!topDirectory.endsWith(separator))
				topDirectory += separator;

			// Iterate through the structure
			for (DirEntry file : channel.readDir(topDirectory)) {
				if (!file.getAttributes().isDirectory()) {
					fileList.add(topDirectory + file.getFilename());
					// Else if it is a subdirectory and not '.' or '..'
				} else if (!(".".equals(file.getFilename()) || "..".equals(file.getFilename()))) {
					// Then add it to the directory list
					directoryList.add(topDirectory + file.getFilename());
					// Recursively iterate over this subdirectory to get its contents
					fillArrays(topDirectory + file.getFilename(), channel);
				}

			}

		} catch (IOException e) {
			logger.error("Could not use channel to connect to browse directories!", e);
		}

	}

	/**
	 * See {@link org.eclipse.ice.commands.FileBrowser#getDirectoryList()}
	 */
	@Override
	public ArrayList<String> getDirectoryList() {
		return directoryList;
	}

	/**
	 * See {@link org.eclipse.ice.commands.FileBrowser#getFileList()}
	 */
	@Override
	public ArrayList<String> getFileList() {
		return fileList;
	}

}
