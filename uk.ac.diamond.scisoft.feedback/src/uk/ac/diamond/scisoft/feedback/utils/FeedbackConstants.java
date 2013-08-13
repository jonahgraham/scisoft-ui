/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.feedback.utils;

import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.feedback.Activator;

public class FeedbackConstants {

	public static final int MAX_SIZE = 10000 * 1024; // bytes
	public static final int MAX_TOTAL_SIZE = 10000 * 2048;
	public static final String DAWN_FEEDBACK = "[DAWN-FEEDBACK]";
	public static final String FROM_PREF = FeedbackConstants.class.getName() + ".emailAddress";
	public static final String SUBJ_PREF = FeedbackConstants.class.getName() + ".subject";

	public static final Image DELETE = Activator.getImageDescriptor("icons/delete_obj.png").createImage();
	public static final String MAIL_TO = "dawnjira@diamond.ac.uk";
	public static final String DAWN_MAILING_LIST = "DAWN@JISCMAIL.AC.UK";

	public static final String RECIPIENT_PROPERTY = "uk.ac.diamond.scisoft.feedback.recipient";

}
