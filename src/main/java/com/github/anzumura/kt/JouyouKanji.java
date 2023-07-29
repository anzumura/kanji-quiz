package com.github.anzumura.kt;

/**
 * class representing the 2,136 official Jōyō Kanji
 */
public final class JouyouKanji extends Kanji.Official {
  private final Grade grade;

  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public JouyouKanji(
      String name, String radical, int strokes, String meaning, String reading,
      Kyu kyu, int number, Level level, int frequency, int year, Grade grade) {
    super(new Fields(name, radical, strokes),
        new LoadedFields(meaning, reading), new NumberedFields(kyu, number),
        new OfficialFields(level, frequency, year));
    this.grade = grade;
  }

  @Override
  public Type getType() {
    return Type.Jouyou;
  }

  @Override
  public Grade getGrade() {
    return grade;
  }
}
