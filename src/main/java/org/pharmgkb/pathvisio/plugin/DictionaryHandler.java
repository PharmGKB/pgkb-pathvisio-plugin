/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nullable;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.util.Utils;
import org.pathvisio.gui.handler.TypeHandler;
import org.pharmgkb.pathvisio.plugin.swing.DictionaryDialog;
import org.pharmgkb.pathvisio.plugin.swing.StyledTableCellRenderer;

/**
 * This is a {@link TypeHandler} for {@link DictionaryProperty}.
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

  @Override
	public PropertyType getType() {
		return m_property.getType();
	}


  @Override
	public @Nullable TableCellRenderer getLabelRenderer() {
		return null;
	}

  @Override
  public @Nullable TableCellRenderer getValueRenderer() {
		return this;
	}

  @Override
  public @Nullable TableCellEditor getValueEditor() {
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
			String key = m_property.getDictionaryType().getReverseEnteries().get(v);
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
			if (m_property.getDictionaryType().getEntries().isEmpty()) {
				JOptionPane.showMessageDialog(this.m_button,
						"The '" + m_property.getName() + "' dictionary has no entries", "Warning",
						JOptionPane.WARNING_MESSAGE);
			} else {
				m_dictionaryDialog.setVisible(true);
				fireEditingStopped();
			}
		}
	}

	@Override
	public String toString() {
		return "DictionaryHandler:" + m_property.getId();
	}
}
