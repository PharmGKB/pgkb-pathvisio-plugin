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
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.gui.swing.propertypanel.TypeHandler;
import org.pathvisio.model.PropertyType;

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
