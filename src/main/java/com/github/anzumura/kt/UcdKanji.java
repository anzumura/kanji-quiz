package com.github.anzumura.kt;

import java.util.List;

/**
 * class for Kanji in 'ucd.txt' file that aren't already another 'Type'. Many of
 * these Kanji have a Morohashi ID (ie they are in 'Dai Kan-Wa Jiten'), but
 * others are pulled in via links and may not even have a Japanese reading.
 */
public final class UcdKanji extends Kanji.Other {
  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public UcdKanji(
      String name, String radical, int strokes, String meaning, String reading,
      boolean oldLinks, List<String> linkNames, boolean linkedReadings) {
    super(new Fields(name, radical, strokes),
        new LoadedFields(meaning, reading),
        new OtherFields(oldLinks, linkNames, linkedReadings));
  }

  @Override
  public Type getType() {
    return Type.Ucd;
  }
}
