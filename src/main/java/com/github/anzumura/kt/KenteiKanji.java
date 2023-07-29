package com.github.anzumura.kt;

import java.util.List;

/**
 * class for kanji in 'kentei/k*.txt' files that aren't already pulled in from
 * other files
 */
public final class KenteiKanji extends Kanji.Standard {
  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public KenteiKanji(
      String name, String radical, int strokes, String meaning, String reading,
      boolean oldLinks, List<String> linkNames, boolean linkedReadings,
      Kyu kyu) {
    super(new Fields(name, radical, strokes),
        new LoadedFields(meaning, reading),
        new OtherFields(oldLinks, linkNames, linkedReadings), kyu);
    if (kyu == Kyu.None)
      throw error("must have a valid Kyu");
  }

  @Override
  public Type getType() {
    return Type.Kentei;
  }
}
