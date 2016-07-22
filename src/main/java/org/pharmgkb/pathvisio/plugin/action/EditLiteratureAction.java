package org.pharmgkb.pathvisio.plugin.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import com.jidesoft.icons.IconsFactory;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.gui.CommonActions;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.view.VPathwaySwing;
import org.pharmgkb.pathvisio.plugin.PgkbPlugin;


/**
 * Action to edit the literature references of an elements.
 * This just delegates the work to {@link CommonActions.EditLiteratureAction}.
 *
 * @author Mark Woon
 */
public class EditLiteratureAction extends AbstractAction {
  private SwingEngine m_engine;

  public EditLiteratureAction(SwingEngine engine) {

    m_engine = engine;
    putValue(NAME, "Edit literature");
    putValue(SHORT_DESCRIPTION, "Edit literature references of selected element");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);

    ImageIcon icon = IconsFactory.getImageIcon(PgkbPlugin.class, "page_edit.png");
    if (icon != null) {
      putValue(Action.SMALL_ICON, icon);
      putValue(Action.LARGE_ICON_KEY, icon);
    }
  }


  public void actionPerformed(ActionEvent e) {

    Set<VPathwayElement> elems = m_engine.getEngine().getActiveVPathway().getSelectedPathwayElements();
    if (elems.size() == 0) {
      JOptionPane.showMessageDialog(m_engine.getFrame(),
          "Must select something before adding literature.",
          "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    if (elems.size() > 1) {
      JOptionPane.showMessageDialog(m_engine.getFrame(),
          "Can only edit literature on a single element at a time.",
          "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // delegate to built-in action
    VPathwayElement element = elems.iterator().next();
    new CommonActions.EditLiteratureAction(m_engine, (VPathwaySwing)element.getDrawing().getWrapper(), element)
        .actionPerformed(e);
  }
}
