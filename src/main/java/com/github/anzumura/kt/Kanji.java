package com.github.anzumura.kt;

import java.util.List;
import java.util.Optional;

/**
 * abstract base class representing a Japanese Kanji character
 */
public abstract sealed class Kanji permits Kanji.Loaded, Kanji.Linked {

  /**
   * @return type of this Kanji (unique for each leaf class)
   */
  public abstract Type getType();

  /**
   * @return list of English meanings (some 'Other' Kanji return empty string)
   */
  public abstract String getMeaning();

  /**
   * @return frequency: 1 for most frequent, 0 means not in top 2,501 list
   */
  public int getFrequency() {
    return 0;
  }

  /**
   * @return year Kanji was added to an official list, 0 means none specified
   */
  public int getYear() {
    return 0;
  }

  /**
   * @return true if readings were loaded via a link
   */
  public boolean hasLinkedReadings() {
    return false;
  }

  /**
   * some JouyouKanji and JinmeiKanji have 'old' (旧字体) forms:
   * <ul>
   *   <li>365 JouyouKanji: 364 have 'oldNames' with one entry and one has
   *     'oldNames' with three entries (弁 has 辨, 瓣 and 辯)
   *   <li>18 JinmeiKanji: alternate forms of standard JinmeiKanji
   *   <li>several hundred Kanji of other types also have non-empty 'oldNames'
   *     populated from 'ucd.txt' 'Traditional Links'
   * </ul>
   * 163 LinkedOldKanji end up getting created (367 - 204 linkedJinmeiKanji)
   *
   * @return list of old names (usually empty)
   */
  public List<String> getOldNames() {
    return List.of();
  }

  /**
   * 'Other' Kanji can populate this field based on 'Simplified Links' loaded
   * from 'ucd.txt'. OfficialLinked Kanji also set 'newName' to the name of the
   * `link` Kanji (which is the 'new/standard' version)
   *
   * @return new name (usually not defined)
   */
  public Optional<String> getNewName() {
    return Optional.empty();
  }

  /**
   * On (音) readings are in Katakana followed by Kun (訓) readings in Hiragana
   * (OfficialLinked Kanji classes return the readings of their 'link' Kanji)
   * Jouyou and Extra Kanji include a dash (-) in Kun readings before any
   * Okurigana (送り仮名), but unfortunately this is not the case for readings
   * loaded from 'ucd.txt'
   *
   * @return comma-separated list of Japanese readings in Kana
   */
  public abstract String getReading();

  /**
   * @return official school grade (non-None for all JouyouKanji)
   */
  public Grade getGrade() {
    return Grade.None;
  }

  /**
   * @return JLPT level (non-None for most JouyouKanji and some JinmeiKanji)
   */
  public Level getLevel() {
    return Level.None;
  }

  /**
   * @return Kentei Kyu (usually non-None for all Kanji types except UcdKanji)
   */
  public Kyu getKyu() {
    return Kyu.None;
  }

  /**
   * @return reason added to Jinmeiyō list (only applies to JinmeiKanji)
   */
  public JinmeiReason getReason() {
    return JinmeiReason.None;
  }

  /**
   * @return link back to a Jouyou or Jinmei Kanji
   */
  public Optional<Kanji.Official> getLink() {
    return Optional.empty();
  }

  /**
   * used to identify which official group (Jouyou or Jinmei) a Kanji belongs to
   * (or has a link to) as well as a few more groups for less common Kanji
   */
  public enum Type {
    Jouyou,       // 2,136 official Jōyō (常用) Kanji
    Jinmei,       // 633 official Jinmeiyō (人名用) Kanji
    LinkedJinmei, // 230 old/variant forms of Jouyou (212) and Jinmei 18)
    LinkedOld,    // 163 old/variant Jouyou Kanji that aren't in LinkedJinmei
    Frequency,    // 124 from 'frequency.txt' that aren't one of the above types
    Extra,        // loaded from 'extra.txt' (file doesn't contain above types)
    Kentei,       // loaded from 'kentei/*.txt' and not one of the above types
    Ucd           // loaded from 'ucd.txt' and not one of the above types
  }

  /**
   * represents the official school grade for all Jouyou Kanji
   */
  public enum Grade {
    G1, G2, G3, G4, G5, G6, S, None
  }

  /**
   * JLPT (Japanese Language Proficiency Test) Levels covers 2,222 total Kanji
   * (including 1,971 Jouyou and 251 Jinmei)
   */
  public enum Level {
    N5, N4, N3, N2, N1, None
  }

  /**
   * Kanji Kentei (漢字検定) Kyū (級), K = Kanken (漢検), J=Jun (準)
   *
   * @see <a href="https://en.wikipedia.org/wiki/Kanji_Kentei"></a>
   */
  public enum Kyu {
    K10, K9, K8, K7, K6, K5, K4, K3, KJ2, K2, KJ1, K1, None
  }

  /**
   * official reason Kanji was added to Jinmeiyō list:
   */
  public enum JinmeiReason {
    Names,   // 246 Kanji: for use in names
    Print,   // 352 Kanji: for use in publications
    Variant, // 2 Kanji: allowed variant form (異体字)
    Moved,   // 5 Kanji: moved out of Jouyou into Jinmei
    Simple,  // 2 Kanji: simplified (表外漢字字体表の簡易慣用字体)
    Other,   // 26 Kanji: reason listed as その他
    None     // Not a Jinmei type Kanji
  }

  // abstract subclasses of Kanji

  /**
   * contains 'meaning' and 'reading' fields loaded from files
   */
  public abstract static sealed class Loaded extends Kanji permits Numbered,
      Other {
    private final String meaning;
    private final String reading;

    protected Loaded(String meaning, String reading) {
      this.meaning = meaning;
      this.reading = reading;
    }

    @Override
    public String getMeaning() {
      return meaning;
    }

    @Override
    public String getReading() {
      return reading;
    }
  }

  /**
   * base class for the officially recognized variants stored in 'jouyou.txt'
   * and 'jinmei.txt'. Some of these Kanji are in the top 2,501 frequency list
   * and almost all of them are in Kentei KJ1 or K1. However, none of them have
   * a JLPT level.
   */
  public abstract static sealed class Linked extends Kanji permits
      LinkedJinmeiKanji, LinkedOldKanji {
    private final Kanji.Official link;
    private final int frequency;
    private final Kyu kyu;

    protected Linked(Kanji.Official link, int frequency, Kyu kyu) {
      this.link = link;
      this.frequency = frequency;
      this.kyu = kyu;
    }

    @Override
    public String getMeaning() {
      return link.getMeaning();
    }

    @Override
    public String getReading() {
      return link.getReading();
    }

    @Override
    public int getFrequency() {
      return frequency;
    }

    @Override
    public Kyu getKyu() {
      return kyu;
    }

    @Override
    public Optional<Kanji.Official> getLink() {
      return Optional.of(link);
    }
  }

  // abstract subclasses of Kanji.Loaded

  /**
   * has 'kyu', 'number' and 'oldNames' fields and includes shared functionality
   * for loading from customized local files
   */
  public abstract static sealed class Numbered extends Loaded permits Official,
      ExtraKanji {
    private final Kyu kyu;
    private final int number;

    protected Numbered(
        String meaning, String reading, Kyu kyu, int number) {
      super(meaning, reading);
      this.kyu = kyu;
      this.number = number;
    }

    @Override
    public Kyu getKyu() {
      return kyu;
    }

    public int getNumber() {
      return number;
    }
  }

  /**
   * base class for Kanji with fields mainly loaded from 'ucd.txt' as opposed to
   * Kanji loaded from 'jouyou.txt', 'jinmei.txt', and 'extra.txt' files
   */
  public abstract static sealed class Other extends Loaded permits Standard,
      UcdKanji {
    private final boolean oldLinks;
    private final boolean linkedReadings;
    private final List<String> linkNames;

    protected Other(
        String meaning, String reading, boolean oldLinks,
        boolean linkedReadings, List<String> linkNames) {
      super(meaning, reading);
      this.oldLinks = oldLinks;
      this.linkedReadings = linkedReadings;
      this.linkNames = linkNames;
    }

    @Override
    public List<String> getOldNames() {
      return oldLinks ? linkNames : List.of();
    }

    @Override
    public Optional<String> getNewName() {
      return linkNames.isEmpty() || oldLinks ? Optional.empty() :
          Optional.of(linkNames.get(0));
    }

    @Override
    public boolean hasLinkedReadings() {
      return linkedReadings;
    }
  }

  // abstract subclasses of Kanji.Numbered

  /**
   * has fields shared by derived classes including 'level' (can be `None`) and
   * optional 'frequency' and 'year' values
   */
  public abstract static sealed class Official extends Numbered permits
      JouyouKanji, JinmeiKanji {
    private final Level level;
    private final int frequency;
    private final int year;

    protected Official(
        String meaning, String reading, Kyu kyu, int number, Level level,
        int frequency, int year) {
      super(meaning, reading, kyu, number);
      this.level = level;
      this.frequency = frequency;
      this.year = year;
    }

    @Override
    public Level getLevel() {
      return level;
    }

    @Override
    public int getFrequency() {
      return frequency;
    }

    @Override
    public int getYear() {
      return year;
    }
  }

  // abstract subclasses of Kanji.Other

  /**
   * base class for FrequencyKanji and KenteiKanji (adds a 'kyu' field)
   */
  public abstract static sealed class Standard extends Other permits
      FrequencyKanji, KenteiKanji {
    private final Kyu kyu;

    protected Standard(
        String meaning, String reading, Kyu kyu, boolean oldLinks,
        boolean linkedReadings, List<String> linkNames) {
      super(meaning, reading, oldLinks, linkedReadings, linkNames);
      this.kyu = kyu;
    }

    @Override
    public Kyu getKyu() {
      return kyu;
    }
  }
}