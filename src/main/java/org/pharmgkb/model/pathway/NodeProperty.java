package org.pharmgkb.model.pathway;

import java.util.Collection;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;
import org.pharmgkb.model.LinkOutResource;


/**
 * This is an enumeration of node properties.
 *
 * @author Mark Woon
 */
public enum NodeProperty implements ExtendedEnum {
  //PGKB_ID(11, "pgkbId", "PharmGKB ID"),
  //TYPE(12, "type", "Type"),

  ORGANISM(20, "organism", "Organism"),
  ORGANISM_ID(21, "organismId", "Organism ID"),
  CELL(22, "cell", "Cell", LinkOutResource.CL),
  CELLULAR_LOCATION(23, "cellularLocation", "Cellular Location", LinkOutResource.CELLULAR_LOCATION),
  TRANSMEMBRANE_TYPE(24, "transmembraneType", "Transmembrane Type", LinkOutResource.TRANSMEMBRANE_TYPES),

  ROLE(30, "role", "Role"),
  STATE(31, "state", "State"),
  MODIFICATION(32, "modification", "Modification", LinkOutResource.PATHWAY_MODIFICATION),
  MODIFICATION_POSITION(33, "modification.position", "Modification Position"),

  GENE_SYMBOL(40, "geneSymbol", "Gene Symbol"),
  GENE_XREF(41, "geneXref", "Gene Cross-reference"),

  //GENE_COMPLEX_COMPONENTS(50, "geneComplexComponents", "Gene Complex Components", false, true),
  //DRUG_COMPLEX_COMPONENTS(51, "drugComplexComponents", "Drug Complex Components", false, true),
  //MEMBER_GENES(52, "memberGenes", "Member Genes", false, true),
  //MEMBER_DRUGS(53, "memberDrugs", "Member Drugs", false, true),
  ;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<NodeProperty> s_extendedEnumHelper;
  private final int m_id;
  private final String m_shortName;
  private final String m_displayName;
  private @Nullable LinkOutResource m_ontologyResource;


  NodeProperty(int id, String shortName, String displayName, LinkOutResource ontologyResource) {
    this(id, shortName, displayName);
    Preconditions.checkArgument(ontologyResource.isOntology());
    m_ontologyResource = ontologyResource;
  }

  NodeProperty(int id, String shortName, String displayName) {
    m_id = id;
    m_shortName = shortName;
    m_displayName = displayName;
    init();
  }


  public boolean isOntologyType() {
    return m_ontologyResource != null;
  }

  public @Nullable LinkOutResource getOntologyResource() {
    return m_ontologyResource;
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
  public static @Nullable NodeProperty lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable NodeProperty lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<NodeProperty> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<NodeProperty> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
