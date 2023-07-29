package com.github.anzumura.kt;

import java.util.List;

/**
 * class for Kanji in the top 2,501 frequency list ('frequency.txt') that
 * haven't already been loaded from a 'jouyou' or 'jinmei' file
 */
public final class FrequencyKanji extends Kanji.Standard {
  private final int frequency;

  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public FrequencyKanji(
      String name, String radical, int strokes, String meaning, String reading,
      boolean oldLinks, List<String> linkNames, boolean linkedReadings, Kyu kyu,
      int frequency) {
    super(new Fields(name, radical, strokes),
        new LoadedFields(meaning, reading),
        new OtherFields(oldLinks, linkNames, linkedReadings), kyu);
    if (frequency <= 0)
      throw error("frequency must be greater than zero");
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
