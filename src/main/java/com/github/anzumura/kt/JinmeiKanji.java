package com.github.anzumura.kt;

/**
 * class representing the 633 official Jinmeiyō Kanji
 */
public final class JinmeiKanji extends Kanji.Official {
  private final JinmeiReason reason;

  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public JinmeiKanji(
      String name, String radical, int strokes, String meaning, String reading,
      Kyu kyu, int number, Level level, int frequency, int year,
      JinmeiReason reason) {
    super(new Fields(name, radical, strokes),
        new LoadedFields(meaning, reading), new NumberedFields(kyu, number),
        new OfficialFields(level, frequency, year));
    if (reason == JinmeiReason.None)
      throw error("must have a valid reason");
    // Jinmei Kanji have year values starting at 1951, but for now just ensure
    // it's non-zero to prevent the case of constructing without a year
    if (year == 0)
      throw error("must have a valid year");
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
