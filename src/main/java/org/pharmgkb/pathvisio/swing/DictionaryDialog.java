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
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.dialogs.OkCancelDialog;
import org.pharmgkb.pathvisio.DictionaryProperty;

/**
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
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							d.setVisible(true);
						}
					});
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
