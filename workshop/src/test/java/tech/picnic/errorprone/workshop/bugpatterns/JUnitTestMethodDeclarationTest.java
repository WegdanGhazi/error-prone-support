package tech.picnic.errorprone.workshop.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class JUnitTestMethodDeclarationTest {
  @Disabled("Enable this to validate part 1.")
  @Test
  void identificationIllegalModifiers() {
    CompilationTestHelper.newInstance(JUnitTestMethodDeclaration.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void method1() {}",
            "",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  public void method2() {}",
            "",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  protected void method3() {}",
            "",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  private void method4() {}",
            "",
            "  public void method5() {}",
            "",
            "  protected void method6() {}",
            "",
            "  private void method7() {}",
            "}")
        .doTest();
  }

  @Disabled("Enable this to validate part 2.")
  @Test
  void replacementIllegalModifiers() {
    BugCheckerRefactoringTestHelper.newInstance(JUnitTestMethodDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void foo() {}",
            "",
            "  @Test",
            "  public void bar() {}",
            "",
            "  @Test",
            "  protected void baz() {}",
            "",
            "  @Test",
            "  private void qux() {}",
            "",
            "  public void quux() {}",
            "",
            "  private void quuz() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void foo() {}",
            "",
            "  @Test",
            "  void bar() {}",
            "",
            "  @Test",
            "  void baz() {}",
            "",
            "  @Test",
            "  void qux() {}",
            "",
            "  public void quux() {}",
            "",
            "  private void quuz() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
