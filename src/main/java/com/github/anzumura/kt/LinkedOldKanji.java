package com.github.anzumura.kt;

/**
 * 163 Kanji that link to a JouyouKanji. These are the published Jōyō variants
 * that aren't already included in the 230 Jinmeiyō 'official variants'.
 */
public final class LinkedOldKanji extends Kanji.Linked {
  public LinkedOldKanji(Kanji.Official link, int frequency, Kyu kyu) {
    super(link, frequency, kyu);
  }

  @Override
  public Type getType() {
    return Type.LinkedOld;
  }
}
