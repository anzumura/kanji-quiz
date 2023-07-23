package kanji_tools.utils;

/**
 * DomainException is thrown in when data loaded from files on startup is
 * inconsistent or malformed, or when internal program logic errors occur.
 */
public class DomainException extends RuntimeException {
  public DomainException(String message) {
    super(message);
  }
}
