/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticPropertyType;
import org.pathvisio.gui.handler.TypeHandler;


/**
 * This is a {@link TypeHandler} for {@link StaticPropertyType#STRING}.
 * Mainly so that it can be wrapped by {@link ReadOnlyTypeHandler}.
 *
 * @author Mark Woon
 */
public class StringTypeHandler extends DefaultCellEditor implements TypeHandler {
 private DefaultTableCellRenderer m_valueRenderer = new DefaultTableCellRenderer();

 public StringTypeHandler() {
  super(new JTextField());
 }


 @Override
 public PropertyType getType() {
  return StaticPropertyType.STRING;
 }

 @Override
 public TableCellRenderer getLabelRenderer() {
  return null;
 }

 @Override
 public TableCellRenderer getValueRenderer() {
  return m_valueRenderer;
 }

 @Override
 public TableCellEditor getValueEditor() {
  return this;
 }


 @Override
 public String toString() {
  return "StringTypeHandler";
 }
}
