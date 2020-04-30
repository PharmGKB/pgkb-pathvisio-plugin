package org.pharmgkb.pathvisio.plugin.swing;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * This class contains utility methods for working with Swing.
 *
 * @author Mark Woon
 */
public class SwingUtils {
  public static final String CANCEL_BUTTON_ACTION = "CANCEL_BUTTON";

  /**
   * Private constructor.
   */
  private SwingUtils() {
  }


  /**
   * Returns a cancel button that calls the given action, and binds it to the ESC key.
   */
  public static JButton buildCancelButton(JComponent parent, Action cancelAction) {
    KeyStroke escKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    parent.getInputMap().put(escKeystroke, CANCEL_BUTTON_ACTION);
    parent.getActionMap().put(CANCEL_BUTTON_ACTION, cancelAction);
    JButton cancelButton = new JButton(cancelAction);
    cancelButton.setText("Cancel");
    return cancelButton;
  }
}
