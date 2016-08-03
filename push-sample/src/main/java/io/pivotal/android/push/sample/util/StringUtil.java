/*
 * Copyright (C) 2014 - 2016 Pivotal Software, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.sample.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Collection of utility methods to assist with String logic.
 */
public class StringUtil {

	/**
	 * Joins all items in the `collection` into a `string`, separated by the given `delimiter`.
	 * 
	 * @param collection
	 *            the collection of items
	 * @param delimiter
	 *            the delimiter to insert between each item
	 * @return the string representation of the collection of items
	 */
	public static String join(Collection<?> collection, String delimiter) {
		if (collection == null)
			return "";

		StringBuilder buffer = new StringBuilder();
		Iterator<?> i = collection.iterator();
		while (i.hasNext()) {
			buffer.append(i.next());
			if (i.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
}
