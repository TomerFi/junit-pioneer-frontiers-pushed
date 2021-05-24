---
title: JUnit Pioneer - Frontiers Pushed
published: true
description: JUnit Pioneer - Frontiers Pushed.
tags: ["java", "test", "junit", "junitpioneer"]
---

## Making test code readable with JUnit Pioneer

As every experienced coder will probably tell you, going over a project's test cases, may sometimes give you a better understanding of the codebase outside the scope of documentation. :monocle_face:

But keeping your test classes short and readable is not always an easy task, seeing that most tests probably require some mocking or other setups before executing. :dizzy_face:

That being the case, and me being the testing addict that I am,
I'm regularly spending part of my coding routine going over some working test cases, considering ways of making them better.
Typically shorter and more readable, which in the long run will be more maintainable. :nerd_face:

If you want your tests more readable, you need [JUnit Pioneer][1] in your toolset!
As its main page describes it as a: *JUnit 5 extension pack, pushing the frontiers on Jupiter.* (a reference for this post's title).

I'm not going to go over all the goodies that come with [JUnit Pioneer][1], it's all pretty well documented.
I would, however, like to point out a couple of goodies that made me change the way I do some specific stuff. :cowboy_hat_face:

For instance, in a previous post of mine called [Property-Based Matrix Testing in Java][2], I've touched base on how the [CartesianProductTest][3] and the [CartesianEnumSource][4] annotations have helped me trim down this:

```java
  @ParameterizedTest
  @MethodSource("getArguments")
  void using_junit_parameterized_test_with_method_source(
      final Direction direction, final Status status) {
    assertTrue(true);
  }

  static Stream<Arguments> getArguments() {
    return Stream.of(Direction.values())
        .flatMap(d -> Stream.of(Status.values()).map(s -> arguments(d, s)));
  }
```

To this:

```java
  @CartesianProductTest
  @CartesianEnumSource(Direction.class)
  @CartesianEnumSource(Status.class)
  void using_junit_pioneer_cartesian_product_test_with_enum_source(
      final Direction direction, final Status status) {
    assertTrue(true);
  }
```

Today I'm going to go over the [Standard Input and Output][5] section of [JUnit Pioneer][1]. :eyes:

Let's take the following code example:

```java
class App {
  private static final Set<String> acceptableAnswers = Set.of("yes", "sure");

  public static void main(final String... args) {
    System.out.println("Hello, do you love Java?");
    verifyResponse(new Scanner(System.in));
    System.out.println("Me too!");
  }

  private static void verifyResponse(final Scanner scanner) {
    if (!acceptableAnswers.contains(scanner.next().toLowerCase())) {
      System.out.println("""
          Hmmm... I'm not sure I understand.
          Please reply with 'yes' or 'sure'.
          Do you love Java?""");
      verifyResponse(scanner);
    }
  }
}
```

This is pretty straightforward, executing the main method will ask: "Hello, do you love Java?" and will not accept any answers other than "yes" or "sure".

> This code is just for the example's sake, in most cases, you would probably be better off injecting the stream.

When testing code that takes data from `System`'s `in` stream and prints to its `out` stream, the basic and pretty much only way to test is by *hijacking* `System`'s streams, which means, replacing them with your streams for testing purposes, and then resetting them to their original ones once you're done.

The following test class executes three test cases by specifying data to the `in` stream and validating the output on the `out` stream, after *hijacking* both streams of course.
The test cases will test one scenario replying "yes", one by replying "sure", and one by replying "no" and then "yes":

```java
class Manual_Hijack_System_Streams_Test {
  private InputStream origIn;
  private PrintStream origOut;
  private OutputStream output;

  @BeforeEach
  void initialize() {
    origIn = System.in;
    origOut = System.out;
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
  }

  @AfterEach
  void cleanup() {
    System.setIn(origIn);
    System.setOut(origOut);
  }

  @ParameterizedTest
  @ValueSource(strings = {"yes", "sure"})
  void verify_replying_with_an_acceptable_answer(final String answer) {
    System.setIn(inputStreamOf(answer));
    App.main(new String[0]);

    assertArrayEquals(
      new String[] {"Hello, do you love Java?", "Me too!"},
      output.toString().split(System.lineSeparator()));
  }

  @Test
  void verify_replying_with_a_wrong_and_an_acceptable_answer() {
    System.setIn(inputStreamOf(collectAnswers("no", "yes")));
    App.main(new String[0]);

    assertArrayEquals(
      new String[] {
        "Hello, do you love Java?",
        """
            Hmmm... I'm not sure I understand.
            Please reply with 'yes' or 'sure'.
            Do you love Java?""",
        "Me too!"},
      output.toString().split(System.lineSeparator()));
  }

  private InputStream inputStreamOf(final String input) {
    return new ByteArrayInputStream(input.getBytes(UTF_8));
  }

  private String collectAnswers(final String... answers) {
    return stream(answers).collect(joining(System.lineSeparator()));
  }
}

```

Now, we can have [JUnit Pioneer][1] handle parts of the streams *hijacking* for us, and refactor our test class:

```java
class Let_Junit_Pioneer_Do_Its_Thing_Test {
  @ParameterizedTest
  @ValueSource(strings = {"yes", "sure"})
  @StdIo
  void verify_replying_with_an_acceptable_answer(final String answer, final StdOut output) {
    var origIn = System.in;

    System.setIn(inputStreamOf(answer));
    App.main(new String[0]);

    assertArrayEquals(
      new String[] {"Hello, do you love Java?", "Me too!"},
      output.capturedLines());

    System.setIn(origIn);
  }

  @Test
  @StdIo({"no", "yes"})
  void verify_replying_with_a_wrong_and_an_acceptable_answer(final StdOut output) {
    App.main(new String[0]);

    assertArrayEquals(
      new String[] {
        "Hello, do you love Java?",
        """
            Hmmm... I'm not sure I understand.
            Please reply with 'yes' or 'sure'.
            Do you love Java?""",
        "Me too!"},
      output.capturedLines());
  }

  private InputStream inputStreamOf(final String input) {
    return new ByteArrayInputStream(input.getBytes(UTF_8));
  }
}
```

For the second test method, `verify_replying_with_a_wrong_and_an_acceptable_answer`, we're using the `StdIo` annotation to *hijack* the `in` stream and specify the value to be captured by the stream to two lines, "no" and then "yes".
We also use the arguments resolver to inject our method with a *hijacked* `out` stream, with which we can validate the result.

For the first test method, `verify_replying_with_an_acceptable_answer`, we're using a propertyless `StdIo` annotation, which marks our test case for the arguments resolver, but we don't need it to *hijack* the `in` stream.
We do, however, make it *hijack* the `out` stream by instructing the arguments resolver to inject it, so we can validate the result with it, as we did in the second test method.

The reason for that is this test method being a test container that will produce multiple test cases, in our case it will produce two test cases, one with "yes" as the answer and one with "sure".
That means that the value specified for the `in` stream, is different for each case, meaning we had to *hijack* the `in` stream ourselves for this specific test.

I hope that the `StdIo` annotation will eventually have container testing support, and if it does, I'll do my best to update this post, but for now, it's good as it is. :star_struck:

Executing both test classes, will of course produce an exact result:

```text
[INFO] '-- JUnit Jupiter [OK]
[INFO]   +-- Manual Hijack System Streams Test [OK]
[INFO]   | +-- verify replying with a wrong and an acceptable answer [OK]
[INFO]   | '-- verify replying with an acceptable answer (String) [OK]
[INFO]   |   +-- [1] yes [OK]
[INFO]   |   '-- [2] sure [OK]
[INFO]   '-- Let Junit Pioneer Do Its Thing Test [OK]
[INFO]     +-- verify replying with a wrong and an acceptable answer (StdOut) [OK]
[INFO]     '-- verify replying with an acceptable answer (String, StdOut) [OK]
[INFO]       +-- [1] yes [OK]
[INFO]       '-- [2] sure [OK]
```

You can check out the code for this post in [Github][0].

**:wave: See you in the next post :wave:**

[0]: https://github.com/TomerFi/junit-pioneer-frontiers-pushed
[1]: https://junit-pioneer.org/
[2]: https://dev.to/tomerfi/property-based-matrix-testing-in-java-47p4
[3]: https://junit-pioneer.org/docs/cartesian-product/
[4]: https://junit-pioneer.org/docs/cartesian-product/#cartesianenumsource
[5]: https://junit-pioneer.org/docs/standard-input-output/
