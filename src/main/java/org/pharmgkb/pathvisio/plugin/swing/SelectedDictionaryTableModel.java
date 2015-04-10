/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;
import org.pathvisio.core.util.Utils;

/**
 * This table model holds the list of selected dictioanry entries.
 *
 * @author Rebecca Tang
 */
public class SelectedDictionaryTableModel extends AbstractTableModel {
	boolean m_isMultiselect;
	private String m_initialState;
	private Map<String, String> m_unsortedData = new HashMap<>();
	private SortedMap<String, String> m_data = new TreeMap<>(new Comparator<String>() {
		public int compare(String o1, String o2) {
			return StyledTextComparator.getInstance().compare(m_unsortedData.get(o1), m_unsortedData.get(o2));
		}
	});


	/**
	 * Constructor.
	 */
	public SelectedDictionaryTableModel(boolean isMultiselect) {
		m_isMultiselect = isMultiselect;
	}

	/**
	 * Sets what entries are selected.
	 *
	 * @param initialState must be in the same format that {@link #getData()} produces
	 */
	public void setData(String initialState) {

		m_initialState = initialState;
		if (m_data.size() > 0) {
			m_data.clear();
			m_unsortedData.clear();
		}
		if (!Utils.isEmpty(initialState)) {
			String[] data = initialState.split("\t");
			for (String d : data) {
				String[] items = d.split("::");
				m_unsortedData.put(items[0], items[1]);
				m_data.put(items[0], items[1]);
			}
		}
	}

	/**
	 * Gets the selected dictionary items in String form (tab separated "key::value").
	 */
	public String getData() {

		String data = "";
		if (m_data.entrySet().size() > 0) {
			if (m_isMultiselect) {
				StringBuilder dataBuilder = new StringBuilder();
				for (Map.Entry<String, String> e : m_data.entrySet()) {
					if (dataBuilder.length() > 0) {
						dataBuilder.append("\t");
					}
					dataBuilder.append(e.getKey())
							.append("::")
							.append(e.getValue());
				}
				data = dataBuilder.toString();
			} else {
				// treat it like an enum
				data = m_data.entrySet().iterator().next().getValue();
			}
		}
		return data;
	}


	public void resetData() {
		setData(m_initialState);
	}


	public boolean isSelected(String key) {
		return m_data.containsKey(key);
	}


	public void setValue(String key, String value) {
		if (value != null) {
			m_unsortedData.put(key, value);
			m_data.put(key, value);
		} else {
			m_data.remove(key);
			m_unsortedData.remove(key);
		}
		fireTableDataChanged();
	}


	public void removeValueAt(int row) {
		if (row >= 0 && row < m_data.size()) {
			String key = (String)m_data.keySet().toArray()[row];
			m_data.remove(key);
			m_unsortedData.remove(key);
			fireTableRowsDeleted(row, row);
		}
	}


	//-- TableModel methods --//

	public int getRowCount() {
		return m_data.size();
	}

	public int getColumnCount() {
		return 1;
	}

	public String getColumnName(int col) {
		return "";
	}

	public Class getColumnClass(int col) {
		return String.class;
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public String getValueAt(int row, int col) {
		return (String)m_data.values().toArray()[row];
	}
}
