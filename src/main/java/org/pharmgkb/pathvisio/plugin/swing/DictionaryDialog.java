/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.gui.dialogs.OkCancelDialog;
import org.pharmgkb.pathvisio.plugin.DictionaryProperty;

/**
 * <p>
 * <b>Warning:  THIS CLASS REQUIRES JDK 1.8</b>
 *
 * @author Rebecca Tang
 */
public class DictionaryDialog extends OkCancelDialog {
	private static final String sf_addButtonAction = "Select Value";
	private DictionaryProperty m_property;
	private SelectedDictionaryTableModel m_dictTableModel;
	private JButton m_addButton;


	public DictionaryDialog(Frame frame, Component locationComp, DictionaryProperty property) {

		super(frame, property.isCollection() ? "Select Terms" : "Select Term", locationComp, true, true);
		m_property = property;
		setSize(450, 300);

		// build panel
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		// table
		m_dictTableModel = new SelectedDictionaryTableModel(property.isCollection());
		JTable dictTable = new JTable(m_dictTableModel) {
			private TableCellRenderer renderer = new DeleteableTextCellRenderer();
			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}
		};
		dictTable.setTableHeader(null);
		JScrollPane tablePane = new JScrollPane(dictTable);
		mainPanel.add(tablePane, BorderLayout.CENTER);

		// buttons
		JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
		m_addButton = new JButton(property.isCollection() ? sf_addButtonAction + "s" : sf_addButtonAction);
		m_addButton.setActionCommand(sf_addButtonAction);
		m_addButton.addActionListener(this);
		btnPane.add(m_addButton);
		mainPanel.add(btnPane, BorderLayout.SOUTH);

		setDialogComponent(mainPanel);
	}


	public String getData() {
		return m_dictTableModel.getData();
	}

	public void setData(String data) {
		m_dictTableModel.setData(data);
	}

	public void setReadOnly(boolean readOnly) {
		m_addButton.setEnabled(!readOnly);
	}


	private void refresh() {
		validate();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(sf_addButtonAction)) {
			final DictionaryValuesDialog d = new DictionaryValuesDialog(m_dictTableModel, null, this, m_property);
			if (!SwingUtilities.isEventDispatchThread()) {
				try {
					SwingUtilities.invokeAndWait(() -> d.setVisible(true));
				} catch (Exception ex) {
					Logger.log.error("Unable to open dialog", ex);
				}
			} else {
				d.setVisible(true);
			}
			if (d.getExitCode().equals(DictionaryValuesDialog.OK)) {
				refresh();
			}
		} else {
			super.actionPerformed(e);
		}
	}


	@Override
	protected void cancelPressed() {
		m_dictTableModel.resetData();
		super.cancelPressed();
	}
}
