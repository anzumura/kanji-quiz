package com.github.anzumura.kt;

/**
 * official set of 230 Jinmeiyō Kanji that are old or alternative forms of
 * JouyouKanji or JinmeiKanji. There are 230 of these Kanji:
 * <ul>
 *   <li>204 are part of the 365 JouyouKanji 'old names' set
 *   <li>8 are different alternate forms of JouyouKanji (薗 駈 嶋 盃 冨 峯 埜 凉)
 *   <li> 18 are alternate forms of standard JinmeiKanji
 * </ul>
 */
public final class LinkedJinmeiKanji extends Kanji.Linked {
  public LinkedJinmeiKanji(Kanji.Official link, int frequency, Kyu kyu) {
    super(link, frequency, kyu);
  }

  @Override
  public Type getType() {
    return Type.LinkedJinmei;
  }
}
