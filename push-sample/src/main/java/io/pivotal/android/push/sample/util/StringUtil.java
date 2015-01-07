/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
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
