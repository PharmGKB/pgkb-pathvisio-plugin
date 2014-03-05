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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jidesoft.swing.AutoCompletionComboBox;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.util.Utils;
import org.pharmgkb.pathvisio.DictionaryPropertyType;
import org.pharmgkb.pathvisio.PgkbType;

/**
 * This dialog allows the user to select the identity of the new element to create based on a dictionary.
 *
 * @author Mark Woon
 */
public class NewDictionaryElementDialog extends JDialog implements ActionListener, ItemListener {
	private static final String EMPTY_SELECTION = "---";
	private static final String sf_okButtonAction = "OK_BUTTON";
	private AutoCompletionComboBox m_autocompleteField;
	private JTextField m_pgkbIdField;
	private PgkbType m_type;
	private DictionaryPropertyType m_dictPropertyType;
	private PvDesktop m_desktop;
	private String m_selectedName;


	public NewDictionaryElementDialog(PvDesktop desktop, PgkbType type, DictionaryPropertyType dictPropType) {
		// initialize JDialog
		super(desktop.getFrame(), "New " + type.getDisplayName(), true);

		m_type = type;
		m_dictPropertyType = dictPropType;
		m_desktop = desktop;
		JPanel mainPanel = new JPanel(new BorderLayout());

		// define main panel's layout
		FormLayout formLayout = new FormLayout("right:pref, 4dlu, pref", "p, 3dlu, p");
		PanelBuilder builder = new PanelBuilder(formLayout);
		builder.border(Borders.DIALOG);
		CellConstraints cc = new CellConstraints();
		// build main panel
		builder.addLabel(m_type.getDisplayName() + ":", cc.xy(1, 1));
		m_autocompleteField = new AutoCompletionComboBox();
		m_autocompleteField.setStrict(true);
		m_autocompleteField.setStrictCompletion(true);
		if (dictPropType.getEntries().isEmpty()) {
			Logger.log.error("DictionaryProperty '" + dictPropType.getId() + "' has no entries");
		} else {
			m_autocompleteField.addItem(EMPTY_SELECTION);
			for (String value : dictPropType.getReverseEnteries().keySet()) {
				m_autocompleteField.addItem(value);
			}
		}
		m_autocompleteField.addItemListener(this);
		m_autocompleteField.setFocusable(true);
		builder.add(m_autocompleteField, cc.xy(3, 1));
		builder.addLabel("PharmGKB ID:", cc.xy(1, 3));
		m_pgkbIdField = new JTextField();
		m_pgkbIdField.setEditable(false);
		builder.add(m_pgkbIdField, cc.xy(3, 3));
		mainPanel.add(builder.getPanel(), BorderLayout.CENTER);

		// build button pane
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());  // align buttons to the right
		// ok button
		JButton okButton = new JButton("Create");
		okButton.setActionCommand(sf_okButtonAction);
		okButton.addActionListener(this);
		getRootPane().setDefaultButton(okButton);
		buttonPanel.add(okButton);
		// cancel button
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(SwingUtils.buildCancelButton(mainPanel, cancelAction));

		mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
		add(mainPanel);
		pack();
		m_autocompleteField.requestFocusInWindow();
		setLocationRelativeTo(desktop.getFrame());
	}


	/**
	 * Prepare dialog for next use.
	 */
	public void reset() {
		m_selectedName = null;
		m_autocompleteField.setSelectedItem(EMPTY_SELECTION);
		m_autocompleteField.requestFocusInWindow();
		m_pgkbIdField.setText("");
	}


	public String getSelectedName() {
		return m_selectedName;
	}

	public String getSelectedAccessionId() {
		return m_pgkbIdField.getText();
	}


	//--  ActionListener  methods --//

	public void actionPerformed(ActionEvent e) {
		if (sf_okButtonAction.equals(e.getActionCommand())) {
			if (!Utils.isEmpty(m_pgkbIdField.getText())) {
				String check = " (" + m_pgkbIdField.getText() + ")";
				m_selectedName = (String)m_autocompleteField.getSelectedItem();
				if (m_selectedName.endsWith(check)) {
					m_selectedName = m_selectedName.substring(0, m_selectedName.length() - check.length());
				}
				setVisible(false);
			} else {
				JOptionPane.showMessageDialog(m_desktop.getFrame(),
						"Please select a " + m_type.getDisplayName().toLowerCase() + ".",
						"Missing Selection", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}


	//-- ItemListener methods --//

	public void itemStateChanged(ItemEvent e) {
		if (m_pgkbIdField != null) {
			String name = (String)m_autocompleteField.getSelectedItem();
			if (EMPTY_SELECTION.equals(name)) {
				m_pgkbIdField.setText("");
			} else {
				m_pgkbIdField.setText(m_dictPropertyType.getReverseEnteries().get(name));
			}
		}
	}
}
