package org.pharmgkb.pathvisio;

import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pharmgkb.common.util.PathUtils;
import org.pharmgkb.test.BasePgkbTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * This is a JUnit test for {@link PathvisioUtils}.
 *
 * @author Mark Woon
 */
class PathvisioUtilsTest implements BasePgkbTest {


  private Pathway readGpml(String fileName) throws Exception {

    Path file = PathUtils.getPathToResource(fileName);
    // read GPML file
    Pathway pvPathway = new Pathway();
    pvPathway.readFromXml(file.toFile(), true);
    return pvPathway;
  }


  @Test
  void testGetAdditionalInputs() throws Exception {

    String filename = "org/pharmgkb/pathvisio/GpmlValidatorTest-complexAssembly.gpml";
    Pathway pvPathway = readGpml(filename);

    for (PathwayElement pvElem : pvPathway.getDataObjects()) {
      if (PathvisioUtils.isPrimaryLine(pvElem)) {
        PathwayElement startElem = PathvisioUtils.getTarget(pvElem.getMStart());
        if (PathvisioUtils.getPgkbType(startElem) == PgkbType.GENE) {
          SortedSet<PathwayElement> inputs = PathvisioUtils.getAdditionalInputs(pvElem);
          assertEquals(1, inputs.size());
        } else {
          SortedSet<PathwayElement> outputs = PathvisioUtils.getAdditionalOutputs(pvElem);
          assertEquals(1, outputs.size());
        }
      }
    }
  }


  @Test
  void testGetAnchoredLines() throws Exception {

    String filename = "org/pharmgkb/pathvisio/GpmlValidatorTest-anchoredLines.gpml";
    Pathway pvPathway = readGpml(filename);
    for (PathwayElement pvElem : pvPathway.getDataObjects()) {
      if (PathvisioUtils.isPrimaryLine(pvElem)) {
        if (PathvisioUtils.getTarget(pvElem.getMStart()).getTextLabel().equals("abacavir")) {
          assertEquals(4, PathvisioUtils.getAnchoredLines(pvElem).size());
          return;
        }
      }
    }
    fail("Failed to find line");
  }


  @Test
  void testGetControllers() throws Exception {

    String filename = "org/pharmgkb/pathvisio/PathvisioUtilsTest-multipleControls-good.gpml";
    Pathway pvPathway = readGpml(filename);
    List<PathwayElement> primaryInteractions = pvPathway.getDataObjects().stream()
        .filter(PathvisioUtils::isPrimaryLine)
        .collect(Collectors.toList());
    assertEquals(1, primaryInteractions.size());
    assertEquals(6, PathvisioUtils.getControllers(primaryInteractions.get(0)).size());
  }
}
