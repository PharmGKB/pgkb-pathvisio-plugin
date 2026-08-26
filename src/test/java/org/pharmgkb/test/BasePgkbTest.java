package org.pharmgkb.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pharmgkb.test.junit.TestResultLoggerExtension;


/**
 * This is the base on which tests should be built upon.
 * This used to be a part of {@code PgkbTest}, but we're splitting it out into a separate class to limit
 * dependencies required for PathVisio.
 * It should only be used directly by tests in this module.
 *
 * @author Mark Woon
 */
@ExtendWith(TestResultLoggerExtension.class)
public interface BasePgkbTest {

  @BeforeAll
  static void baseBeforeAll() {
    BasicTestUtils.initializeJUnitTest();
  }


  default String getTestName(TestInfo testInfo) {
    String testMethodName = "";
    if (testInfo.getTestMethod().isPresent()) {
      testMethodName = "." + testInfo.getTestMethod().get().getName();
    }
    return getClass().getSimpleName() + testMethodName;
  }


  default void println() {
    BasicTestUtils.println();
  }

  default void println(String msg) {
    BasicTestUtils.println(msg);
  }

  default void println(Number num) {
    BasicTestUtils.println(num.toString());
  }

  default void println(String fmt, Object... args) {
    BasicTestUtils.println(fmt, args);
  }


  default void printWarning(String fmt, Object... args) {
    BasicTestUtils.logWarning(fmt, args);
  }

  default void printError(String fmt, Object... args) {
    BasicTestUtils.logError(fmt, args);
  }
}
