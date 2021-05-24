package info.tomfi.tutorials;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
