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
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * This class posts Command job updates to an HTTP link via POST
 * 
 * @author Joe Osborn
 *
 */
public class HTTPCommandUpdateHandler implements ICommandUpdateHandler {

	// HTTP address to post the update to
	private String HTTPAddress = "";

	// Message to be posted
	String message = "";

	/**
	 * Default constructor
	 */
	public HTTPCommandUpdateHandler() {
	}

	/**
	 * Setter for http address to post to
	 * 
	 * @param HTTPAddress
	 */
	public void setHTTPAddress(String HTTPAddress) {
		this.HTTPAddress = HTTPAddress;
	}

	/**
	 * See {@link org.eclipse.ice.commands.ICommandUpdateHandler#postUpdate()}
	 */
	@Override
	public void postUpdate() throws IOException {

		// Get a default httpClient to use to send the post
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(HTTPAddress);

		// Setup the parameters to be passed in the post
		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("body", message));
		httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		// Execute and get the response
		try {
			HttpResponse response = httpClient.execute(httpPost);
		} catch (Exception e) {
			logger.info("HTTP Post was not successful.", e);
			throw new IOException();
		}
	}

	/**
	 * See {@link org.eclipse.ice.commands.ICommandUpdateHandler#setMessage(String)}
	 */
	@Override
	public void setMessage(String message) {
		this.message = message;
	}

}
