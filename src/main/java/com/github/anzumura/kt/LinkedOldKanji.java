package com.github.anzumura.kt;

/**
 * 163 Kanji that link to a JouyouKanji. These are the published Jōyō variants
 * that aren't already included in the 230 Jinmeiyō 'official variants'.
 */
public final class LinkedOldKanji extends Kanji.Linked {
  /**
   * see Kanji class 'get' methods for details on parameters
   */
  public LinkedOldKanji(
      String name, String radical, int strokes, Kanji link, int frequency,
      Kyu kyu) {
    super(new Fields(name, radical, strokes),
        new LinkedFields(link, frequency, kyu));
    if (!(link instanceof JouyouKanji))
      throw error("link must be JouyouKanji");
  }

  @Override
  public Type getType() {
    return Type.LinkedOld;
  }
}
