/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import com.jidesoft.swing.StyledLabel;
import org.pathvisio.core.util.Resources;

/**
 * @author Rebecca Tang
 */
public class DeleteableTextCellRenderer implements TableCellRenderer, MouseListener {
  private static final String REMOVE_BUTTON_ACTION = "Remove";
  private static final URL REMOVE_BUTTON_IMG = Resources.getResourceURL("cancel.gif");

  private JPanel m_panel;
  private StyledLabel m_label;
  private JTable m_table;
  private long m_lastClick;

  public DeleteableTextCellRenderer() {

    m_panel = new JPanel(new BorderLayout());
    m_label = new StyledLabel();
    m_panel.add(m_label, BorderLayout.CENTER);
    JButton btnRemove = new JButton();
    btnRemove.setActionCommand(REMOVE_BUTTON_ACTION);
    btnRemove.setIcon(new ImageIcon(REMOVE_BUTTON_IMG));
    btnRemove.setBackground(Color.WHITE);
    btnRemove.setBorder(null);
    btnRemove.setToolTipText("Remove this dictionary entry");
    m_panel.add(btnRemove, BorderLayout.EAST);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {

    StyledTableCellRenderer.updateStyledLabel(m_label, (String)value);
    table.addMouseListener(this);
    m_table = table;
    return m_panel;
  }

  private void remove(MouseEvent e) {
    if (e.getWhen() > m_lastClick) {
      int row = e.getY() / m_table.getRowHeight();
      ((SelectedDictionaryTableModel)m_table.getModel()).removeValueAt(row);
      m_lastClick = e.getWhen();
    }
  }

  public void mouseClicked(MouseEvent e) {
    remove(e);
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }
}
