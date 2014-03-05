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
package org.pharmgkb.pathvisio;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.gui.swing.propertypanel.TypeHandler;
import org.pathvisio.model.PropertyType;
import org.pathvisio.util.Utils;
import org.pharmgkb.pathvisio.swing.DictionaryDialog;
import org.pharmgkb.pathvisio.swing.StyledTableCellRenderer;

/**
 * This defines an dictionary Property which is also it's own PropertyType.
 * This should be paired with the DictionaryHandler.
 *
 * @author Rebecca Tang
 */
public class DictionaryHandler extends AbstractCellEditor implements TypeHandler, TableCellEditor, TableCellRenderer,
		ActionListener {
	private static final String sf_buttonLabel = "Select Data From Dictionary";
	private static final String sf_editAction = "edit";

	private JFrame m_frame;
	private DictionaryProperty m_property;
	private TableCellRenderer m_valueRenderer = new StyledTableCellRenderer();
	private JButton m_button;
	private DictionaryDialog m_dictionaryDialog;


	public DictionaryHandler(JFrame frame, DictionaryProperty property) {

		if (property == null) {
			throw new NullPointerException("property cannot be null");
		}
		m_frame = frame;
		m_property = property;
		m_button = new JButton();
		m_button.setText(sf_buttonLabel);
		m_button.setActionCommand(sf_editAction);
		m_button.addActionListener(this);
	}

	public PropertyType getType() {
		return m_property.getType();
	}


	public TableCellRenderer getLabelRenderer() {
		return null;
	}

	public TableCellRenderer getValueRenderer() {
		return this;
	}

	public TableCellEditor getValueEditor() {
		return this;
	}


	//-- TableCellRenderer methods --//

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		StringBuilder valBuilder = new StringBuilder();
		String entries = (String)value;
		if (!Utils.isEmpty(entries)) {
			if (!m_property.isCollection()) {
				// treat as enum (i.e. only have value, no id)
				valBuilder.append(entries);
			} else {
				String[] data = entries.split("\t");
				for (String d : data) {
					String[] entry = d.split("::");
					if (valBuilder.length() > 0) {
						valBuilder.append(", ");
					}
					valBuilder.append(entry[1]);
				}
			}
		}
		return m_valueRenderer.getTableCellRendererComponent(table, valBuilder.toString(), isSelected, hasFocus, row, column);
	}


	//-- TableCellEditor methods --//

	public Object getCellEditorValue() {
		if (m_dictionaryDialog == null) {
			return "";
		} else {
			return m_dictionaryDialog.getData();
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (m_dictionaryDialog == null) {
			m_dictionaryDialog = new DictionaryDialog(m_frame, m_frame, m_property);
		}
		String v = (String)value;
		if (!m_property.isCollection() && !Utils.isEmpty(v)) {
			// convert from enum, which only has a value and no id
			String key = m_property.getType().getReverseEnteries().get(v);
			if (key == null) {
				JOptionPane.showMessageDialog(this.m_button,
						"Potentially obsolete data?  The '" + m_property.getName() + "' dictionary has no entry for '" + v + "'. Ignoring value!",
						"Warning", JOptionPane.WARNING_MESSAGE);
				v = "";
			} else {
				v = key + "::" + v;
			}
		}
		m_dictionaryDialog.setData(v);
		return m_button;
	}


	public void actionPerformed(ActionEvent e) {
		if (sf_editAction.equals(e.getActionCommand())) {
			if (m_property.getType().getEntries().isEmpty()) {
				JOptionPane.showMessageDialog(this.m_button,
						"The '" + m_property.getName() + "' dictionary has no entries", "Warning",
						JOptionPane.WARNING_MESSAGE);
			} else {
				m_dictionaryDialog.setVisible(true);
				fireEditingStopped();
			}
		}
	}
}
