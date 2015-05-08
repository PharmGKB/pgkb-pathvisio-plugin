/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;
import com.jidesoft.swing.AutoCompletionComboBox;
import org.apache.commons.lang3.StringUtils;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.desktop.PvDesktop;
import org.pharmgkb.pathvisio.PgkbType;
import org.pharmgkb.pathvisio.plugin.DictionaryPropertyType;

/**
 * This dialog allows the user to select the identity of the new element to create based on a dictionary.
 *
 * @author Mark Woon
 */
public class NewDictionaryElementDialog extends JDialog implements ActionListener, ItemListener, KeyListener {
	private static final String EMPTY_SELECTION = "---";
	private static final String sf_okButtonAction = "OK_BUTTON";
	private AutoCompletionComboBox m_autocompleteField;
	private JTextField m_pgkbIdField;
	private JTextField m_freetextField;
	private PgkbType m_type;
	private DictionaryPropertyType m_dictPropertyType;
	private PvDesktop m_desktop;
	private String m_selectedName;


	public NewDictionaryElementDialog(PvDesktop desktop, PgkbType type, DictionaryPropertyType dictPropType,
			boolean dictValueRequired) {
		// initialize JDialog
		super(desktop.getFrame(), "New " + type.getDisplayName(), true);

		m_type = type;
		m_dictPropertyType = dictPropType;
		m_desktop = desktop;
		JPanel mainPanel = new JPanel(new BorderLayout());

		// define main panel's layout
		FormLayout formLayout;
		if (dictValueRequired) {
			formLayout = new FormLayout("right:pref, 4dlu, pref", "p, 3dlu, p");
		} else {
			formLayout = new FormLayout("right:pref, 4dlu, pref", "p, 3dlu, p, 3dlu, p, 3dlu, p");
		}

		FormBuilder builder = FormBuilder.create()
				.layout(formLayout)
				.padding(Paddings.DIALOG);

		// build main panel
		builder.addLabel(m_type.getDisplayName() + ":")
				.xy(1, 1);
		m_autocompleteField = new AutoCompletionComboBox();
		m_autocompleteField.setStrict(true);
		m_autocompleteField.setStrictCompletion(true);
		if (dictPropType.getEntries().isEmpty()) {
			Logger.log.error("DictionaryProperty '" + dictPropType.getId() + "' has no entries");
		} else {
			//noinspection unchecked
			m_autocompleteField.addItem(EMPTY_SELECTION);
			for (String value : dictPropType.getReverseEnteries().keySet()) {
				//noinspection unchecked
				m_autocompleteField.addItem(value);
			}
		}
		m_autocompleteField.addItemListener(this);
		m_autocompleteField.getEditor().getEditorComponent().addKeyListener(this);
		m_autocompleteField.setFocusable(true);
		builder.add(m_autocompleteField)
				.xy(3, 1);
		builder.addLabel("PharmGKB ID:")
				.xy(1, 3);
		m_pgkbIdField = new JTextField();
		m_pgkbIdField.setEditable(false);
		builder.add(m_pgkbIdField)
				.xy(3, 3);

		if (!dictValueRequired) {
			builder.addLabel(" -- OR --")
					.xy(1, 5);
			builder.addLabel("Name:")
					.xy(1, 7);
			m_freetextField = new JTextField(30);
			m_freetextField.addKeyListener(this);
			builder.add(m_freetextField)
					.xy(3, 7);
		}

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
		if (m_freetextField != null) {
			m_freetextField.setText("");
		}
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
			String freetext = null;
			if (m_freetextField != null) {
				freetext = StringUtils.stripToNull(m_freetextField.getText());
			}
			String id = StringUtils.stripToNull(m_pgkbIdField.getText());

			if (id == null && freetext == null) {
				JOptionPane.showMessageDialog(m_desktop.getFrame(),
						"Please select a " + m_type.getDisplayName().toLowerCase() + ".",
						"Missing Selection", JOptionPane.PLAIN_MESSAGE);
				return;
			}
			if (freetext != null) {
				if (id != null) {
					JOptionPane.showMessageDialog(m_desktop.getFrame(),
							"Please pick an item OR provide a name.  Do not provide both.",
							"Confusing Input", JOptionPane.PLAIN_MESSAGE);
					return;
				}
				m_selectedName = freetext;
			} else {
				String check = " (" + id + ")";
				m_selectedName = (String)m_autocompleteField.getSelectedItem();
				if (m_selectedName.endsWith(check)) {
					m_selectedName = m_selectedName.substring(0, m_selectedName.length() - check.length());
				}
			}
			setVisible(false);
		}
	}


	//-- BEGIN ItemListener methods --//
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
	//-- END ActionListener  methods --//

	//-- BEGIN KeyListener  methods --//
	int m_escCount = 0;

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			m_escCount += 1;
			if (m_escCount == 2) {
				setVisible(false);
				m_escCount = 0;
			}
		} else {
			m_escCount = 0;
		}
	}
	//-- END KeyListener  methods --//
}
