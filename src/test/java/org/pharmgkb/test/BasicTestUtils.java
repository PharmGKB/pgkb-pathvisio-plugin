package org.pharmgkb.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pharmgkb.common.util.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Utility methods for writing tests.
 * <p>
 * This is an intentionally trimmed fork of pgkb-core's {@code org.pharmgkb.test.BasicTestUtils} for use by
 * pgkb-pathvisio-plugin's own test suite. It drops {@code parseXml()} and the {@code LogbackConstants} import
 * so this module doesn't need to duplicate those pgkb-core classes just to support test infra. This fork is
 * NOT expected to stay byte-for-byte identical to pgkb-core's copy — don't try to re-sync it to match.
 *
 * @author Mark Woon
 */
public class BasicTestUtils {
  private static final String MDC_LOG_KEY = "pgkb-logKey";
  private static final Logger sf_logger = LoggerFactory.getLogger("testOutput");
  private static final Logger sf_logOnlyLogger = LoggerFactory.getLogger("testOutputLogOnly");


  /**
   * Private constructor to prevent instantiation of utility class.
   */
  BasicTestUtils() {
  }


  /**
   * Initializes test: configures logging, disables Hibernate change notifications, adds shutdown hook.
   * Should be run in {@code @BeforeAll}.
   */
  static void initializeJUnitTest() {
    logDebug("Running base @BeforeAll");
  }


  public static void initializeLogging() {

    MDC.put(MDC_LOG_KEY, "test");
    // test classes should log at debug level
    enableDebugLogging(LoggerFactory.getLogger("org.pharmgkb.test"));
  }


  public static void enableDebugLogging(Logger logger) {
    ((ch.qos.logback.classic.Logger)logger).setLevel(Level.DEBUG);
  }


  /**
   * Gets {@link InputStream} to specified resource.
   *
   * @param resource an absolute path
   */
  public static BufferedReader getReaderToResource(String resource) {
    return getReaderToResource(BasicTestUtils.class, resource);
  }

  /**
   * Gets {@link InputStream} to specified resource.
   *
   * @param clz class to look up resource as
   * @param resource an absolute path or a one relative to {@code clz}
   */
  public static BufferedReader getReaderToResource(Class clz, String resource) {
    InputStream in = clz.getResourceAsStream(resource);
    if (in == null) {
      throw new IllegalArgumentException("Cannot find resource '" + resource + "'");
    }
    return new BufferedReader(new InputStreamReader(in));
  }


  /**
   * Checks that both {@link Collection}s have the same number of elements.
   *
   * @param property name of property being checked for debug purposes
   */
  public static void checkSize(@Nullable Collection orig, @Nullable Collection current, String property) {

    Boolean isOrigEmpty = orig == null || orig.isEmpty();
    Boolean isCurEmpty = current == null || current.isEmpty();

    if (!isOrigEmpty.equals(isCurEmpty)) {
      fail("original object " + (isOrigEmpty ? "does not have " : "has ") + property +
          " but new object " + (isCurEmpty ? "does not" : "does"));
    }
    if (!isOrigEmpty) {
      assertEquals(orig.size(), current.size(), "different number of elements in " + property);
    }
  }

  /**
   * Checks that both {@link Map}s have the same number of elements.
   *
   * @param property name of property being checked for debug purposes
   */
  public static void checkSize(@Nullable Map orig, @Nullable Map current, String property) {

    Boolean isOrigEmpty = orig == null || orig.isEmpty();
    Boolean isCurEmpty = current == null || current.isEmpty();

    if (!isOrigEmpty.equals(isCurEmpty)) {
      fail("original object " + (isOrigEmpty ? "does not have " : "has ") + property +
          " but new object " + (isCurEmpty ? "does not" : "does"));
    }
    if (!isOrigEmpty) {
      assertEquals(orig.size(), current.size(), "different number of elements in " + property);
    }
  }


  public static void compareReaders(BufferedReader readerA, BufferedReader readerB) {
    try {
      while (true) {
        String lineA = readerA.readLine();
        String lineB = readerB.readLine();
        if (lineA == null && lineB == null) {
          return;
        }
        assertEquals(lineA, lineB);
      }
    } catch (IOException ex) {
      fail(ex.getMessage());
    }
  }


  public static void compareStringArray(List<String> actualText, String... expectedText) {
    for (int x = 0; x < actualText.size(); x += 1) {
      String got = actualText.get(x);
      if (expectedText.length < x) {
        fail("Got more errors than expected");
      }
      String expected = expectedText[x];
      if (!got.equals(expected)) {
        assertEquals(expected, got);
      }
    }
    if (actualText.size() < expectedText.length) {
      StringBuilder builder = new StringBuilder();
      for (int x = actualText.size(); x < expectedText.length; x += 1) {
        builder.append(expectedText[x]);
      }
      assertEquals("", builder.toString(), "Got fewer lines than expected");
    }
  }


  public static void printMessages(Set<String> messages, boolean isWarning) {

    if (isWarning) {
      System.out.println("Warnings:");
    } else {
      System.out.println("Errors:");
    }
    messages.stream()
        .map(msg -> " * " + msg)
        .forEach(System.out::println);
  }


  public static Logger getLogger() {
    return sf_logger;
  }

  private static final String ENV_ANT_TESTS = "ANT_TESTS";

  private static Logger getPrintlnLogger() {
    String value = StringUtils.stripToNull(System.getenv(ENV_ANT_TESTS));
    if (value == null) {
      value = StringUtils.stripToNull(System.getProperty(ENV_ANT_TESTS));
    }
    if ("true".equalsIgnoreCase(value)) {
      return sf_logOnlyLogger;
    }
    return sf_logger;
  }


  public static void println() {
    getPrintlnLogger().info("");
  }

  public static void println(String msg) {
    getPrintlnLogger().info(msg);
  }

  public static void println(Number num) {
    getPrintlnLogger().info(num.toString());
  }

  public static void println(String fmt, Object... args) {
    getPrintlnLogger().info(fmt, args);
  }

  public static void logDebug(String fmt, Object... args) {
    getPrintlnLogger().debug(fmt, args);
  }

  public static void logWarning(String fmt, Object... args) {
    getLogger().warn(AnsiConsole.styleWarning(fmt), args);
  }

  public static void logError(String fmt, Object... args) {
    getLogger().error(AnsiConsole.styleError(fmt), args);
  }


  /**
   * Assert equality of {@link LocalDateTime}s down to second precision.
   */
  public static void assertEqualLocalDateTimes(LocalDateTime expected, LocalDateTime actual) {
    if (expected == null || actual == null) {
      if (expected == null && actual == null) {
        return;
      }
      // this will fail
      assertEquals(expected, actual);
    }

    Objects.requireNonNull(expected);
    Objects.requireNonNull(actual);

    // compare date portion
    assertEquals(expected.toLocalDate(), actual.toLocalDate());
    // compare time portion only to second precision
    if (expected.getHour() != actual.getHour() ||
        expected.getMinute() != actual.getMinute() ||
        expected.getSecond() != actual.getSecond()) {
      // this will fail
      assertEquals(expected, actual);
    }
  }


  /**
   * Checks if test is running in GitHub actions.
   * This is determined based on the <a
   * href="https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables">`CI`
   * environment variable on GH Actions</a>.
   */
  public static boolean isGithubActions() {
    return Boolean.parseBoolean(System.getenv("CI"));
  }

  /**
   * Checks if test is running in Jenkins
   */
  public static boolean isJenkins() {
    return System.getenv("JENKINS_URL") != null;
  }
}
