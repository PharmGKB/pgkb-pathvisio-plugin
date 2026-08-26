package org.pharmgkb.pathvisio;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pharmgkb.common.util.UrlUtils;
import org.pharmgkb.exception.PgkbException;
import org.pharmgkb.model.AminoAcid;
import org.pharmgkb.model.LinkOutResource;


/**
 * This class validates a PathVisio {@link Pathway}.
 *
 * @author Mark Woon
 */
public class GpmlValidator {
  private final Pathway m_pathway;
  private final Set<String> m_warnings = new LinkedHashSet<>();
  private final Set<String> m_errors = new LinkedHashSet<>();
  private final Map<String, PathwayElement> m_nodeMap = new HashMap<>();
  private final Set<String> m_lineIds = new HashSet<>();
  private final Set<PathwayElement> m_displayOnly = new HashSet<>();


  private GpmlValidator(Pathway pathway) {
    Preconditions.checkNotNull(pathway);
    m_pathway = pathway;
  }


  public static class Builder {
    private Pathway m_pathway;

    public Builder forFile(Path gpmlFile) throws PgkbException {
      try {
        Logger.log.setLogLevel(false, false, false, true, true, true);
        // read GPML file
        m_pathway = new Pathway();
        m_pathway.readFromXml(gpmlFile.toFile(), true);
      } catch (ConverterException ex) {
        throw new PgkbException("Error parsing " + gpmlFile, ex);
      }
      return this;
    }

    public Builder forPathway(Pathway pathway) {
      m_pathway = pathway;
      return this;
    }

    public GpmlValidator build() {
      Preconditions.checkState(m_pathway != null, "Missing pathway");
      return new GpmlValidator(m_pathway);
    }
  }


  public Pathway getPathway() {
    return m_pathway;
  }

  public Set<String> getWarnings() {
    return m_warnings;
  }

  public Set<String> getErrors() {
    return m_errors;
  }



  public boolean validate() {

    validateMappInfo(m_pathway.getMappInfo());

    m_pathway.getDataObjects().stream()
        .filter(PathvisioUtils::isNode)
        .filter(pvElem -> PathvisioUtils.getPgkbType(pvElem).isDrawingOnly())
        .forEach(m_displayOnly::add);

    for (PathwayElement pvElem : m_pathway.getDataObjects()) {
      checkDynamicProperties(pvElem);
      checkComments(pvElem);
      switch (pvElem.getObjectType()) {
        case DATANODE:
          validateNode(pvElem);
          break;
        case LINE:
          validateLine(pvElem);
          break;
        default:
          // fall out
      }
    }

    Set<String> unlinkedNodeIds = new HashSet<>(m_nodeMap.keySet());
    m_lineIds.forEach(unlinkedNodeIds::remove);
    for (String id : unlinkedNodeIds) {
      PathwayElement node = m_nodeMap.get(id);
      PgkbType type = PathvisioUtils.getPgkbType(node);
      if (!type.isDrawingOnly()) {
        addError(node, "Not linked to anything", false);
      }
    }

    return m_errors.isEmpty() && m_warnings.isEmpty();
  }


  /**
   * Validate the pathway's MappInfo.
   */
  private void validateMappInfo(PathwayElement mappInfo) {

    String organism = StringUtils.stripToNull(mappInfo.getOrganism());
    if (organism == null) {
      addError(m_pathway.getMappInfo(), "Organism is undefined for pathway", false);
    } else if (!organism.toLowerCase().equals("homo sapiens")) {
      addError(m_pathway.getMappInfo(), "Organism is not homo sapiens", false);
    }

    String name = StringUtils.stripToNull(mappInfo.getMapInfoName());
    if (name == null || name.equalsIgnoreCase("new pathway")) {
      addError(m_pathway.getMappInfo(), "Please provide a name for this pathway", false);
    } else {
      name = name.toLowerCase();
      // must embed COE in pathway's name
      String coe = DynamicProperty.COE.of(mappInfo);
      if (coe != null) {
        if (coe.contains("PD") && !name.contains("pharmacodynamic")) {
          addError(m_pathway.getMappInfo(), "PD pathway doesn't have 'pharmacodynamic' in name", false);
        }
        if (coe.contains("PK") && !name.contains("pharmacokinetic")) {
          addError(m_pathway.getMappInfo(), "PK pathway doesn't have 'pharmacokinetic' in name", false);
        }
        if (coe.contains("ADR") && !name.contains("adverse drug reaction")) {
          addError(m_pathway.getMappInfo(), "ADR pathway doesn't have 'adverse drug reaction' in name", false);
        }
      } else {
        if (name.contains("pharmacodynamic")) {
          addError(m_pathway.getMappInfo(), "Pathway name has 'pharmacodynamic' in name but not marked as PD (see " +
              "Pathway Metadata section on wiki for details)", false);
        }
        if (name.contains("pharmacokinetic")) {
          addError(m_pathway.getMappInfo(), "Pathway name has 'pharmacokinetic' in name but not marked as PK (see " +
              "Pathway Metadata section on wiki for details)", false);
        }
      }
    }
  }


  private void validateNode(PathwayElement node) {

    PgkbType pgkbType = PathvisioUtils.getPgkbType(node);
    if (pgkbType.isDrawingOnly()) {
      return;
    }
    m_nodeMap.put(node.getGraphId(), node);

    checkAccessionId(node, pgkbType);
    checkLocation(node, pgkbType);
    checkModification(node, pgkbType);
    checkNodeHasNoEvidence(node);

    // validate based on type
    switch (pgkbType) {
      case BLACK_BOX:
        addError(node, "BlackBox should be converted to Phenotype or Process", false);
        break;
      case INFO_LABEL:
        addError(node, "Info labels are no longer allowed", false);
        break;
      case GENE_COLLECTION:
        validateCollection(node, DynamicProperty.MEMBER_GENES);
        break;
      case GENE_COMPLEX:
        validateCollection(node, DynamicProperty.GENE_COMPLEX_COMPONENTS);
        break;
      case GENE_DRUG_COMPLEX:
        validateCollection(node, DynamicProperty.GENE_COMPLEX_COMPONENTS);
        validateCollection(node, DynamicProperty.DRUG_COMPLEX_COMPONENTS);
        break;
      case DRUG_COLLECTION:
        validateCollection(node, DynamicProperty.MEMBER_DRUGS);
        break;
      case DRUG_COMPLEX:
        validateCollection(node, DynamicProperty.DRUG_COMPLEX_COMPONENTS);
        break;
      case DNA_ENTITY:
        // fall through
      case RNA_ENTITY:
        checkOrganism(node, pgkbType);
        break;
      case NONHUMAN_GENE:
        checkOrganism(node, pgkbType);
        if (DynamicProperty.GENE_SYMBOL.of(node) == null) {
          addError(node, "Gene symbol is required for non-human gene", false);
        }
        String geneXref = DynamicProperty.GENE_XREF.of(node);
        if (geneXref == null) {
          addWarning(node, "Missing gene xref on non-human gene");
        } else if (!UrlUtils.isValidWebUrl(geneXref)) {
          addError(node, "Gene xref is an invalid URL", false);
        }
        break;
      default:
        // fall out
    }
  }

  private void checkAccessionId(PathwayElement node, PgkbType type) {

    if (type.getAccObjName() != null) {
      String id = DynamicProperty.PGKB_ID.of(node);
      if (id == null) {
        addError(node, "Missing PharmGKB Accession ID.", false);
      }
    }
  }

  private void checkOrganism(PathwayElement node, PgkbType type) {
    if (DynamicProperty.ORGANISM.of(node) == null) {
      addError(node, "Organism is required for " + type.getDisplayName().toLowerCase(), false);
    }
    String organismId = DynamicProperty.ORGANISM_ID.of(node);
    if (organismId == null) {
      addWarning(node, "Missing organism ID on " + type.getDisplayName().toLowerCase());
    } else if (!organismId.matches("^\\d+$")) {
      addError(node, "Invalid NCBI Taxonomy format", false);
    }
  }

  private void checkLocation(PathwayElement node, PgkbType type) {

    PathwayElement container = null;
    for (PathwayElement dpOnly : m_displayOnly) {
      if (PathvisioUtils.isWithin(dpOnly, node)) {
        if (container != null) {
          if (dpOnly.getZOrder() > container.getZOrder()) {
            container = dpOnly;
          }
        } else {
          container = dpOnly;
        }

      }
    }

    String cellularLocation = DynamicProperty.CELLULAR_LOCATION.of(node);
    if (cellularLocation == null || cellularLocation.equalsIgnoreCase("Unknown")) {
      String msg = "Missing cellular location";
      switch (type) {
        case BLACK_BOX:
        case PHENOTYPE:
        case PROCESS:
          addWarning(node, msg);
          break;
        default:
          addError(node, msg, false);
      }
    } else {
      if (container != null) {
        PgkbType containerType = PathvisioUtils.getPgkbType(container);
        if (!cellularLocation.equalsIgnoreCase(containerType.getDisplayName())) {
          if (containerType == PgkbType.CELL) {
            // if container type is cell, we don't care if property is more specific
            if (cellularLocation.equalsIgnoreCase("Extracellular")) {
              addError(node, "Cellular location is '" + cellularLocation + "' but it's in a " + containerType.getDisplayName(), false);
            }
          } else {
            addError(node, "Cellular location is '" + cellularLocation + "' but it's in a " + containerType.getDisplayName(), false);
          }
        }
        // TODO(markwoon): check all CELL_TYPE in a container is the same
      }
      // check dependent transmembrane
      String transmembraneType = DynamicProperty.TRANSMEMBRANE_TYPE.of(node);
      if (cellularLocation.equals("Transmembrane")) {
        if (transmembraneType == null) {
          addError(node, "Missing transmembrane type", false);
        }
      } else {
        if (transmembraneType != null) {
          addError(node, "Should not have transmembrane type", true);
        }
      }
      // check dependent cell type
      String cellType = DynamicProperty.CELL.of(node);
      if ("NA".equalsIgnoreCase(cellType)) {
        addError(node, "Cell cannot be \"NA\".  Click on it, then either leave it blank or make a selection.",
            false);
        cellType = null;
      }
      if (cellularLocation.equals("Extracellular")) {
        if (cellType != null) {
          addError(node, "Extracellular nodes should not have cell specified", false);
        }
      } else {
        if (cellType == null) {
          addError(node, "Missing cell type", false);
        }
      }
    }
  }


  private void checkModification(PathwayElement node, PgkbType type) {

    String mod = DynamicProperty.MODIFICATION.of(node);
    if (mod != null) {
      if (mod.equals("Ubiquitination")) {
        if (type.isADrug()) {
          addError(node, mod + " is an invalid modification for a " + type, false);
        }
      }
      if (type.isAGene()) {
        String modPosition = DynamicProperty.MODIFICATION_POSITION.of(node);
        if (modPosition == null) {
          addError(node, "Missing modification position", false);
        } else {
          List<String> positions = Splitter.on(";").omitEmptyStrings().trimResults().splitToList(modPosition);
          positions.stream()
              .filter(pos -> !pos.equalsIgnoreCase("Unknown"))
              .forEach(pos -> {
                Matcher m = DataConstants.MODIFICATION_POSITION_PATTERN.matcher(pos);
                if (m.matches()) {
                  if (!m.group(1).equalsIgnoreCase("Unknown")) {
                    if (AminoAcid.lookupByName(m.group(1)) == null) {
                      addError(node, "Invalid modification AA format: '" + m.group(1) + "' in '" + pos + "'", false);
                    }
                  }
                } else {
                  addError(node, "Invalid modification format '" + pos + "'", false);
                }
              });
        }
      }
    } else {
      String modPosition = DynamicProperty.MODIFICATION_POSITION.of(node);
      if (modPosition != null) {
        addError(node, "Must specify modification if modification position is defined", false);
      }
    }
  }


  private void checkNodeHasNoEvidence(PathwayElement node) {

    for (PathwayElement.Comment comElem : node.getComments()) {
      String text = StringUtils.stripToNull(comElem.getComment());
      if (text == null) {
        continue;
      }
      Matcher m = LinkOutResource.MARKUP_PATTERN.matcher(text);
      if (m.matches()) {
        addError(node, "Evidence should only be provided in interactions (found in comment)", false);
      }
    }
    if (!PathvisioUtils.getPmids(node).isEmpty()) {
      addError(node, "Evidence should only be provided on interactions", false);
    }
  }


  private void validateCollection(PathwayElement collectionElement, DynamicProperty prop) {
    try {
      String termString = prop.of(collectionElement);
      Map<String, String> kvPairs = null;
      if (termString != null) {
        kvPairs = DataConstants.TERMS_SPLITTER.split(termString);
      }
      if (kvPairs == null || kvPairs.isEmpty()) {
        addError(collectionElement, PathvisioUtils.getPgkbType(collectionElement) + " must specify " + prop.getDisplayName(), false);
      }
    } catch (GpmlParseException ex) {
      addError(collectionElement, ex.getMessage(), true);
    }
  }


  private void validateLine(PathwayElement line) {

    // sometimes deleted lines linger around; when this happens getPathway() returns null and should be ignored
    if (line.getPathway() == null) {
      return;
    }

    BiopaxInteractionType type = PathvisioUtils.getInteractionType(line);
    if (type == BiopaxInteractionType.INFO_LABEL_LINE) {
      addError(line, "Info labels lines are no longer allowed", false);
      return;
    }
    if (type == BiopaxInteractionType.TEMPLATE_REACTION_REGULATION) {
      addError(line, "TemplateReactionRegulation lines are no longer allowed, use inhibition or activation instead", false);
      return;
    }

    if (!pointIsConnected(line, line.getMStart(), "start")) {
      return;
    }
    if (type == BiopaxInteractionType.DEGRADATION) {
      // degradation lines shouldn't have an end
      if (line.getMEnd().isLinked()) {
        addError(line, "Degradation interactions cannot end on anything", false);
      }
    } else if (!pointIsConnected(line, line.getMEnd(), "end")) {
      return;
    }
    if (type.isConversionType() && PathvisioUtils.isDependentLine(line)) {
      addError(line, "Conversion interactions must connect two objects", false);
    }

    // validation for node-to-node interactions
    if (PathvisioUtils.isPrimaryLine(line)) {
      PathwayElement startNode = PathvisioUtils.getTarget(line.getMStart());
      PgkbType startType = PathvisioUtils.getPgkbType(startNode);

      if (type == BiopaxInteractionType.DEGRADATION) {
        if (!startType.isAGene() && !startType.isADrug()) {
          addError(line, "Can only degrade gene/drug", false);
        }
        return;
      }

      PathwayElement endNode = PathvisioUtils.getTarget(line.getMEnd());
      PgkbType endType = PathvisioUtils.getPgkbType(endNode);

      if (type != BiopaxInteractionType.LEADS_TO) {
        if (endType == PgkbType.PATHWAY || endType == PgkbType.PROCESS) {
          addWarning(line, "Should use 'Leads To' interaction if end node is pathway or process");
        }
      }

      switch (type) {
        case ADDITIONAL_NODE:
          addError(line, "Can only add additional nodes to interactions", false);
          break;
        case CATALYSIS:
          addError(line, "Catalysis interactions can only be applied to other interactions", false);
          break;
        case SUBINTERACTION:
          addError(line, "What?  I don't even.  How?!?", true);
          return;

        case COMPLEX_ASSEMBLY:
          int numInputs = PathvisioUtils.getAdditionalInputs(line).size();
          int numOutputs = PathvisioUtils.getAdditionalOutputs(line).size();
          if (numInputs > 0 && numOutputs > 0) {
            addError(line, "ComplexAssembly cannot have multiple inputs (i.e. assembly) or multiple outputs " +
                "(i.e. disassembly), not both", false);
          } else if (numInputs == 0 && numOutputs == 0) {
            addError(line, "ComplexAssembly does not have additional nodes (i.e. nothing being assembled/disassembled)",
                false);
          }
          if ((startType.isADrug() && !endType.isADrug()) ||
              (startType.isAGene() && !endType.isAGene())) {
            addWarning(line, "Both ends of a ComplexAssembly are usually of the same type (i.e. both gene-types or " +
                "both drug-types)");
          }
          break;

        case TEMPLATE_REACTION:
          // must start with NucleicAcid
          if (!BiopaxConstants.NUCLEIC_ACID_TYPES.contains(startType)) {
            addError(line, "TemplateReaction must start with either a DNA or RNA", false);
          }
          // must end with "Gene-type"
          if (!endType.isAGene()) {
            addError(line, "TemplateReaction must end with a Gene-type node", false);
          }
          break;

        case TRANSPORT:
          String startLoc = DynamicProperty.CELL.of(startNode) + "." + DynamicProperty.CELLULAR_LOCATION.of(startNode);
          String endLoc = DynamicProperty.CELL.of(endNode) + "." + DynamicProperty.CELLULAR_LOCATION.of(endNode);
          if (startLoc.equals(endLoc)) {
            addWarning(line, "Transport did not change location of entity");
          }
          if (!startNode.getTextLabel().equals(endNode.getTextLabel())) {
            addError(line, "Transport does not have same entity on both ends", false);
          }
          break;

        default:
          // fall out
      }
    }

    // check anchored lines
    // must be a Control, and cannot start from line
    for (PathwayElement.MAnchor anchor : line.getMAnchors()) {
      anchor.getReferences().stream()
          .filter(PathwayElement.MPoint.class::isInstance)
          .forEach(graphRefContainer -> {
            PathwayElement.MPoint anchorPoint = (PathwayElement.MPoint)graphRefContainer;
            PathwayElement anchorLine = anchorPoint.getParent();
            // sometimes deleted lines linger around; when this happens getPathway() returns null and should be ignored
            if (anchorLine.getPathway() != null) {
              BiopaxInteractionType anchoredLineType = PathvisioUtils.getInteractionType(anchorLine);
              if (anchoredLineType == BiopaxInteractionType.ADDITIONAL_NODE) {
                if (PathvisioUtils.lineEndsAtAnchor(anchorLine.getMStart()) &&
                    PathvisioUtils.lineEndsAtAnchor(anchorLine.getMEnd())) {
                  addError(anchorLine, "Cannot start and end on interaction", false);
                } else {
                  if (anchorLine.getMEnd() == anchorPoint) {
                    // extra input
                    if (!PathvisioUtils.lineEndsAtNode(anchorLine.getMStart())) {
                      addError(anchorLine, "Must start with node, not interaction", false);
                    }
                    if (type == BiopaxInteractionType.TEMPLATE_REACTION) {
                      addError(anchorLine, "Can only have one input to a Template Reaction", false);
                    }
                  } else {
                    // extra output
                    if (!PathvisioUtils.lineEndsAtNode(anchorLine.getMEnd())) {
                      addError(anchorLine, "Must end with node, not interaction", false);
                    }
                    switch (type) {
                      case ACTIVATION:
                      case CATALYSIS:
                      case INHIBITION:
                        addError(anchorLine, "Cannot have additional outputs for control interactions", false);
                        break;
                      default:
                        // fall out
                    }
                  }
                }

              } else {
                if (!anchoredLineType.isControlType()) {
                  addError(anchorLine, "Only a control interaction can be anchored to another interaction", false);
                } else if (anchorLine.getMStart() == anchorPoint) {
                  addError(anchorLine, "Control interactions cannot start on an interaction", false);
                } else if (type.isControlType()) {
                  addError(anchorLine, "Cannot control a " + type.getDisplayName() + " interaction", false);
                }
              }
            }
          });
    }
    if (type == BiopaxInteractionType.TEMPLATE_REACTION) {
      // can only be regulated by ACTIVATION and INHIBITION
      PathvisioUtils.getAnchoredLines(line)
          .forEach(anchoredLine -> {
            BiopaxInteractionType linkedType = PathvisioUtils.getInteractionType(anchoredLine);
            if (linkedType != BiopaxInteractionType.ACTIVATION && linkedType != BiopaxInteractionType.INHIBITION) {
              addError(anchoredLine, "TemplateReaction can only be controlled by activation and inhibition", false);
            }
          });
    }

    checkLineHasEvidence(line);
  }

  /**
   * Makes sure the line's end point is connected to something legitimate.
   * Adds an error if it's not.
   */
  private boolean pointIsConnected(PathwayElement line, PathwayElement.MPoint point, String type) {

    if (point.isLinked()) {
      if (PathvisioUtils.lineEndsAtNode(point)) {
        PathwayElement target = m_pathway.getElementById(point.getGraphRef());
        if (PathvisioUtils.getPgkbType(target).isDrawingOnly()) {
          addError(target, "Drawing only elements cannot be used as interaction target", false);
          return false;
        } else {
          m_lineIds.add(point.getGraphRef());
        }
      } else if (!PathvisioUtils.lineEndsAtAnchor(point)) {
        addError(line, "Not linked to a valid " + type + " element", false);
        return false;
      }
    } else {
      addError(line, "Not linked to an " + type + " element", false);
      return false;
    }
    return true;
  }


  private static final Pattern sf_markupPattern = Pattern.compile("\\[\\s*(.+?)\\s*:\\s*(.+?)\\s*]");

  private void checkLineHasEvidence(PathwayElement line) {

    try {
      boolean hasEvidence = !PathvisioUtils.getPmids(line).isEmpty();

      for (PathwayElement.Comment comElem : line.getComments()) {
        String text = StringUtils.stripToNull(comElem.getComment());
        if (text == null) {
          continue;
        }
        Matcher m = sf_markupPattern.matcher(text);
        if (m.matches()) {
          LinkOutResource resource = LinkOutResource.lookupByName(m.group(1));
          if (resource == null) {
            addError(line, "Invalid resource '" + m.group(1) + "' in comment", false);
          } else if (!resource.validateResourceId(m.group(2))) {
            addError(line, "Invalid ID '" + m.group(2) + "' for " + resource.getDisplayName(), false);
          } else {
            hasEvidence = true;
          }
        }
      }

      if (!hasEvidence) {
        if (PathvisioUtils.getInteractionType(line) != BiopaxInteractionType.ADDITIONAL_NODE) {
          if (PathvisioUtils.isDependentLine(line)) {
            addWarning(line, "Missing evidence");
          } else {
            addError(line, "Missing evidence", false);
          }
        }
      } else {
        if (PathvisioUtils.getInteractionType(line) == BiopaxInteractionType.ADDITIONAL_NODE) {
          addError(line, "Evidence should go on primary interaction, not additional node interaction", false);
        }
      }

    } catch (GpmlParseException ex) {
      if (ex.getPathwayElement() != null) {
        addError(ex.getPathwayElement(), ex.getBaseMessage(), ex.isFatal());
      } else {
        addError(line, ex.getBaseMessage(), ex.isFatal());
      }
    }
  }


  /**
   * Make sure all dynamic properties are valid.
   * This is mainly to catch obsolete properties.
   */
  private void checkDynamicProperties(PathwayElement pvElem) {

    pvElem.getDynamicPropertyKeys().stream()
        .filter(key -> DynamicProperty.lookupByName(key) == null)
        .forEach(key -> addError(pvElem, "Unsupported dynamic property: '" + key + "'", true));
  }


  /**
   * A PathVisio comment should either be an xref/term or prefixed with "Curator Note:".
   */
  private void checkComments(PathwayElement pvElem) {
    for (PathwayElement.Comment comElem : pvElem.getComments()) {
      String text = StringUtils.stripToNull(comElem.getComment());
      if (text == null) {
        continue;
      }
      Matcher m = LinkOutResource.MARKUP_PATTERN.matcher(text);
      if (!m.matches()) {
        if (!text.toLowerCase().startsWith(DataConstants.CURATOR_NOTE_PREFIX)) {
          addWarning(pvElem, "Has comment that's neither a curator note nor an xref or term");
        }
      }
    }
  }



  private void addError(PathwayElement pvElem, String msg, boolean isFatal) {
    Preconditions.checkNotNull(pvElem);
    Preconditions.checkNotNull(msg);

    StringBuilder builder = new StringBuilder();
    if (pvElem.getObjectType() == ObjectType.LINE) {
      builder.append(PathvisioUtils.buildLineNameReference(pvElem));
    } else {
      builder.append(PathvisioUtils.buildElementNameReference(pvElem));
    }
    builder.append(": ")
        .append(msg);
    if (!msg.endsWith(".")) {
      builder.append(".");
    }
    if (isFatal) {
      builder.append(" (CONTACT DEV!)");
    }
    m_errors.add(builder.toString());
  }

  private void addWarning(PathwayElement pvElem, String msg) {
    Preconditions.checkNotNull(pvElem);
    Preconditions.checkNotNull(msg);

    StringBuilder builder = new StringBuilder();
    if (pvElem.getObjectType() == ObjectType.LINE) {
      builder.append(PathvisioUtils.buildLineNameReference(pvElem));
    } else {
      builder.append(PathvisioUtils.buildElementNameReference(pvElem));
    }
    builder.append(": ")
        .append(msg);
    if (!msg.endsWith(".")) {
      builder.append(".");
    }
    m_warnings.add(builder.toString());
  }
}
