package com.github.anzumura.kt;

import java.util.List;

/**
 * class for Kanji in 'ucd.txt' file that aren't already another 'Type'. Many of
 * these Kanji have a Morohashi ID (ie they are in 'Dai Kan-Wa Jiten'), but
 * others are pulled in via links and may not even have a Japanese reading.
 */
public final class UcdKanji extends Kanji.Other {
  public UcdKanji(
      String meaning, String reading, boolean oldLinks, boolean linkedReadings,
      List<String> linkNames) {
    super(meaning, reading, oldLinks, linkedReadings, linkNames);
  }

  @Override
  public Type getType() {
    return Type.Ucd;
  }
}
