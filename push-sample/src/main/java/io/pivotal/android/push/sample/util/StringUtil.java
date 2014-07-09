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
		Iterator<?> iter = collection.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	/**
	 * Appends the string `append` to `string` separated by the `delimiter` string.
	 * 
	 * @param string
	 *            the string in front
	 * @param append
	 *            the string to append
	 * @param delimiter
	 *            the string that separates `string` and `append`
	 * @return appended string of `string` + `delimiter` + `append`, or `null` if `string` if `string` is `null`.
	 */
	public static String append(final String string, final String append, final String delimiter) {
		if (string == null) {
			return append;
		} else {
			final StringBuilder builder = new StringBuilder(string);
			if (delimiter != null)
				builder.append(delimiter);
			if (append != null)
				builder.append(append);
			return builder.toString();
		}
	}
}
