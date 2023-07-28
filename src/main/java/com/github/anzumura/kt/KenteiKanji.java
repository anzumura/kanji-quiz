package com.github.anzumura.kt;

import java.util.List;

/**
 * class for kanji in 'kentei/k*.txt' files that aren't already pulled in from
 * other files
 */
public final class KenteiKanji extends Kanji.Standard {
  @Override public Type getType() {
    return Type.Kentei;
  }

  public KenteiKanji(String meaning, String reading, Kyu kyu, boolean oldLinks,
                     boolean linkedReadings, List<String> linkNames) {
    super(meaning, reading, kyu, oldLinks, linkedReadings, linkNames);
  }
}
