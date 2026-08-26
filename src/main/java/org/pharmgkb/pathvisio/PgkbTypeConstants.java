package org.pharmgkb.pathvisio;

import java.awt.Color;
import org.pathvisio.core.model.DataNodeType;


/**
 * Constants used by {@link PgkbType}.
 *
 * @author Mark Woon
 */
public class PgkbTypeConstants {
  // DataNodeTypes
  public static final DataNodeType SMALL_MOLECULE_TYPE = DataNodeType.create("Small Molecule");
  public static final DataNodeType DRUG_CLASS_TYPE = DataNodeType.create("Drug Class");

  public static final DataNodeType COLLECTION_TYPE = DataNodeType.create("Collection");
  public static final DataNodeType COMPLEX_TYPE = DataNodeType.create("Complex");
  public static final DataNodeType GROUP_TYPE = DataNodeType.create("Group");

  public static final DataNodeType PROCESS_TYPE = DataNodeType.create("Process");

  public static final DataNodeType PHENOTYPE_TYPE = DataNodeType.create("Phenotype");

  public static final DataNodeType PHYSICAL_ENTITY_TYPE = DataNodeType.create("Physical Entity");


  // Colors
  public static final Color GENE_COLOR = new Color(16, 122, 249);
  public static final Color GENE_FILL_COLOR = new Color(171, 205, 249);
  public static final Color GENE_ALT_FILL_COLOR = new Color(197, 252, 249);

  public static final Color DRUG_COLOR = new Color(119, 44, 127);
  public static final Color DRUG_ALT_COLOR = new Color(167, 61, 178);
  public static final Color DRUG_FILL_COLOR = new Color(197, 132, 203);
  public static final Color DRUG_ALT_FILL_COLOR = new Color(197, 158, 203);

  public static final Color PROCESS_COLOR = new Color(252, 133, 16);

  public static final Color PHYSICAL_ENTITY_COLOR = new Color(36, 126, 16);

  public static final Color FREE_TEXT_COLOR = new Color(239, 194, 6);
  public static final Color FREE_TEXT_FILL_COLOR =  new Color(102, 102, 102);

  public static final Color LOCATION_COLOR = new Color(0, 101, 0);
  public static final Color LOCATION_FILL_COLOR = new Color(236, 236, 236);
  public static final Color SUBLOCATION_FILL_COLOR = new Color(209, 209, 209);
  public static final Color SUBLOCATION_ALT_FILL_COLOR = new Color(236, 236, 209);
}
