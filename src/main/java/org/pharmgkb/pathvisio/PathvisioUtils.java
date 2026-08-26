package org.pharmgkb.pathvisio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pathvisio.core.biopax.BiopaxProperty;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.model.GraphLink;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;


/**
 * Utility methods for working with PathVisio.
 *
 * @author Mark Woon
 */
public class PathvisioUtils {

  /** Private constructor. */
  private PathvisioUtils() {
  }


  public static boolean isNode(PathwayElement pvElem) {
    return pvElem.getObjectType() == ObjectType.DATANODE;
  }

  public static boolean isLine(PathwayElement pvElem) {
    return pvElem.getObjectType() == ObjectType.LINE;
  }


  public static String buildNameReference(PathwayElement pvElem) {

    if (isNode(pvElem)) {
      return buildElementNameReference(pvElem);
    } else if (isLine(pvElem)) {
      return buildLineNameReference(pvElem);
    }
    throw new UnsupportedOperationException("Neither a node nor a line: " + pvElem.getGraphId());
  }


  /**
   * Generates a standard reference for a line.
   */
  public static String buildLineNameReference(PathwayElement line) {

    Preconditions.checkNotNull(line);
    String startGraphRef = line.getMStart().getGraphRef();
    String endGraphRef = line.getMEnd().getGraphRef();

    PathwayElement startNode = null;
    if (startGraphRef != null) {
      startNode = line.getPathway().getElementById(startGraphRef);
    }
    PathwayElement endNode = null;
    if (endGraphRef != null) {
      endNode = line.getPathway().getElementById(endGraphRef);
    }

    StringBuilder builder = new StringBuilder()
        .append(getInteractionType(line))
        .append(" line [id:")
        .append(line.getGraphId())
        .append("]");
    boolean started = false;
    if (startNode != null) {
      builder.append(" (from ")
          .append(buildElementNameReference(startNode));
      started = true;
    } else  if (startGraphRef != null) {
      builder.append(" (from anchor on ")
          .append(buildAnchorNameReference(line, startGraphRef));
      started = true;
    }
    if (endNode != null) {
      if (!started) {
        builder.append(" (from ");
        started = true;
      } else {
        builder.append(" to ");
      }
      builder.append(buildElementNameReference(endNode));
    } else if (endGraphRef != null) {
      if (!started) {
        builder.append(" (from anchor on ")
            .append(buildAnchorNameReference(line, endGraphRef));
        started = true;
      } else {
        builder.append(" to anchor on ")
            .append(buildAnchorNameReference(line, endGraphRef));
      }
    }
    if (started) {
      builder.append(")");
    }
    return builder.toString();
  }

  private static String buildAnchorNameReference(PathwayElement line, String anchorPointRef) {

    GraphLink.GraphIdContainer anchor = line.getPathway().getGraphIdContainer(anchorPointRef);
    if (anchor instanceof PathwayElement.MAnchor) {
      return buildLineNameReference(((PathwayElement.MAnchor)anchor).getParent());
    }
    throw new GpmlParseException(line, "Does not have an anchor");
  }


  /**
   * Generates a standard reference for an element.
   */
  public static String buildElementNameReference(PathwayElement element) {
    Preconditions.checkNotNull(element);
    if (element.getObjectType() == ObjectType.MAPPINFO) {
      return "[MAPPINFO]";
    }
    return "'" + StringUtils.stripToEmpty(element.getTextLabel()) + "' [id:" + element.getGraphId() + "]";
  }


  public static PgkbType getPgkbType(PathwayElement node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkArgument(isNode(node) || node.getObjectType() == ObjectType.MAPPINFO,
        "Expecting %s or %s, got %s", ObjectType.DATANODE, ObjectType.MAPPINFO, node.getObjectType());

    if (node.getObjectType() == ObjectType.MAPPINFO) {
      return PgkbType.PATHWAY;
    }
    String type = DynamicProperty.TYPE.of(node);
    if (type == null) {
      throw new GpmlParseException(node, "Missing entity type", true);
    }
    PgkbType t = PgkbType.lookupByName(type);
    if (t == null) {
      throw new GpmlParseException(node, "Unrecognized entity type '" + type + "'", true);
    }
    return t;
  }


  public static String getPgkbId(PathwayElement node) {
    Preconditions.checkNotNull(node);
    Preconditions.checkArgument(isNode(node) || node.getObjectType() == ObjectType.MAPPINFO,
        "Expecting %s or %s, got %s", ObjectType.DATANODE, ObjectType.MAPPINFO, node.getObjectType());

    String id = DynamicProperty.PGKB_ID.of(node);
    if (id == null) {
      throw new GpmlParseException("Missing ClinPGx ID for '" + buildElementNameReference(node) + "'");
    }
    return id;
  }


  public static BiopaxInteractionType getInteractionType(PathwayElement line) {
    Preconditions.checkNotNull(line);
    Preconditions.checkArgument(line.getObjectType() == ObjectType.LINE, "Expected line, got %s", line.getObjectType());

    String type = DynamicProperty.INTERACTION_TYPE.of(line);
    if (type == null) {
      throw new GpmlParseException("Missing dynamic property '" + DynamicProperty.INTERACTION_TYPE.getShortName() +
          "' from line [" + line.getGraphId() + "]");
    }
    return BiopaxInteractionType.lookupByName(type);
  }


  public static List<String> getBiopaxPropertyValues(@Nullable List<BiopaxProperty> props) {

    List<String> values = new ArrayList<>();
    if (props != null) {
      for (BiopaxProperty p : props) {
        String pmid = StringUtils.stripToNull(p.getContent(0).getValue());
        if (pmid != null) {
          values.add(pmid);
        }
      }
    }
    return values;
  }


  private static final Pattern sf_pmidPattern = Pattern.compile("^\\d+$");

  public static List<String> getPmids(PathwayElement pvElem) {

    Preconditions.checkNotNull(pvElem);

    List<String> pmids = new ArrayList<>();
    for (PublicationXref pubXref : pvElem.getBiopaxReferenceManager().getPublicationXRefs()) {
      String pmid = StringUtils.strip(pubXref.getPubmedId());
      if (!sf_pmidPattern.matcher(pmid).matches()) {
        throw new GpmlParseException(pvElem, "PublicationXref has invalid PMID: '" + pmid + "'", false);
      }
      pmids.add(pmid);
    }
    return pmids;
  }


  /**
   * Checks if the line is a primary line (i.e. connects nodes).
   */
  public static boolean isPrimaryLine(PathwayElement pvElem) {
    if (isLine(pvElem) && lineEndsAtNode(pvElem.getMStart())) {
      BiopaxInteractionType lineType = getInteractionType(pvElem);
      return lineType == BiopaxInteractionType.DEGRADATION || lineEndsAtNode(pvElem.getMEnd());
    }
    return false;
  }


  /**
   * Checks if the line is dependent (i.e. it links to another line).
   */
  public static boolean isDependentLine(PathwayElement line) {
    return lineEndsAtAnchor(line.getMStart()) || lineEndsAtAnchor(line.getMEnd());
  }

  /**
   * Checks if the line ends at a node.
   */
  public static boolean lineEndsAtNode(PathwayElement.MPoint point) {

    if (point.isLinked()) {
      PathwayElement target = point.getPathway().getElementById(point.getGraphRef());
      if (target != null && isNode(target)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the line ends at an anchor.
   */
  public static boolean lineEndsAtAnchor(PathwayElement.MPoint point) {
    if (point.isLinked()) {
      GraphLink.GraphIdContainer anchor = point.getPathway().getGraphIdContainer(point.getGraphRef());
      if (anchor instanceof PathwayElement.MAnchor) {
        return true;
      }
    }
    return false;
  }


  /**
   * Gets all lines anchored to the given {@code line}.
   */
  public static Set<PathwayElement> getAnchoredLines(PathwayElement line) {
    Preconditions.checkNotNull(line);
    Preconditions.checkArgument(line.getObjectType() == ObjectType.LINE);

    return line.getMAnchors().stream()
        .map(anchor -> anchor.getReferences())
        .flatMap(refs -> refs.stream()
            .filter(ref -> ref instanceof PathwayElement.MPoint)
            .map(ref -> ((PathwayElement.MPoint)ref).getParent())
        )
        // sometimes deleted lines linger around; when this happens getPathway() returns null and should be ignored
        .filter(l -> l.getPathway() != null)
        .collect(Collectors.toSet());
  }

  /**
   * Get all controllers of an interaction.
   */
  public static SortedSet<PathwayElement> getControllers(PathwayElement line) {
    Preconditions.checkNotNull(line);
    Preconditions.checkArgument(line.getObjectType() == ObjectType.LINE);

    return line.getMAnchors().stream()
        .map(anchor -> anchor.getReferences())
        .flatMap(refs -> refs.stream()
            .filter(ref -> ref instanceof PathwayElement.MPoint)
            .map(ref -> ((PathwayElement.MPoint)ref).getParent())
        )
        // sometimes deleted lines linger around; when this happens getPathway() returns null and should be ignored
        .filter(l -> l.getPathway() != null)
        .filter(l -> PathvisioUtils.getInteractionType(l).isControlType())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public static SortedSet<PathwayElement> getAdditionalInputs(PathwayElement line) {

    SortedSet<PathwayElement> additionalNodes = new TreeSet<>();
    for (PathwayElement.MAnchor anchor : line.getMAnchors()) {
      anchor.getReferences().stream()
          .filter(graphRefContainer -> graphRefContainer instanceof PathwayElement.MPoint)
          .forEach(graphRefContainer -> {
            PathwayElement.MPoint anchorPoint = (PathwayElement.MPoint)graphRefContainer;
            PathwayElement anchorLine = anchorPoint.getParent();
            BiopaxInteractionType anchoredLineType = PathvisioUtils.getInteractionType(anchorLine);
            if (anchoredLineType == BiopaxInteractionType.ADDITIONAL_NODE) {
              if (anchorLine.getMEnd() == anchorPoint) {
                additionalNodes.add(PathvisioUtils.getTarget(anchorLine.getMStart()));
              }
            }
          });
    }
    return additionalNodes;
  }

  public static SortedSet<PathwayElement> getAdditionalOutputs(PathwayElement line) {

    SortedSet<PathwayElement> additionalNodes = new TreeSet<>();
    for (PathwayElement.MAnchor anchor : line.getMAnchors()) {
      anchor.getReferences().stream()
          .filter(graphRefContainer -> graphRefContainer instanceof PathwayElement.MPoint)
          .forEach(graphRefContainer -> {
            PathwayElement.MPoint anchorPoint = (PathwayElement.MPoint)graphRefContainer;
            PathwayElement anchorLine = anchorPoint.getParent();
            BiopaxInteractionType anchoredLineType = PathvisioUtils.getInteractionType(anchorLine);
            if (anchoredLineType == BiopaxInteractionType.ADDITIONAL_NODE) {
              if (anchorLine.getMStart() == anchorPoint) {
                additionalNodes.add(PathvisioUtils.getTarget(anchorLine.getMEnd()));
              }
            }
          });
    }
    return additionalNodes;
  }

  public static SortedSet<PathwayElement> getAllInputs(PathwayElement line) {

    SortedSet<PathwayElement> additionalNodes = getAdditionalInputs(line);
    additionalNodes.add(getTarget(line.getMStart()));
    return additionalNodes;
  }


  public static SortedSet<PathwayElement> getAllOutputs(PathwayElement line) {

    SortedSet<PathwayElement> additionalNodes = getAdditionalOutputs(line);
    PathwayElement endNode = getTarget(line.getMEnd());
    if (isLine(endNode)) {
      if (!additionalNodes.isEmpty()) {
        throw new GpmlParseException(line, "Output elements cannot be a mix of nodes and interactions");
      }
    } else {
      for (PathwayElement e : additionalNodes) {
        if (!isNode(e)) {
          throw new GpmlParseException(line, "Output elements cannot be a mix of nodes and interactions");
        }
      }
    }
    additionalNodes.add(endNode);
    return additionalNodes;
  }


  public static boolean isComplexAssembly(PathwayElement line) {

    return getInteractionType(line) == BiopaxInteractionType.COMPLEX_ASSEMBLY
        && getAdditionalInputs(line).size() > 0;
  }

  public static boolean isComplexDisassembly(PathwayElement line) {

    return getInteractionType(line) == BiopaxInteractionType.COMPLEX_ASSEMBLY
        && getAdditionalOutputs(line).size() > 0;
  }



  /**
   * Gets what the point is connected to.
   */
  public static PathwayElement getTarget(PathwayElement.MPoint point) {

    PathwayElement elem = point.getPathway().getElementById(point.getGraphRef());
    if (elem != null && isNode(elem)) {
      return elem;
    }
    GraphLink.GraphIdContainer anchor = point.getPathway().getGraphIdContainer(point.getGraphRef());
    if (anchor instanceof PathwayElement.MAnchor) {
      return ((PathwayElement.MAnchor)anchor).getParent();
    }
    throw new IllegalArgumentException("Point is not connected to anything");
  }


  public static boolean isWithin(PathwayElement outer, PathwayElement inner) {

    double halfValue = outer.getMWidth() / 2;
    double outStartX = outer.getMCenterX() - halfValue;
    double outEndX = outer.getMCenterX() + halfValue;
    halfValue = outer.getMHeight() / 2;
    double outStartY = outer.getMCenterY() - halfValue;
    double outEndY = outer.getMCenterY() + halfValue;

    halfValue = inner.getMWidth() / 2;
    double inStartX = inner.getMCenterX() - halfValue;
    double inEndX = inner.getMCenterX() + halfValue;
    halfValue = inner.getMHeight() / 2;
    double inStartY = inner.getMCenterY() - halfValue;
    double inEndY = inner.getMCenterY() + halfValue;

    return outStartX < inStartX && inEndX < outEndX &&
        outStartY < inStartY && inEndY < outEndY;
  }
}
