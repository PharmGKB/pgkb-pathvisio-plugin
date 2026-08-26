package org.pharmgkb.pathvisio;

import java.awt.Color;
import java.util.Collection;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.ShapeType;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;
import org.pharmgkb.model.pathway.NodeType;


/**
 * Enumeration of PharmGKB entity types.
 *
 * @author Rebecca Tang
 * @author Mark Woon
 */
public enum PgkbType implements ExtendedEnum {
	GENE(1, NodeType.GENE,
      "gene", DataNodeType.GENEPRODUCT, Protein.class, false,
      PgkbTypeConstants.GENE_COLOR, Color.WHITE, ShapeType.OVAL, LineStyle.SOLID),
  HAPLOTYPE(2, NodeType.HAPLOTYPE,
      "haplotype", DataNodeType.GENEPRODUCT, Protein.class, false,
      PgkbTypeConstants.GENE_COLOR, Color.WHITE, ShapeType.OVAL, LineStyle.DASHED),
  NONHUMAN_GENE(3, NodeType.NONHUMAN_GENE,
      null, DataNodeType.GENEPRODUCT, Protein.class, false,
      PgkbTypeConstants.GENE_COLOR, Color.PINK, ShapeType.OVAL, LineStyle.SOLID),

  GENE_GROUP(5, NodeType.GENE_GROUP,
      null, PgkbTypeConstants.GROUP_TYPE, Protein.class, false,
      Color.BLACK, PgkbTypeConstants.GENE_FILL_COLOR, ShapeType.OVAL, LineStyle.SOLID),
	GENE_COLLECTION(6, NodeType.GENE_COLLECTION,
      null, PgkbTypeConstants.COLLECTION_TYPE, Protein.class, true,
      Color.BLACK, PgkbTypeConstants.GENE_FILL_COLOR, ShapeType.OVAL, LineStyle.DASHED),
	GENE_COMPLEX(7, NodeType.GENE_COMPLEX,
      null, PgkbTypeConstants.COMPLEX_TYPE, Complex.class, false,
      Color.BLACK, PgkbTypeConstants.GENE_ALT_FILL_COLOR, ShapeType.OVAL, LineStyle.DASHED),
  GENE_DRUG_COMPLEX(8, NodeType.GENE_DRUG_COMPLEX,
      null, PgkbTypeConstants.COMPLEX_TYPE, Complex.class, false,
      PgkbTypeConstants.DRUG_COLOR, PgkbTypeConstants.GENE_ALT_FILL_COLOR, ShapeType.ROUNDED_RECTANGLE, LineStyle.DASHED),


  DRUG(11, NodeType.DRUG,
      "chemical", PgkbTypeConstants.SMALL_MOLECULE_TYPE, SmallMolecule.class, false,
      PgkbTypeConstants.DRUG_COLOR, Color.WHITE, ShapeType.RECTANGLE, LineStyle.SOLID),
  METABOLITE(12, NodeType.METABOLITE,
      "chemical", PgkbTypeConstants.SMALL_MOLECULE_TYPE, SmallMolecule.class, false,
      PgkbTypeConstants.DRUG_COLOR, Color.WHITE, ShapeType.RECTANGLE, LineStyle.DASHED),
  ION(13, NodeType.ION,
      "chemical", PgkbTypeConstants.SMALL_MOLECULE_TYPE, SmallMolecule.class, false,
      PgkbTypeConstants.DRUG_ALT_COLOR, Color.WHITE, ShapeType.RECTANGLE, LineStyle.DASHED),
  BIOLOGICAL_INTERMEDIATE(14, NodeType.BIOLOGICAL_INTERMEDIATE,
      "chemical", PgkbTypeConstants.SMALL_MOLECULE_TYPE, SmallMolecule.class, false,
      PgkbTypeConstants.DRUG_ALT_COLOR, Color.WHITE, ShapeType.RECTANGLE, LineStyle.DASHED),

  DRUG_CLASS(15, NodeType.DRUG_CLASS,
      "chemical", PgkbTypeConstants.DRUG_CLASS_TYPE, SmallMolecule.class, false,
      Color.BLACK, PgkbTypeConstants.DRUG_FILL_COLOR, ShapeType.RECTANGLE, LineStyle.SOLID),
	DRUG_COLLECTION(16, NodeType.DRUG_COLLECTION,
      null, PgkbTypeConstants.COLLECTION_TYPE, null, true,
      Color.BLACK, PgkbTypeConstants.DRUG_FILL_COLOR, ShapeType.RECTANGLE, LineStyle.DASHED),
	DRUG_COMPLEX(17, NodeType.DRUG_COMPLEX,
      null, PgkbTypeConstants.COMPLEX_TYPE, Complex.class, false,
      Color.BLACK, PgkbTypeConstants.DRUG_ALT_FILL_COLOR, ShapeType.RECTANGLE, LineStyle.DASHED),


  PHENOTYPE(21, NodeType.PHENOTYPE,
      "disease", PgkbTypeConstants.PHENOTYPE_TYPE, Pathway.class, false,
      Color.BLACK, Color.WHITE, ShapeType.ROUNDED_RECTANGLE, LineStyle.DASHED),


  @Deprecated
  BLACK_BOX(30, "Black Box", null,
      "blackBoxEvent", PgkbTypeConstants.PROCESS_TYPE, null, false,
      PgkbTypeConstants.FREE_TEXT_COLOR, PgkbTypeConstants.FREE_TEXT_FILL_COLOR, ShapeType.PENTAGON, LineStyle.SOLID),
  PATHWAY(31, NodeType.PATHWAY,
      "pathway", DataNodeType.PATHWAY, Pathway.class, false,
      PgkbTypeConstants.PROCESS_COLOR, Color.WHITE, ShapeType.PENTAGON, LineStyle.SOLID),
  PROCESS(32, NodeType.PROCESS,
      "ontologyTerm", PgkbTypeConstants.PROCESS_TYPE, Pathway.class, false,
      PgkbTypeConstants.PROCESS_COLOR, Color.WHITE, ShapeType.PENTAGON, LineStyle.DASHED),

  PHYSICAL_ENTITY(40, NodeType.PHYSICAL_ENTITY,
      null, PgkbTypeConstants.PHYSICAL_ENTITY_TYPE, Pathway.class, false,
      PgkbTypeConstants.PHYSICAL_ENTITY_COLOR, Color.WHITE, ShapeType.ROUNDED_RECTANGLE, LineStyle.SOLID),

  DNA_ENTITY(41, NodeType.DNA_ENTITY,
      null, DataNodeType.RNA, Dna.class, false,
      PgkbTypeConstants.PHYSICAL_ENTITY_COLOR, PgkbTypeConstants.GENE_FILL_COLOR, ShapeType.ROUNDED_RECTANGLE, LineStyle.SOLID),
  RNA_ENTITY(42, NodeType.RNA_ENTITY,
      null, DataNodeType.RNA, Rna.class, false,
      PgkbTypeConstants.PHYSICAL_ENTITY_COLOR, PgkbTypeConstants.GENE_ALT_FILL_COLOR, ShapeType.ROUNDED_RECTANGLE, LineStyle.SOLID),


  //-- BEGIN: DRAWING ONLY --//
  CELL(101, "Cell",
      Color.BLACK, PgkbTypeConstants.LOCATION_FILL_COLOR, ShapeType.ROUNDED_RECTANGLE, LineStyle.SOLID),
  @Deprecated
  DNA(102, "DNA",
      PgkbTypeConstants.GENE_COLOR, PgkbTypeConstants.SUBLOCATION_FILL_COLOR, ShapeType.HEXAGON, LineStyle.SOLID),
  @Deprecated
  RNA(103, "RNA",
      PgkbTypeConstants.GENE_COLOR, PgkbTypeConstants.SUBLOCATION_FILL_COLOR, ShapeType.HEXAGON, LineStyle.DASHED),
	NUCLEUS(104, "Nucleus",
      Color.RED,  PgkbTypeConstants.SUBLOCATION_FILL_COLOR, ShapeType.OVAL, LineStyle.SOLID),
	MITOCHONDRIA(105, "Mitochondria",
      PgkbTypeConstants.LOCATION_COLOR, PgkbTypeConstants.SUBLOCATION_FILL_COLOR, ShapeType.MITOCHONDRIA, LineStyle.SOLID),
  VESICLE(106, "Vesicle",
      PgkbTypeConstants.LOCATION_COLOR, PgkbTypeConstants.SUBLOCATION_FILL_COLOR, ShapeType.OVAL, LineStyle.SOLID),
  ENDOPLASMIC_RETICULUM(107, "Endoplasmic Reticulum",
      PgkbTypeConstants.LOCATION_COLOR, PgkbTypeConstants.SUBLOCATION_ALT_FILL_COLOR, ShapeType.ENDOPLASMICRETICULUM, LineStyle.SOLID),
  SARCOPLASMIC_RETICULUM(108, "Sarcoplasmic Reticulum",
      PgkbTypeConstants.LOCATION_COLOR, PgkbTypeConstants.SUBLOCATION_ALT_FILL_COLOR, ShapeType.SARCOPLASMICRETICULUM, LineStyle.SOLID),

  @Deprecated
	INFO_LABEL(111, "Info Label",
      PgkbTypeConstants.FREE_TEXT_COLOR, PgkbTypeConstants.FREE_TEXT_FILL_COLOR, ShapeType.ROUNDED_RECTANGLE, LineStyle.SOLID),
  //-- END: DRAWING ONLY --//
	;

  @SuppressWarnings("NotNullFieldNotInitialized")
  private static ExtendedEnumHelper<PgkbType> s_extendedEnumHelper;
  private static EnumProperty s_pgkbProperty;
  private int m_id;
	private String m_shortName;
	private String m_displayName;
  private @Nullable String m_notes;
	private Color m_color;
	private Color m_fillColor;
	private ShapeType m_shapeType;
	private int m_lineStyle;
	private DataNodeType m_dataNodeType;
  private @Nullable NodeType m_nodeType;
  private @Nullable String m_accObjName;
  private boolean m_isOntologyTerm;
  private boolean m_drawingOnly;
  private @Nullable Class<? extends Entity> m_biopaxClass;
  private boolean m_isCollection;


  /**
   * Constructor for drawing-only {@link PgkbType}.
   */
  PgkbType(int id, String displayName,
      Color color, Color fillColor, ShapeType shapeType, int lineStyle) {
    init(id, displayName, null, null, null, DataNodeType.UNKOWN, null, false,
        color, fillColor, shapeType, lineStyle, true);
  }

  PgkbType(int id, NodeType nodeType,
      @Nullable String accObjName, DataNodeType dataNodeType, @Nullable Class<? extends Entity> biopaxClass,
      boolean isCollection,
      Color color, Color fillColor, ShapeType shapeType, int lineStyle) {
    init(id, nodeType.getDisplayName(), null, nodeType, accObjName, dataNodeType, biopaxClass, isCollection,
        color, fillColor, shapeType, lineStyle, false);
  }

  @Deprecated
  PgkbType(int id, String displayName, @Nullable String notes,
      @Nullable String accObjName, DataNodeType dataNodeType, @Nullable Class<? extends Entity> biopaxClass,
      boolean isCollection,
      Color color, Color fillColor, ShapeType shapeType, int lineStyle) {
    init(id, displayName, notes, null, accObjName, dataNodeType, biopaxClass, isCollection,
        color, fillColor, shapeType, lineStyle, false);
  }

  private void init(int id, String displayName, @Nullable String notes,
      @Nullable NodeType nodeType,
      @Nullable String accObjName, DataNodeType dataNodeType, @Nullable Class<? extends Entity> biopaxClass,
      boolean isCollection,
      Color color, Color fillColor, ShapeType shapeType, int lineStyle, boolean drawingOnly) {

    m_id = id;
		m_shortName = ExtendedEnumHelper.camelCaseFormat(displayName);
		m_displayName = displayName;
    m_notes = notes;
    m_nodeType = nodeType;
    // we use name here instead of ObjectType because we don't want to link to ObjectType class
    m_accObjName = accObjName;
    if ("ontologyTerm".equals(m_accObjName)) {
      m_isOntologyTerm = true;
    }

		m_color = color;
		m_fillColor = fillColor;
		m_shapeType = shapeType;
		m_lineStyle = lineStyle;
    m_dataNodeType = dataNodeType;
    m_drawingOnly = drawingOnly;
    m_biopaxClass = biopaxClass;
    m_isCollection = isCollection;
    init();
	}


	public @Nullable
  NodeType getNodeType() {
	  return m_nodeType;
  }

  public @Nullable String getAccObjName() {
    return m_accObjName;
  }

  public boolean isOntologyTerm() {
    return m_isOntologyTerm;
  }


  public Color getColor() {
		return m_color;
	}

	public Color getFillColor() {
		return m_fillColor;
	}

	public ShapeType getShapeType() {
		return m_shapeType;
	}

	public int getLineStyle(){
		return m_lineStyle;
	}

  public boolean isDrawingOnly() {
    return m_drawingOnly;
  }

	public DataNodeType getDataNodeType() {
		return m_dataNodeType;
	}

  public @Nullable Class<? extends Entity> getBiopaxClass() {
    return m_biopaxClass;
  }


  public boolean isCollection() {
    return m_isCollection;
  }

	public static EnumProperty getProperty() {
		return s_pgkbProperty;
	}


  public boolean isAGene() {
    switch (this) {
      case GENE:
      case GENE_COLLECTION:
      case GENE_COMPLEX:
      case GENE_DRUG_COMPLEX:
      case GENE_GROUP:
      case HAPLOTYPE:
      case NONHUMAN_GENE:
        return true;
      default:
        return false;
    }
  }

  public boolean isADrug() {
    switch (this) {
      case BIOLOGICAL_INTERMEDIATE:
      case DRUG:
      case DRUG_CLASS:
      case DRUG_COLLECTION:
      case DRUG_COMPLEX:
      case ION:
      case METABOLITE:
        return true;
      default:
        return false;
    }
  }


  public @Nullable String getNotes() {
    return m_notes;
  }


  //-- BEGIN ExtendedEnum methods --//
  private synchronized void init() {
    //noinspection ConstantValue
    if (s_extendedEnumHelper == null) {
      s_extendedEnumHelper = new ExtendedEnumHelper<>(getClass());
      PropertyType type = new ReadOnlyPropertyType(new SimplePropertyType(DynamicProperty.TYPE.getShortName()));
      s_pgkbProperty = new EnumProperty(DynamicProperty.TYPE.getShortName(), "Entity Type", "PharmGKB Entity Type", 10,
          null, type, false);
    }
    s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName);
    s_pgkbProperty.addValue(m_displayName);
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
  public static @Nullable PgkbType lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static @Nullable PgkbType lookupByName(String text) {
    return s_extendedEnumHelper.lookupByName(text);
  }

  public static Collection<PgkbType> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<PgkbType> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
