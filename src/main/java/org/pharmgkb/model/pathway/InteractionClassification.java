package org.pharmgkb.model.pathway;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;


/**
 * This enum lists all possible interaction types.
 *
 * @author Rebecca Tang
 * @author Mark Woon
 */
public enum InteractionClassification implements ExtendedEnum {
  INTERACTION(0, "interaction", "Interaction", null),
  TEMPLATE_REACTION(10, "templateReaction", "Template Reaction", INTERACTION),
  // custom non-BioPax types
  LEADS_TO(301, "leadsTo", "Leads To", INTERACTION),
  COMES_FROM(302, "comesFrom", "Comes From", INTERACTION),

  // conversions
  CONVERSION(1, "conversion", "Conversion", INTERACTION),
  BIOCHEMICAL_REACTION(6, "biochemicalReaction", "Biochemical Reaction", CONVERSION),
  COMPLEX_ASSEMBLY(3, "complexAssembly", "Complex Assembly", CONVERSION),
  DEGRADATION(104, "degradation", "Degradation", CONVERSION),
  TRANSPORT(5, "transport", "Transport", CONVERSION),

  // controls
  CONTROL(2, "control", "Control", INTERACTION),
  ACTIVATION(201, "activation", "Activation", CONTROL),
  CATALYSIS(8, "catalysis", "Catalysis", CONTROL),
  INHIBITION(203, "inhibition", "Inhibition", CONTROL),
  @Deprecated
  MODULATION(204, "modulation", "Modulation", CONTROL),
  @Deprecated
  TEMPLATE_REACTION_REGULATION(205, "templateReactonRegulation", "Template Reaction Regulation", CONTROL),
  ;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<InteractionClassification> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private final @Nullable InteractionClassification m_parent;


  /**
   * Standard constructor.
   */
  InteractionClassification(int id, String shortName, String displayName, @Nullable InteractionClassification parent) {
    m_id = id;
    m_shortName = shortName;
    m_displayName = displayName;
    m_parent = parent;
    init();
  }


  public boolean isControl() {
    return m_parent == CONTROL || this == CONTROL;
  }


  /**
   * Gets the parent {@link InteractionClassification}.
   */
  public @Nullable InteractionClassification getParent() {
    return m_parent;
  }


  //-- BEGIN ExtendedEnum methods --//
  private synchronized void init() {
    //noinspection ConstantValue
    if (s_extendedEnumHelper == null) {
      s_extendedEnumHelper = new ExtendedEnumHelper<>(getClass());
    }
    s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName);
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public String getShortName() {
    return m_shortName;
  }

  @Override
  public String getDisplayName() {
    if (m_displayName != null) {
      return m_displayName;
    }
    return m_shortName;
  }

  @Override
  public final String toString() {
    return getDisplayName();
  }
  //-- END ExtendedEnum methods --//

  //-- BEGIN ExtendedEnum statics --//
  public static @Nullable InteractionClassification lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable InteractionClassification lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<InteractionClassification> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<InteractionClassification> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
