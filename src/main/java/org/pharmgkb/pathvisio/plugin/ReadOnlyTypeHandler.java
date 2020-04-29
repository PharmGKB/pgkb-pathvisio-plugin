package org.pharmgkb.pathvisio.plugin;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import com.google.common.base.Preconditions;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.gui.handler.TypeHandler;

/**
 * This type handler wraps around another type handler and makes it read-only.
 *
 * @author Mark Woon
 */
public class ReadOnlyTypeHandler implements TypeHandler {
  private PropertyType m_type;
  private TypeHandler m_handler;
  private NotEditableTableCellEditor m_cellEditor;


  public ReadOnlyTypeHandler(PropertyType type, TypeHandler typeHandler) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(typeHandler);
    m_type = type;
    m_handler = typeHandler;
    m_cellEditor = new NotEditableTableCellEditor(typeHandler);
  }


  public PropertyType getType() {
    return m_type;
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

  @Override
  public String toString() {
    return "ReadOnlyTypeHandler:" + m_handler.getType();
  }
}
