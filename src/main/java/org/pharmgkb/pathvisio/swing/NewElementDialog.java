/*
 ----- BEGIN LICENSE BLOCK -----
 This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ----- END LICENSE BLOCK -----
 */
package org.pharmgkb.pathvisio.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.pathvisio.gui.swing.PvDesktop;
import org.pathvisio.util.Utils;
import org.pharmgkb.pathvisio.PgkbType;

/**
 * This dialog allows the user to name the new element to create.
 *
 * @author Mark Woon
 */
public class NewElementDialog extends JDialog implements ActionListener {
	private static final String sf_okButtonAction = "OK_BUTTON";
	private PgkbType m_type;
	private JTextField m_textField;
	private PvDesktop m_desktop;


	public NewElementDialog(PvDesktop desktop, PgkbType type) {
		// initialize JDialog
		super(desktop.getFrame(), "New " + type.getDisplayName(), true);

		m_type = type;
		m_desktop = desktop;
		JPanel mainPanel = new JPanel(new BorderLayout());

		// define main panel's layout
		FormLayout formLayout = new FormLayout("right:pref, 4dlu, pref", "p");
		PanelBuilder builder = new PanelBuilder(formLayout);
    builder.border(Borders.DIALOG);

    CellConstraints cc = new CellConstraints();
		// build main panel
		builder.addLabel(m_type.getDisplayName() + ":", cc.xy(1, 1));
		m_textField = new JTextField(30);
		m_textField.setFocusable(true);
		builder.add(m_textField, cc.xy(3, 1));
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
		m_textField.requestFocusInWindow();
		setLocationRelativeTo(desktop.getFrame());
	}

	/**
	 * Prepare dialog for next use.
	 */
	public void reset() {
		m_textField.setText("");
		m_textField.requestFocusInWindow();
	}


	public String getName() {
		return m_textField.getText();
	}


	//--  ActionListener  methods --//

	public void actionPerformed(ActionEvent e) {
		if (sf_okButtonAction.equals(e.getActionCommand())) {
			if (!Utils.isEmpty(m_textField.getText())) {
				setVisible(false);
			} else {
				JOptionPane.showMessageDialog(m_desktop.getFrame(),
						"Please enter a name for the " + m_type.getDisplayName().toLowerCase() + ".",
						"Missing Selection", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}
}
