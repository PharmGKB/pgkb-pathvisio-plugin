/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;
import org.pathvisio.gui.dialogs.OkCancelDialog;
import org.pharmgkb.pathvisio.plugin.DictionaryProperty;

/**
 * This dialog presents the user with values available for selection from a dictionary.
 * <p>
 * <b>Warning:  THIS CLASS REQUIRES JDK 1.8</b>
 *
 * @author Rebecca Tang
 */
public class DictionaryValuesDialog extends OkCancelDialog {
  private DictionaryProperty m_property;
  private SelectedDictionaryTableModel m_dictTableModel;
  private JTextField m_filterText;
  private TableRowSorter<DictionaryValuesModel> m_sorter;


  public DictionaryValuesDialog(SelectedDictionaryTableModel curDictTableModel, @Nullable Frame frame,
      Component locationComp, DictionaryProperty property) {
    super(frame, "Dictionary Entries", locationComp, true, false);
    m_property = property;
    m_dictTableModel = curDictTableModel;

    JPanel p = createDialogTablePane();
    setDialogComponent(p);
    setSize(p.getPreferredSize());
  }


  /**
   * table implementation
   */
  private JPanel createDialogTablePane() {

    DictionaryValuesModel model = new DictionaryValuesModel(m_property.getDictionaryType().getEntries());
    JTable table = new JTable(model);
    table.getColumnModel().getColumn(0).setMaxWidth(36);
    table.getColumnModel().getColumn(1).setCellRenderer(new StyledTableCellRenderer());
    m_sorter = new TableRowSorter<>(model);
    table.setRowSorter(m_sorter);
    table.setFillsViewportHeight(true);
    table.setTableHeader(null);
    JScrollPane scrollPane = new JScrollPane(table);

    // create a separate form for filterText
    JPanel filterPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JLabel filterTextLabel = new JLabel("Filter Text:", SwingConstants.LEFT);
    filterPane.add(filterTextLabel);
    m_filterText = new JTextField(30);
    // whenever filterText changes, invoke newFilter.
    m_filterText.getDocument().addDocumentListener(
        new DocumentListener() {
          public void changedUpdate(DocumentEvent e) {
            newFilter();
          }

          public void insertUpdate(DocumentEvent e) {
            newFilter();
          }

          public void removeUpdate(DocumentEvent e) {
            newFilter();
          }
        });
    filterTextLabel.setLabelFor(m_filterText);
    filterPane.add(m_filterText);

    FormBuilder builder = FormBuilder.create()
        .layout(new FormLayout("fill:pref:grow", "fill:default:grow, $rgap, default"))
        .padding(Paddings.DIALOG)
        .add(scrollPane).xy(1, 1)
        .add(filterPane).xy(1, 3);
    return builder.getPanel();
  }


  /**
   * Update the row filter regular expression from the expression in the text box.
   * <p>
   * XXX: JDK 1.6 specific
   */
  private void newFilter() {
    String text = m_filterText.getText().trim();
    if (!text.isEmpty()) {
      // If current expression doesn't parse, don't update.
      try {
        m_sorter.setRowFilter(new RegexFilter(text));
      } catch (java.util.regex.PatternSyntaxException ex) {
        // ignore
      }
    }
  }


  private class DictionaryValuesModel extends AbstractTableModel {
    private static final int NAME_COL = 1;
    private static final int CHECKBOX_COL = 0;
    private String[] columnNames = {"", ""};
    private List<Map.Entry<String, Boolean>> m_data;
    private Map<String, String> m_dictValues;
    // only used when property.isCollection == fase
    private int m_selectedRow = -1;


    public DictionaryValuesModel(Map<String, String> dictValues) {

      Map<String, Boolean> data = new HashMap<>();
      String selectedKey = null;
      for (String key : dictValues.keySet()) {
        boolean isSelected = m_dictTableModel.isSelected(key);
        if (m_property.isCollection()) {
          // don't display if selected
          if (!isSelected) {
            data.put(key, Boolean.FALSE);
          }
        } else {
          data.put(key, isSelected);
          if (isSelected) {
            selectedKey = key;
          }
        }
      }
      m_dictValues = dictValues;
      m_data = new ArrayList<>(data.entrySet());
      Collections.sort(m_data, (o1, o2) ->
          StyledTextComparator.getInstance().compare(m_dictValues.get(o1.getKey()), m_dictValues.get(o2.getKey()))
      );
      if (selectedKey != null) {
        for (Map.Entry<String, Boolean> entry : m_data) {
          if (entry.getValue()) {
            m_selectedRow = m_data.indexOf(entry);
            break;
          }
        }
      }
    }


    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return m_data.size();
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      Map.Entry<String, Boolean> entry = m_data.get(row);
      if (col == NAME_COL) {
        return m_dictValues.get(entry.getKey());
      } else {
        return entry.getValue();
      }
    }

    /*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
    public Class getColumnClass(int c) {
      if (c == NAME_COL) {
        return String.class;
      } else {
        return Boolean.class;
      }
    }

    public boolean isCellEditable(int row, int col) {
      return col == CHECKBOX_COL;
    }

    public void setValueAt(Object value, int row, int col) {

      if (col != CHECKBOX_COL) {
        throw new IllegalStateException("Only checkbox column is editable");
      }
      Map.Entry<String, Boolean> entry = m_data.get(row);
      Boolean doAdd = (Boolean)value;
      entry.setValue(doAdd);
      if (doAdd) {
        if (!m_property.isCollection()) {
          if (m_selectedRow != -1) {
            Map.Entry<String, Boolean> prevEntry = m_data.get(m_selectedRow);
            m_dictTableModel.setValue(prevEntry.getKey(), null);
            prevEntry.setValue(false);
            fireTableCellUpdated(m_selectedRow, col);
          }
          m_selectedRow = row;
        }
        m_dictTableModel.setValue(entry.getKey(), m_dictValues.get(entry.getKey()));
      } else {
        if (!m_property.isCollection()) {
          m_selectedRow = -1;
        }
        m_dictTableModel.setValue(entry.getKey(), null);
      }
      fireTableCellUpdated(row, col);
    }
  }


  private static class RegexFilter extends RowFilter<DictionaryValuesModel, Integer>  {
    private Matcher matcher;

    RegexFilter(String pattern) {
      matcher = Pattern.compile("^(<i>)?" + pattern, Pattern.CASE_INSENSITIVE).matcher("");
    }

    @Override
    public boolean include(Entry<? extends DictionaryValuesModel, ? extends Integer> entry) {
      int count = entry.getValueCount();
      while (--count >= 0) {
        matcher.reset(entry.getStringValue(count));
        if (matcher.find()) {
          return true;
        }
      }
      return false;
    }
  }
}
