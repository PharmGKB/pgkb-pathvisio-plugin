package org.pharmgkb.pathvisio;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.PathwayElement;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;
import org.pharmgkb.model.pathway.NodeProperty;


/**
 * This is an enumeration of PathVisio dynamic properties.
 *
 * @author Mark Woon
 */
public enum DynamicProperty implements ExtendedEnum {
  // mappinfo properties
  SUB_ID(1, "subId", true, false),
  COE(2, "coe", true, false),

  // node properties
  PGKB_ID(11, "pgkbId", true, false),
  TYPE(12, "type", true, false),

  ORGANISM(NodeProperty.ORGANISM, true),
  ORGANISM_ID(NodeProperty.ORGANISM_ID, true),
  CELL(NodeProperty.CELL, false),
  CELLULAR_LOCATION(NodeProperty.CELLULAR_LOCATION, true),
  TRANSMEMBRANE_TYPE(NodeProperty.TRANSMEMBRANE_TYPE, true),
  ROLE(NodeProperty.ROLE, false),
  STATE(NodeProperty.STATE, false),
  MODIFICATION(NodeProperty.MODIFICATION, true),
  MODIFICATION_POSITION(NodeProperty.MODIFICATION_POSITION, true),
  GENE_SYMBOL(NodeProperty.GENE_SYMBOL, false),
  GENE_XREF(NodeProperty.GENE_XREF, false),

  GENE_COMPLEX_COMPONENTS(50, "geneComplexComponents", true, false),
  DRUG_COMPLEX_COMPONENTS(51, "drugComplexComponents", true, false),
  MEMBER_GENES(52, "memberGenes", true, false),
  MEMBER_DRUGS(53, "memberDrugs", true, false),
  ALL_MEMBERS_REQUIRED(54, "allMembersRequired", true, false),

  // line properties
  INTERACTION_TYPE(101, "biopaxInteractionType", false, false),
  REACTION_NAME(102, "reactionName", false, true),
  LINE_STRENGTH(103, "lineStrength", false, false),
  IS_REVERSIBLE(104, "isReversible", false, false),
  @Deprecated
  XREF_DB(110, "externalXrefDb", false, false),
  @Deprecated
  XREF_ID(111, "externalXrefId", false, false),
  @Deprecated
  REACTION_XREF_DB(112, "reactionXrefDb", false, false),
  @Deprecated
  REACTION_XREF_ID(113, "reactionXrefId", false, false)
  ;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<DynamicProperty> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private @Nullable NodeProperty m_nodeProperty;
  private final boolean m_isNodeProperty;
  private final boolean m_isBiopaxAnnotation;


  DynamicProperty(NodeProperty nodeProperty, boolean isBiopaxAnnotation) {
    this(nodeProperty.getId(), nodeProperty.getShortName(), true, isBiopaxAnnotation);
    m_nodeProperty = nodeProperty;
  }

  DynamicProperty(int id, String shortName, boolean isNodeProperty, boolean isBiopaxAnnotation) {
    m_id = id;
    m_shortName = "pgkb." + shortName;
    m_displayName = shortName;
    m_isNodeProperty = isNodeProperty;
    m_isBiopaxAnnotation = isBiopaxAnnotation;
    init();
  }

  public boolean isNodeProperty() {
    return m_isNodeProperty;
  }

  public boolean isSaveAsBiopaxAnnotation() {
    return m_isBiopaxAnnotation;
  }

  public @Nullable NodeProperty getNodeProperty() {
    return m_nodeProperty;
  }


  /**
   * Gets the value of this dynamic property from the {@link PathwayElement}.
   */
  public @Nullable String of(PathwayElement pvElem) {
    Preconditions.checkNotNull(pvElem);
    return StringUtils.stripToNull(pvElem.getDynamicProperty(m_shortName));
  }

  /**
   * Gets the value of this dynamic property as a set of dictionary terms ({@code <id, name>}).
   */
  public Map<String, String> dictionaryTermsOf(PathwayElement pvElem) {

    String termString = of(pvElem);
    if (termString == null) {
      return Collections.emptyMap();
    }
    return DataConstants.TERMS_SPLITTER.split(termString);
  }

  /**
   * Sets the value of this dynamic value on the {@link PathwayElement}.
   */
  public void set(PathwayElement pvElem, @Nullable String value) {
    Preconditions.checkNotNull(pvElem);
    pvElem.setDynamicProperty(m_shortName, value);
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
  public static @Nullable DynamicProperty lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable DynamicProperty lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<DynamicProperty> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<DynamicProperty> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
