package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.pathvisio.core.util.Utils;
import org.pathvisio.desktop.PvDesktop;
import org.pharmgkb.pathvisio.PgkbType;

/**
 * This dialog allows the user to name the new element to create.
 *
 * @author Mark Woon
 */
public class NewElementDialog extends JDialog implements ActionListener, KeyListener {
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
    FormBuilder builder = FormBuilder.create()
        .layout(formLayout)
        .padding(Paddings.DIALOG);

    // build main panel
    builder.addLabel(m_type.getDisplayName() + ":")
        .xy(1, 1);
    m_textField = new JTextField(30);
    m_textField.setFocusable(true);
    m_textField.addKeyListener(this);
    builder.add(m_textField)
        .xy(3, 1);
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


  //-- BEGIN ActionListener  methods --//
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
  //-- END ActionListener  methods --//

  //-- BEGIN KeyListener  methods --//
  private int m_escCount = 0;

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
