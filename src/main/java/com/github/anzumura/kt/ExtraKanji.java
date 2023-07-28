package com.github.anzumura.kt;

import java.util.Optional;

/**
 * class representing Kanji loaded from 'extra.txt' - a file that contains
 * manually selected 'fairly common' Kanji that aren't in official Jōyō or
 * Jinmeiyō lists (or their official old/alternative forms). These Kanji are
 * also not supposed to be in 'frequency.txt'.
 */
public final class ExtraKanji extends Kanji.Numbered {
  private final String newName;

  public ExtraKanji(
      String meaning, String reading, String newName, Kyu kyu, int number) {
    super(meaning, reading, kyu, number);
    this.newName = newName.isEmpty() ? null : newName;
  }

  @Override
  public Type getType() {
    return Type.Extra;
  }

  @Override
  public Optional<String> getNewName() {
    return Optional.ofNullable(newName);
  }
}
