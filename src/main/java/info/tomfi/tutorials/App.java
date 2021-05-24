package info.tomfi.tutorials;

import java.util.Scanner;
import java.util.Set;

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
