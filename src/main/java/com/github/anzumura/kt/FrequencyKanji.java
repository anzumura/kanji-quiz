package com.github.anzumura.kt;

import java.util.List;

/**
 * class for Kanji in the top 2,501 frequency list ('frequency.txt') that
 * haven't already been loaded from a 'jouyou' or 'jinmei' file
 */
public final class FrequencyKanji extends Kanji.Standard {
  private final int frequency;

  public FrequencyKanji(
      String meaning, String reading, Kyu kyu, boolean oldLinks,
      boolean linkedReadings, List<String> linkNames, int frequency) {
    super(meaning, reading, kyu, oldLinks, linkedReadings, linkNames);
    this.frequency = frequency;
  }

  @Override
  public Type getType() {
    return Type.Frequency;
  }

  @Override
  public int getFrequency() {
    return frequency;
  }
}
