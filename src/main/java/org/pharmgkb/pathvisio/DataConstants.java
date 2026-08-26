package org.pharmgkb.pathvisio;

import java.util.regex.Pattern;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;


/**
 * Constants used in both PathVisio (in comments) and ClinPGx's version of BioPax Level 3.
 *
 * @author Mark Woon
 */
public class DataConstants {

  // for comment formatting
  // sample usage: "pgkb.memberGenes == PA298::RRM1\tPA299::RRM2\tPA34866::RRM2B"
  public static final String COMMENT_SEPARATOR = "==";
  private static final String COMMENT_VALUE_SEPARATOR = "\t";
  private static final String COMMENT_VALUE_KEY_VALUE_SEPARATOR = "::";

  public static final Joiner.MapJoiner TERMS_JOINER = Joiner.on(DataConstants.COMMENT_VALUE_SEPARATOR)
      .withKeyValueSeparator(DataConstants.COMMENT_VALUE_KEY_VALUE_SEPARATOR);
  public static final Splitter.MapSplitter TERMS_SPLITTER = Splitter.on(DataConstants.COMMENT_VALUE_SEPARATOR)
      .trimResults().omitEmptyStrings()
      .withKeyValueSeparator(DataConstants.COMMENT_VALUE_KEY_VALUE_SEPARATOR);

  public static final String CURATOR_NOTE_PREFIX = "curator note:";


  public static final String PGKB_PGKB_ID_UNKNOWN = "PA_Num_not_Available";

  // dynamic property keys for MAPPINFO
  // should be using DynamicProperty instead
  @Deprecated
  public static final String PGKB_SUB_ID = "pgkb.subId";
  @Deprecated
  public static final String PGKB_COE = "pgkb.coe";
  @Deprecated
  public static final String PGKB_TYPE = "pgkb.type";
  @Deprecated
  public static final String PGKB_ROLE = "pgkb.role";
  @Deprecated
  public static final String PGKB_STATE = "pgkb.state";
  @Deprecated
  public static final String PGKB_CELL = "pgkb.cell";
  @Deprecated
  public static final String PGKB_CELLULAR_LOCATION = "pgkb.cellularLocation";
  @Deprecated
  public static final String PGKB_TRANSMEMBRANE_TYPE = "pgkb.transmembraneTypes";
  @Deprecated
  public static final String PGKB_GENE_COMPLEX_COMPONENTS = "pgkb.geneComplexComponents";
  @Deprecated
  public static final String PGKB_DRUG_COMPLEX_COMPONENTS = "pgkb.drugComplexComponents";
  @Deprecated
  public static final String PGKB_MEMBER_GENES = "pgkb.memberGenes";
  // dynamic property keys for LINE
  @Deprecated
  public static final String PGKB_BIOPAX_INTERACTION_TYPE = "pgkb.biopaxInteractionType";
  @Deprecated
  public static final String PGKB_REACTION_NAME = "pgkb.reactionName";
  @Deprecated
  public static final String PGKB_LINE_STRENGTH = "pgkb.lineStrength";

  @Deprecated
  public static final String BLACK_BOX_PATHWAY_PREFIX = "Black Box: ";

  public static final Pattern MODIFICATION_POSITION_PATTERN = Pattern.compile("^(.+?):(\\d+)$");


}
