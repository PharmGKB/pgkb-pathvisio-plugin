/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.gui.handler.TypeHandler;

/**
 * This type handler wraps around another type handler and makes it read-only.
 *
 * @author Mark Woon
 */
public class ReadOnlyTypeHandler implements TypeHandler {
	private TypeHandler m_handler;
	private NotEditableTableCellEditor m_cellEditor;


	public ReadOnlyTypeHandler(TypeHandler typeHandler) {
		m_handler = typeHandler;
		m_cellEditor = new NotEditableTableCellEditor(typeHandler);
	}


	public PropertyType getType() {
		return m_handler.getType();
	}

	public TableCellRenderer getLabelRenderer() {
		return m_handler.getLabelRenderer();
	}

	public TableCellRenderer getValueRenderer() {
		return m_handler.getValueRenderer();
	}

	public TableCellEditor getValueEditor() {
		return m_cellEditor;
	}


	private class NotEditableTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		private TypeHandler m_handler;
		private Object m_value;

		public NotEditableTableCellEditor(TypeHandler handler) {
			m_handler = handler;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			m_value = value;
			return m_handler.getValueEditor().getTableCellEditorComponent(table, value, isSelected, row, column);
		}

		public Object getCellEditorValue() {
			return m_value;
		}

		public boolean isCellEditable(EventObject anEvent) {
			return false;
		}
	}
}
