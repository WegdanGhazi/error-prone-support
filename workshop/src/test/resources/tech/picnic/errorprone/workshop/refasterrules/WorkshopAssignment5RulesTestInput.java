package tech.picnic.errorprone.workshop.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment5RulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testStreamDoAllMatch() {
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(s -> !s.isBlank()),
        Stream.of("bar").noneMatch(b -> !b.startsWith("b")));
  }
}
