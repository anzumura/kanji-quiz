package com.github.anzumura.kt;

/**
 * class representing the 633 official Jinmeiyō Kanji
 */
public final class JinmeiKanji extends Kanji.Official {
  private final JinmeiReason reason;

  public JinmeiKanji(
      String meaning, String reading, Kyu kyu, int number, Level level,
      int frequency, int year, JinmeiReason reason) {
    super(meaning, reading, kyu, number, level, frequency, year);
    this.reason = reason;
  }

  @Override
  public Type getType() {
    return Type.Jinmei;
  }

  @Override
  public JinmeiReason getReason() {
    return reason;
  }
}
