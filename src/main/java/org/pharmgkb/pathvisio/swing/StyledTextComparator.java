/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.swing;

import java.util.Comparator;

/**
 * Strips <i> tags from text before comparing.
 *
 * @author Mark Woon
 */
public class StyledTextComparator implements Comparator<String> {
	private static final StyledTextComparator sf_comparator = new StyledTextComparator();


	public static StyledTextComparator getInstance() {
		return sf_comparator;
	}


	public int compare(String o1, String o2) {

		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (o2 == null) {
			return 1;
		}
		if (o1.startsWith("<i>")) {
			o1 = o1.substring(3);
		}
		if (o2.startsWith("<i>")) {
			o2 = o2.substring(3);
		}
		return o1.compareToIgnoreCase(o2);
	}
}
