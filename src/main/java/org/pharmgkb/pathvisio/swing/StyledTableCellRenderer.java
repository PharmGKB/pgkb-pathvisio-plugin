/*
 ----- BEGIN LICENSE BLOCK -----
 Version: MPL 1.1/GPL 2.0/LGPL 2.1

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with the
 License. You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is PharmGKB.

 The Initial Developer of the Original Code is PharmGKB (The Pharmacogenetics
 and Pharmacogenetics Knowledge Base, supported by NIH U01GM61374). Portions
 created by the Initial Developer are Copyright (C) 2014 the Initial Developer.
 All Rights Reserved.

 Contributor(s):

 Alternatively, the contents of this file may be used under the terms of
 either the GNU General Public License Version 2 or later (the "GPL"), or the
 GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in
 which case the provisions of the GPL or the LGPL are applicable instead of
 those above. If you wish to allow use of your version of this file only
 under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your
 decision by deleting the provisions above and replace them with the notice
 and other provisions required by the GPL or the LGPL. If you do not delete
 the provisions above, a recipient may use your version of this file under
 the terms of any one of the MPL, the GPL or the LGPL.

 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.swing;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import com.jidesoft.swing.StyleRange;
import com.jidesoft.swing.StyledLabel;

/**
 * This table cell renderer allows text in HTML format.
 *
 * @author Mark Woon
 */
public class StyledTableCellRenderer implements TableCellRenderer {
	private static final StyleRange[] sf_plainStyledRange = new StyleRange[] { new StyleRange(Font.PLAIN) };
	private StyledLabel m_label;

	public StyledTableCellRenderer() {
		m_label = new StyledLabel();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		updateStyledLabel(m_label, (String)value);
		return m_label;
	}


	public static void updateStyledLabel(StyledLabel label, String text) {

		if (text.contains("<i>")) {
			String[] parts = text.split("<i>");
			StringBuilder styleBuilder = new StringBuilder();
			List<StyleRange> styleRanges = new ArrayList<>();
			if (parts[0].length() > 0) {
				styleBuilder.append(parts[0]);
				styleRanges.add(new StyleRange(0, parts[0].length(), Font.PLAIN));
			}
			for (int x = 1; x < parts.length; x++) {
				int idx = parts[x].indexOf("</i>");

				String italic = parts[x].substring(0, idx);
				styleRanges.add(new StyleRange(styleBuilder.length(), italic.length(), Font.ITALIC));
				styleBuilder.append(italic);

				idx += 4;
				if (idx != parts[x].length()) {
					String plain = parts[x].substring(idx);
					styleRanges.add(new StyleRange(styleBuilder.length(), plain.length(), Font.PLAIN));
					styleBuilder.append(plain);
				}
			}
			text = styleBuilder.toString();
			StyleRange[] ranges = new StyleRange[styleRanges.size()];
			for (int x = 0; x < ranges.length; x++) {
				ranges[x] = styleRanges.get(x);
			}
			label.setStyleRanges(ranges);
		} else {
			label.setStyleRanges(sf_plainStyledRange);
		}
		label.setText(text);
	}
}
