package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.BaseTestConsumer;
import io.reactivex.observers.TestObserver;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  Single<Object> testSingleErrorThrowable() {
    return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException()));
  }

  Single<Object> testSingleErrorCallable() {
    return RxJava2Adapter.monoToSingle(
        Mono.error(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  throw new IllegalStateException();
                })));
  }

  Single<Integer> testSingleFromCallable() {
    return RxJava2Adapter.monoToSingle(
        Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> 1)));
  }

  Single<Integer> testSingleJust() {
    return RxJava2Adapter.monoToSingle(Mono.just(1));
  }

  Single<Integer> testSingleWrap() {
    return Single.just(1);
  }

  Single<Integer> testSingleDoOnError() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToSingle);
  }

  Single<Integer> testSingleDoOnSuccess() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .flatMap(i -> Single.just(i * 2).as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }

  Completable testCompletableIgnoreElement() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .ignoreElement()
        .as(RxJava2Adapter::monoToCompletable);
  }

  Single<Integer> testSingleMap() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(RxJavaReactorMigrationUtil.toJdkFunction(i -> i + 1))
        .as(RxJava2Adapter::monoToSingle);
  }

  Flowable<Integer> testSingleToFlowable() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .flux()
        .as(RxJava2Adapter::fluxToFlowable);
  }

  void testSingleTestAssertResultItem() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
    RxJava2Adapter.singleToMono(Single.just(2))
        .as(StepVerifier::create)
        .expectNext(2)
        .verifyComplete();
  }

  void testSingleTestAssertResult() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertValue() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 1))
        .verifyComplete();
  }

  void testSingleTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectError(InterruptedException.class)
        .verify();
  }

  void testSingleTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
