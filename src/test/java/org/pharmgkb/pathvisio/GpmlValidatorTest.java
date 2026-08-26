package org.pharmgkb.pathvisio;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pharmgkb.common.util.PathUtils;
import org.pharmgkb.test.BasePgkbTest;
import org.pharmgkb.test.BasicTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is a JUnit test for {@link GpmlValidator}.
 *
 * @author Mark Woon
 */
class GpmlValidatorTest implements BasePgkbTest {


  @BeforeAll
  static void beforeClass() {
    org.pathvisio.core.debug.Logger.log.setLogLevel(false, false, false, true, true, true);
  }


  @Test
  void testAdditionalNodes() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-additionalNodes.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(0, warnings.size());

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(2, errors.size());
    assertTrue(errors.remove("Additional Node line [id:bbc52] (from anchor on Inhibition line [id:edaf0] " +
        "(from 'A1BG' [id:b02c7] to anchor on Biochemical Reaction line [id:c837c] (from 'abacavir' [id:fdb74] to " +
        "'bacampicillin' [id:d1bb5])) to 'B2M' [id:e3b91]): Cannot have additional outputs for control interactions."));
    assertTrue(errors.remove("Additional Node line [id:ecce6] (from anchor on Biochemical Reaction line [id:c837c] (" +
        "from 'abacavir' [id:fdb74] to 'bacampicillin' [id:d1bb5]) to anchor on Degradation line [id:c67a1] (from " +
        "'cabazitaxel' [id:c8f7c])): Cannot start and end on interaction."));
  }


  @Test
  void testAnchoredLines() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-anchoredLines.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(0, warnings.size());

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(5, errors.size());
    assertTrue(errors.remove("Conversion line [id:id30002c9f] (from 'B2M' [id:ed68f] to anchor on Biochemical Reaction line [id:e1203] " +
        "(from 'abacavir' [id:f2a04] to 'bacampicillin' [id:fe7d7])): Conversion interactions must connect two objects."));
    assertTrue(errors.remove("Conversion line [id:id30002c9f] (from 'B2M' [id:ed68f] to anchor on Biochemical Reaction line [id:e1203] " +
        "(from 'abacavir' [id:f2a04] to 'bacampicillin' [id:fe7d7])): Only a control interaction can be anchored to another interaction."));
    assertTrue(errors.remove("Conversion line [id:ide652d292] (from 'A1BG' [id:a9641] to anchor on Biochemical Reaction line [id:e1203] " +
        "(from 'abacavir' [id:f2a04] to 'bacampicillin' [id:fe7d7])): Conversion interactions must connect two objects."));
    assertTrue(errors.remove("Conversion line [id:ide652d292] (from 'A1BG' [id:a9641] to anchor on Biochemical Reaction line [id:e1203] " +
        "(from 'abacavir' [id:f2a04] to 'bacampicillin' [id:fe7d7])): Only a control interaction can be anchored to another interaction."));
    assertTrue(errors.remove("Inhibition line [id:a36d1] (from anchor on Biochemical Reaction line [id:e1203] " +
        "(from 'abacavir' [id:f2a04] to 'bacampicillin' [id:fe7d7]) to 'DAAM1' [id:e11b9]): Control interactions cannot start on an interaction."));
  }



  @Test
  void testCellularLocation() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-cellularLocation.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(1, warnings.size());
    assertTrue(warnings.remove("Transport line [id:ddebb] (from 'B2M' [id:f79dc] to 'B2M' [id:c7155]): Transport did not change location of entity."));

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(2, errors.size());
    assertTrue(errors.remove("'B2M' [id:f79dc]: Cellular location is 'Intracellular' but it's in a Nucleus."));
    assertTrue(errors.remove("Transport line [id:a5a9a] (from 'A1BG' [id:dab27] to 'CA1' [id:e4baf]): Transport does not have same entity on both ends."));
  }


  @Test
  void testComment() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-comment-good.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    boolean isValid = validator.validate();

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);

    assertTrue(isValid);
  }


  @Test
  void testDegradation() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-degradation-good.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    boolean isValid = validator.validate();

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);

    assertTrue(isValid);
  }


  @Test
  void testEvidence() throws Exception {

    URL url = getClass().getResource("GpmlValidatorTest-evidence.gpml");
    Path file = Paths.get(url.toURI());
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(2, warnings.size());
    assertTrue(warnings.remove("Activation line [id:de6eb] (from 'A1BG' [id:a22ed] to anchor on Biochemical Reaction line [id:cbe8c] (from 'doxorubicin' [id:a99e8] to 'doxorubicinol' [id:d2878])): Missing evidence."));
    assertTrue(warnings.remove("'doxorubicinol' [id:d2878]: Has comment that's neither a curator note nor an xref or term."));

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(2, errors.size(), "Unexpected errors");
    assertTrue(errors.remove("'doxorubicin' [id:a99e8]: Evidence should only be provided in interactions (found in comment)."));
    assertTrue(errors.remove("'morphine' [id:a151d]: Evidence should only be provided on interactions."));
  }


  @Test
  void testGeneCollection() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-geneCollection.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    assertEquals(0, validator.getWarnings().size());

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(2, errors.size());
    assertTrue(errors.remove("'Empty Collection' [id:b9dbb]: Gene Collection must specify memberGenes."));
    assertTrue(errors.remove("'Empty Collection' [id:b9dbb]: Not linked to anything."));
  }


  @Test
  void testModification() throws Exception {

    URL url = getClass().getResource("GpmlValidatorTest-modification.gpml");
    Path file = Paths.get(url.toURI());
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    BasicTestUtils.printMessages(validator.getWarnings(), true);
    assertEquals(3, validator.getWarnings().size());
    assertEquals(0, (int)validator.getWarnings().stream()
        .filter(s -> !s.endsWith("Missing evidence."))
        .count());

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertTrue(errors.remove("'doxorubicinol' [id:d2878]: Ubiquitination is an invalid modification for a Drug."));
    assertTrue(errors.remove("'A1BG' [id:a22ed]: Invalid modification AA format: 'Tyt' in 'Tyt:44'."));
    assertTrue(errors.remove("'A1BG' [id:a22ed]: Invalid modification format 'Ddd'."));
    assertEquals(0, errors.size());
  }


  @Test
  void testNonHumanGene() throws Exception {

    URL url = getClass().getResource("GpmlValidatorTest-nonHumanGene.gpml");
    Path file = Paths.get(url.toURI());

    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(2, warnings.size());
    assertThat(warnings, contains(
        "'NHG3' [id:cbd71]: Missing gene xref on non-human gene.",
        "'NHG2' [id:cfb24]: Missing organism ID on non-human gene."
    ));

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(4, errors.size());
    assertThat(errors, contains(
        "'NHG1' [id:c4a21]: Gene symbol is required for non-human gene.",
        "'NHG4' [id:c6f20]: Organism is required for non-human gene.",
        "'NHG4' [id:c6f20]: Gene xref is an invalid URL.",
        "'NHG3' [id:cbd71]: Invalid NCBI Taxonomy format."
    ));
  }


  @Test
  void testPhysicalEntity() throws Exception {

    URL url = getClass().getResource("GpmlValidatorTest-physicalEntity-good.gpml");
    Path file = Paths.get(url.toURI());

    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    boolean isValid = validator.validate();

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);

    assertTrue(isValid);
  }


  @Test
  void testPseudoControlsGood() throws Exception {

    Path file = PathUtils.getPathToResource("org/pharmgkb/pathvisio/GpmlValidatorTest-pseudoControls-good.gpml");
    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    boolean isValid = validator.validate();

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);

    assertTrue(isValid);
  }


  @Test
  void testTemplateReaction() throws Exception {

    URL url = getClass().getResource("GpmlValidatorTest-templateReaction.gpml");
    Path file = Paths.get(url.toURI());

    GpmlValidator validator = new GpmlValidator.Builder().forFile(file).build();
    assertFalse(validator.validate());

    Set<String> warnings = validator.getWarnings();
    BasicTestUtils.printMessages(warnings, true);
    assertEquals(0, warnings.size());

    Set<String> errors = validator.getErrors();
    BasicTestUtils.printMessages(errors, false);
    assertEquals(5, errors.size());
    assertTrue(errors.remove("Conversion line [id:fa4fe] (from 'calcidiol' [id:a8f8f] to anchor on Template Reaction " +
        "line [id:e3e42] (from 'A2M DNA' [id:c57e6] to 'A2M' [id:a46b8])): Only a control interaction can be anchored to another interaction."));
    assertTrue(errors.remove("Catalysis line [id:d0e2d] (from 'calcidiol' [id:a8f8f] to anchor on Template Reaction " +
        "line [id:e3e42] (from 'A2M DNA' [id:c57e6] to 'A2M' [id:a46b8])): TemplateReaction can only be controlled by activation and inhibition."));
    assertTrue(errors.remove("Conversion line [id:fa4fe] (from 'calcidiol' [id:a8f8f] to anchor on Template Reaction " +
        "line [id:e3e42] (from 'A2M DNA' [id:c57e6] to 'A2M' [id:a46b8])): TemplateReaction can only be controlled by activation and inhibition."));
    assertTrue(errors.remove("Conversion line [id:fa4fe] (from 'calcidiol' [id:a8f8f] to anchor on Template Reaction " +
        "line [id:e3e42] (from 'A2M DNA' [id:c57e6] to 'A2M' [id:a46b8])): Conversion interactions must connect two objects."));
    assertTrue(errors.remove("Template Reaction line [id:ae7c2] (from 'A2M' [id:a46b8] to 'A1BG' [id:da2c7]): " +
        "TemplateReaction must start with either a DNA or RNA."));
  }
}
