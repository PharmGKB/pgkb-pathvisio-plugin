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
