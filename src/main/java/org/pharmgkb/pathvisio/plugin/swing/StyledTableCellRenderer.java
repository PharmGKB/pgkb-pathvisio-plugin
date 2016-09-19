/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

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
