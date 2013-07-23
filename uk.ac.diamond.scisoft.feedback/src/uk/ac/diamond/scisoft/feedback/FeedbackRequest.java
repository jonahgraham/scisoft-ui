/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.feedback;

import java.io.File;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that submits a request to the DAWNFeedback Servlet
 */
public class FeedbackRequest {

	private static Logger logger = LoggerFactory.getLogger(FeedbackRequest.class);
	// this is the URL of the GAE servlet
	// public static final String SERVLET_URL = "http://localhost:8888/";
	public static final String SERVLET_URL = "http://dawnsci-feedback-test.appspot.com/";
	public static final String SERVLET_NAME = "dawnfeedback";
	// proxy
	private static String host;
	private static int port;

	/**
	 * Method used to submit a form data/file through HTTP to a GAE servlet
	 * 
	 * @param email
	 * @param to
	 * @param name
	 * @param subject
	 * @param messageBody
	 * @param logFile
	 * @param monitor
	 */
	public static IStatus doRequest(String email, String to, String name, String subject, String messageBody,
			File logFile, List<File> attachmentFiles, IProgressMonitor monitor) throws Exception {
		Status status = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		FeedbackProxy.init();
		host = FeedbackProxy.getHost();
		port = FeedbackProxy.getPort();

		if (monitor.isCanceled()) return Status.CANCEL_STATUS;

		// if there is a proxy
		if (host != null) {
			HttpHost proxy = new HttpHost(host, port);
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		if (monitor.isCanceled()) return Status.CANCEL_STATUS;

		try {
			HttpPost httpost = new HttpPost(SERVLET_URL + SERVLET_NAME);

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("name", new StringBody(name));
			entity.addPart("email", new StringBody(email));
			entity.addPart("to", new StringBody(to));
			entity.addPart("subject", new StringBody(subject));
			entity.addPart("message", new StringBody(messageBody));

			// Add the log file to the Multipart entity
			if (logFile != null && logFile.exists())
				entity.addPart("log.html", new FileBody(logFile));

			// add attachement files to the multipart entity
			for (int i = 0 ; i < attachmentFiles.size(); i++) {
				if (attachmentFiles.get(i) != null && attachmentFiles.get(i).exists())
					entity.addPart("attachment"+ i+".html", new FileBody(attachmentFiles.get(i)));
			}

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;

			httpost.setEntity(entity);

			// HttpPost post = new HttpPost("http://dawnsci-feedback.appspot.com/dawnfeedback?name=baha&email=baha@email.com&subject=thisisasubject&message=thisisthemessage");
			HttpResponse response = httpclient.execute(httpost);

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;

			final String reasonPhrase = response.getStatusLine().getReasonPhrase();
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				logger.debug("Status code 200");
				status = new Status(IStatus.OK, "Feedback successfully sent", "Thank you for your contribution");
			} else {
				logger.debug("Feedback email not sent - HTTP response: " + reasonPhrase);
				status = new Status(
						IStatus.WARNING,
						"Feedback not sent",
						"The response from the server is the following:\n"
								+ reasonPhrase
								+ "\nClick on OK to submit your feedback using the online feedback form available at http://dawnsci-feedback.appspot.com/");
			}
			logger.debug("HTTP Response: " + response.getStatusLine());
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return status;
	}
}
