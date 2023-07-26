package com.github.anzumura.kt;

/**
 * DomainException is thrown when data loaded from files on startup is
 * inconsistent or malformed, or when internal program logic errors occur.
 */
public class DomainException extends RuntimeException {

  /**
   * @param message exception message
   */
  public DomainException(String message) {
    super(message);
  }
}
