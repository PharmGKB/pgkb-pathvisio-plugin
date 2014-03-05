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
import org.pathvisio.util.Resources;

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
