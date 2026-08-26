package org.pharmgkb.pathvisio;

import java.awt.Color;
import java.util.Collection;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.Transport;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.view.MIMShapes;
import org.pharmgkb.common.util.ExtendedEnum;
import org.pharmgkb.common.util.ExtendedEnumHelper;
import org.pharmgkb.model.pathway.InteractionClassification;


/**
 * This is an enumeration of supported BioPax interactions.
 * Entries here should link to {@link InteractionClassification} if they should be stored in the database.
 * <p>
 * Changes here should also be reflected in properties.xml.
 *
 * @author Rebecca Tang
 * @author Mark Woon
 */
public enum BiopaxInteractionType implements ExtendedEnum {
  // conversions
  CONVERSION(100, InteractionClassification.CONVERSION, Conversion.class,
      false, true,
      new Color(13, 36, 237), LineType.LINE, MIMShapes.MIM_CONVERSION, LineStyle.SOLID),
  BIOCHEMICAL_REACTION(105, InteractionClassification.BIOCHEMICAL_REACTION, BiochemicalReaction.class,
      false, true,
      new Color(0, 0, 0), LineType.LINE, LineType.ARROW, LineStyle.SOLID),
  COMPLEX_ASSEMBLY(110,  InteractionClassification.COMPLEX_ASSEMBLY, ComplexAssembly.class,
      false, true,
      new Color(9, 155, 170), LineType.LINE, LineType.ARROW, LineStyle.SOLID),
  DEGRADATION(115,  InteractionClassification.DEGRADATION, Degradation.class,
      false, true,
      new Color(39, 229, 5), LineType.LINE, MIMShapes.MIM_CLEAVAGE, LineStyle.SOLID),
  TRANSPORT(120, InteractionClassification.TRANSPORT, Transport.class,
      false, true,
      new Color(201, 5, 201), LineType.LINE, MIMShapes.MIM_MODIFICATION, LineStyle.SOLID),

  // controls
  ACTIVATION(205, InteractionClassification.ACTIVATION, Control.class,
      true, false,
      new Color(255, 130, 6), LineType.LINE, LineType.ARROW, LineStyle.SOLID),
  CATALYSIS(210,  InteractionClassification.CATALYSIS, Catalysis.class,
      true, false,
      new Color(102, 102, 102), LineType.LINE, MIMShapes.MIM_CATALYSIS, LineStyle.SOLID),
  INHIBITION(215,  InteractionClassification.INHIBITION, Control.class,
      true, false,
      new Color(198, 6, 34), LineType.LINE, LineType.TBAR, LineStyle.SOLID),
  @Deprecated
  TEMPLATE_REACTION_REGULATION(225, "templateReactionRegulation", "Template Reaction Regulation", null,
      true, false,
      new Color(150,165,4), LineType.LINE, MIMShapes.MIM_CATALYSIS, LineStyle.SOLID),
  // pseudo-controls
  LEADS_TO(250,  InteractionClassification.LEADS_TO, Control.class,
      true, false,
      new Color(150,165,4), LineType.LINE, LineType.ARROW, LineStyle.DASHED),
  COMES_FROM(251,  InteractionClassification.COMES_FROM, Control.class,
      true, false,
      new Color(239,194,6), LineType.LINE, LineType.ARROW, LineStyle.DASHED),

  @Deprecated
  INFO_LABEL_LINE(300, "infoLine", "Info Line", null,
      false, false,
      new Color(239,194,6), LineType.LINE, LineType.LINE, LineStyle.SOLID),
	TEMPLATE_REACTION(305, InteractionClassification.TEMPLATE_REACTION, TemplateReaction.class,
      false, true,
      new Color(150,165,4), LineType.LINE, LineType.ARROW, LineStyle.SOLID),

  // used to add additional nodes to an interaction
  ADDITIONAL_NODE(400, "additionalNode", "Additional Node", null,
      false, false,
      new Color(100, 50, 100), LineType.LINE, MIMShapes.MIM_STIMULATION, LineStyle.SOLID),

  // only used in BioPax output, as a marker, when splitting control into control and interaction
  SUBINTERACTION(500, "subinteraction", "Sub-Interaction", Interaction.class,
      false, false,
      new Color(200, 200, 100), LineType.LINE, MIMShapes.MIM_TRANSLATION, LineStyle.DASHED)
	;

  private static ExtendedEnumHelper<BiopaxInteractionType> s_extendedEnumHelper;
  private static EnumProperty s_interactionProperty;
  private final int m_id;
	private final String m_shortName;
	private final String m_displayName;
  private final Class<? extends Interaction> m_biopaxClass;
  private InteractionClassification m_type;
  private final boolean m_isControl;
  private final boolean m_isConversion;
	private final Color m_color;
	private final LineType m_startLineType;
	private final LineType m_endLineType;
	private final int m_lineStyle;

  BiopaxInteractionType(int id, InteractionClassification type,
      Class<? extends Interaction> biopaxClass,
      boolean isControl, boolean isConversion,
      Color color, LineType startLineType, LineType endLineType, int lineStyle) {
    this(id, type.getShortName(), type.getDisplayName(), biopaxClass, isControl, isConversion,
        color, startLineType, endLineType, lineStyle);
    m_type = type;
  }


	BiopaxInteractionType(int id, String shortName, String displayName,
      @Nullable Class<? extends Interaction> biopaxClass,
      boolean isControl, boolean isConversion,
      Color color, LineType startLineType, LineType endLineType, int lineStyle) {

    m_id = id;
		m_shortName = shortName;
		m_displayName = displayName;
    m_biopaxClass = biopaxClass;
    m_isControl = isControl;
    m_isConversion = isConversion;
		m_color = color;
		m_startLineType = startLineType;
		m_endLineType = endLineType;
		m_lineStyle = lineStyle;
		init();
	}

  public @Nullable Class<? extends Interaction> getBiopaxClass() {
    return m_biopaxClass;
  }

  public InteractionClassification getType() {
    return m_type;
  }

  public boolean isControlType() {
    return m_isControl;
  }

  public boolean isConversionType(){
    return m_isConversion;
  }

  public Color getColor(){
		return m_color;
	}

	public LineType getStartLineStyle(){
		return m_startLineType;
	}

	public LineType getEndLineStyle(){
		return m_endLineType;
	}

	public int getLineStyle(){
		return m_lineStyle;
	}

	public static EnumProperty getProperty() {
		return s_interactionProperty;
	}


  //-- BEGIN ExtendedEnum methods --//
  private synchronized void init() {
    if (s_extendedEnumHelper == null) {
      s_extendedEnumHelper = new ExtendedEnumHelper<>(getClass());
      PropertyType type = new ReadOnlyPropertyType(new SimplePropertyType(DynamicProperty.INTERACTION_TYPE.getShortName()));
      s_interactionProperty = new EnumProperty(DynamicProperty.INTERACTION_TYPE.getShortName(), "Interaction Type",
          "BioPAX Interaction Type", 11, null, type, false);
    }
    s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName);
    s_interactionProperty.addValue(m_displayName);
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
  public static @Nullable BiopaxInteractionType lookupById(int id) {
    return s_extendedEnumHelper.lookupById(id);
  }

  public static BiopaxInteractionType lookupByName(String text) {
    BiopaxInteractionType type = s_extendedEnumHelper.lookupByName(text);
    if (type == null) {
      throw new IllegalArgumentException("Unknown interaction type: '" + text + "'");
    }
    return type;
  }

  public static Collection<BiopaxInteractionType> getAllSortedById() {
    return s_extendedEnumHelper.getAllSortedById();
  }

  public static Collection<BiopaxInteractionType> getAllSortedByName() {
    return s_extendedEnumHelper.getAllSortedByName();
  }
  //-- END ExtendedEnum statics --//
}
