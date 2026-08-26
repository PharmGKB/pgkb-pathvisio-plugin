package org.pharmgkb.test.junit;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.pharmgkb.common.util.AnsiConsole;
import org.pharmgkb.test.BasicTestUtils;


/**
 * This JUnit extension is responsible for pretty printing test results.
 *
 * @author Mark Woon
 */
public class TestResultLoggerExtension implements BeforeAllCallback, BeforeEachCallback, TestWatcher, AfterAllCallback {
  private static final Map<Class, TestStats> sf_processed = new HashMap<>();


  @Override
  public void beforeAll(ExtensionContext context) {
    BasicTestUtils.initializeLogging();
    BasicTestUtils.getLogger().info(AnsiConsole.colorize("------", AnsiConsole.ANSI_GREEN));
    BasicTestUtils.getLogger().info(AnsiConsole.colorize("TEST: {}", AnsiConsole.ANSI_GREEN), context.getRequiredTestClass().getName());
  }


  @Override
  public void beforeEach(ExtensionContext context) {
  }


  private String getTestName(ExtensionContext context) {
    String testName = context.getDisplayName();
    if (testName.endsWith("()")) {
      return testName.substring(0, testName.length() - 2);
    }
    return testName;
  }


  private TestStats status(ExtensionContext context) {
    return sf_processed.computeIfAbsent(context.getRequiredTestClass(), (cls) -> new TestStats());
  }


  @Override
  public void testSuccessful(ExtensionContext context) {
    status(context).succeeded();
    BasicTestUtils.getLogger().info(AnsiConsole.colorize("- {}", AnsiConsole.ANSI_CYAN), getTestName(context));
  }

  @Override
  public void testDisabled(ExtensionContext context, Optional<String> reason) {
    status(context).disabled();

    StringBuilder builder = new StringBuilder()
        .append("- ")
        .append(getTestName(context))
        .append(" SKIPPED");

    if (reason.isPresent()) {
      builder.append(" (");
      String prefix = "void " + context.getRequiredTestClass().getName() + "." + context.getDisplayName();
      String text = reason.get();
      if (text.startsWith(prefix)) {
        text = text.substring(prefix.length());
        int idx = text.indexOf(" is disabled ");
        if (idx > -1) {
          builder.append(text.substring(idx + 4));
        } else if (text.endsWith("is @Disabled")) {
          builder.append("is @Disabled");
        } else {
          builder.append(text);
        }
      } else {
        builder.append(text);
      }
      builder.append(")");
    }

    BasicTestUtils.getLogger().warn(AnsiConsole.styleWarning(builder.toString()));
  }

  @Override
  public void testAborted(ExtensionContext context, Throwable cause) {
    status(context).aborted();
    if (cause == null) {
      BasicTestUtils.logError("- {} ABORTED", getTestName(context));
    } else {
      BasicTestUtils.logError("- {} ABORTED", getTestName(context), cause);
    }
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    status(context).failed();
    if (cause == null) {
      BasicTestUtils.logError("- {} FAILED", getTestName(context));
    } else {
      BasicTestUtils.logError("- {} FAILED", getTestName(context), cause);
    }
  }


  @Override
  public void afterAll(ExtensionContext context) {
    TestStats stats = status(context);
    BasicTestUtils.getLogger().info(AnsiConsole.colorize("\n{}\n", AnsiConsole.ANSI_BLUE), stats.toString());
  }


  private static final class TestStats {
    private int m_disabled;
    private int m_succeeded;
    private int m_aborted;
    private int m_failed;
    private final Instant m_started = Instant.now();


    void disabled() {
      m_disabled += 1;
    }

    void aborted() {
      m_aborted += 1;
    }

    void failed() {
      m_failed += 1;
    }

    void succeeded() {
      m_succeeded += 1;
    }

    @Override
    public String toString() {
      int totalTestsInClass = m_succeeded + m_aborted + m_failed + m_disabled;
      Duration duration = Duration.between(m_started, Instant.now());
      String prettyDuration = duration.toString()
          .substring(2)
          .replaceAll("(\\d[HMS])(?!$)", "$1 ")
          .toLowerCase();
      return String.format("Elapsed: %s  [Succeeded: %d, Skipped: %d, Failures: %d, Aborted: %d]",
          prettyDuration, m_succeeded, m_disabled, m_failed, m_aborted);
    }
  }
}
