package info.tomfi.tutorials;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

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
