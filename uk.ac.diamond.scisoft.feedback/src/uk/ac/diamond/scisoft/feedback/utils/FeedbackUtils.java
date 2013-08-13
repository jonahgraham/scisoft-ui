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

/**
 * 
 */
public class FeedbackUtils {

	/**
	 * size unit
	 * @param value
	 * @return the string value with unit
	 */
	public static String getValueWithUnit(long value){
		if (((value / 1000) > 1) && ((value / 1000) < 1000))
			return String.valueOf(value / 1000) + "KB";
		else if ((value / 1000) > 1000)
			return String.valueOf(value / 1000000) + "MB";
		else
			return String.valueOf(value) + "B";
	}
}
