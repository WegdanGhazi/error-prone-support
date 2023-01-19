package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGMigrationCheckTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TestNGMigrationCheck.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "    // BUG: Diagnostic contains:",
            "    @Test",
            "    public class A {",
            "      public void foo() {}",
            "",
            "    // BUG: Diagnostic contains:",
            "      @Test(description = \"bar\")",
            "      public void bar() {}",
            "",
            "    // BUG: Diagnostic contains:",
            "      @Test",
            "      public static class B {",
            "        public void baz () {}",
            "      }",
            "    }")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TestNGMigrationCheck.class, getClass())
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  public void foo() {}",
            "",
            "  @Test(priority = 1, description = \"unit\")",
            "  public void bar() {}",
            "",
            "  @Test(dataProvider = \"fooNumbers\")",
            "  public void foo(String string, int number) {}",
            "",
            "  @DataProvider",
            "  private Object[][] fooNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@org.junit.jupiter.api.TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @org.junit.jupiter.api.Test",
            "  public void foo() {}",
            "",
            "  @org.junit.jupiter.api.Order(1)",
            "  @org.junit.jupiter.api.DisplayName(\"unit\")",
            "  @org.junit.jupiter.api.Test",
            "  public void bar() {}",
            "",
            "  @org.junit.jupiter.params.ParameterizedTest",
            "  @org.junit.jupiter.params.provider.MethodSource(\"fooNumbers\")",
            "  public void foo(String string, int number) {}",
            "",
            "  private static Stream<Arguments> fooNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", values[0]), arguments(\"2\", values[1]));",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
