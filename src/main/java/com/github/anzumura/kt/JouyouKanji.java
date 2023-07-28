package com.github.anzumura.kt;

/**
 * class representing the 2,136 official Jōyō Kanji
 */
public final class JouyouKanji extends Kanji.Official {
  private final Grade grade;

  public JouyouKanji(
      String meaning, String reading, Kyu kyu, int number, Level level,
      int frequency, int year, Grade grade) {
    super(meaning, reading, kyu, number, level, frequency, year);
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
