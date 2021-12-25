package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import org.assertj.core.api.BigDecimalAssert;

/**
 * Refaster templates which improve {@link BigDecimal} asserts written in AssertJ.
 *
 * <p>A few {@link BigDecimal} assertions are not rewritten. This is because the {@link BigDecimal}
 * also uses scale to determine whether two values are equal. As a result, we cannot rewrite the
 * following assertions:
 *
 * <ul>
 *   <li>{@link BigDecimalAssert#isEqualTo(Object)} (for values 0L, 1L, BigDecimal.ZERO, and
 *       BigDecimal.ONE)
 *   <li>{@link BigDecimalAssert#isNotEqualTo(Object)} (for values 0L, 1L, BigDecimal.ZERO, and
 *       BigDecimal.ONE)
 * </ul>
 */
// XXX: If we add a rule which drops unnecessary `L` suffixes from literal longs, then the `0L`/`1L`
// cases below can go.
final class AssertJBigDecimalTemplates {
  private AssertJBigDecimalTemplates() {}

  static final class AbstractBigDecimalAssertIsEqualTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isEqualTo(n);
    }
  }

  static final class AbstractBigDecimalAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isNotCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractBigDecimalAssertIsZero {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isEqualTo(BigDecimal.ZERO);
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isZero();
    }
  }

  static final class AbstractBigDecimalAssertIsNotZero {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isNotEqualTo(BigDecimal.ZERO);
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isNotZero();
    }
  }

  static final class AbstractBigDecimalAssertIsOne {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isEqualTo(BigDecimal.ONE);
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isOne();
    }
  }
}
