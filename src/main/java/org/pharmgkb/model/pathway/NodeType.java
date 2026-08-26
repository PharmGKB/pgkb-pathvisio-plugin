package org.pharmgkb.model.pathway;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;


/**
 * This is an enumeration of pathway node types.
 *
 * @author Mark Woon
 */
public enum NodeType implements ExtendedEnum {
  GENE(1, "Gene", true, false, false),
  HAPLOTYPE(2, "Haplotype", true, false, false),

  GENE_COLLECTION(11, "Gene Collection", true, false, true),
  GENE_COMPLEX(12, "Gene Complex", true, false, true),

  DRUG(21, "Drug", true, false, false),
  METABOLITE(22, "Metabolite", true, false, false),
  ION(23, "Ion", true, false, false),
  BIOLOGICAL_INTERMEDIATE(24, "Biological Intermediate", true, false, false),

  DRUG_CLASS(30, "Drug Class", true, false, false),
  DRUG_COLLECTION(31, "Drug Collection", true, false, true),
  DRUG_COMPLEX(32, "Drug Complex", true, false, true),

  GENE_DRUG_COMPLEX(41, "Gene-Drug Complex", true, false, true),

  PHENOTYPE(51, "Phenotype", true, false, false),

  PATHWAY(61, "Pathway", true, false, false),
  PROCESS(62, "Process", false, true, false),

  PHYSICAL_ENTITY(70, "Physical Entity", false, true, false),
  DNA_ENTITY(71, "DNA Entity", false, false, false),
  RNA_ENTITY(72, "RNA Entity", false, false, false),

  NONHUMAN_GENE(81, "Non-human Gene", false, false, false),
  /** An indeterminate collection of genes. */
  GENE_GROUP(82, "Gene Group", false, false, false),
  ;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<NodeType> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private final boolean m_isAccessionRef;
  private final boolean m_isTermRef;
  private final boolean m_isCollection;


  /**
   * Standard constructor.
   */
  NodeType(int id, String displayName, boolean isAccessionRef, boolean isTermRef, boolean isCollection) {
    m_id = id;
    m_shortName = ExtendedEnumHelper.camelCaseFormat(displayName);
    m_displayName = displayName;
    m_isAccessionRef = isAccessionRef;
    m_isTermRef = isTermRef;
    m_isCollection = isCollection;
    init();
  }


  public boolean isAccessionRef() {
    return m_isAccessionRef;
  }

  public boolean isTermRef() {
    return m_isTermRef;
  }

  public boolean isCollection() {
    return m_isCollection;
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
  public static @Nullable NodeType lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable NodeType lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<NodeType> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<NodeType> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
