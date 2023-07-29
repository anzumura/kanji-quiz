package com.github.anzumura.kt;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static com.github.anzumura.kt.Kanji.*;
import static org.junit.jupiter.api.Assertions.*;

class KanjiTest {
  // sample Kanji fields
  private static final String name = "海";
  private static final String radical = "水";
  private static final int strokes = 9;
  private static final String meaning = "sea";
  private static final String reading = "カイ、うみ";
  private static final Kyu kyu = Kyu.K9;
  private static final Kyu differentKyu = Kyu.KJ1;
  private static final int number = 182;
  private static final Level level = Level.N4;
  private static final int frequency = 200;
  private static final int differentFrequency = 2489;
  private static final int year = 2023;

  private static void checkKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(name, k.getName()),
        () -> assertEquals(radical, k.getRadical()),
        () -> assertEquals(strokes, k.getStrokes()));
  }

  private static void checkLinkedKanjiFields(Kanji k, Kanji link) {
    assertAll(() -> assertTrue(k.getOldNames().isEmpty()),
        () -> assertEquals(Optional.of(link.getName()), k.getNewName()),
        () -> assertEquals(link.getReading(), k.getReading()),
        () -> assertEquals(link.getMeaning(), k.getMeaning()),
        () -> assertEquals(Optional.of(link), k.getLink()),
        () -> assertTrue(k.hasLinkedReading()),
        () -> assertEquals(differentFrequency, k.getFrequency()),
        () -> assertEquals(differentKyu, k.getKyu()),
        // these should all have default values for linked (ie non-Loaded) Kanji
        () -> assertEquals(Grade.None, k.getGrade()),
        () -> assertEquals(Level.None, k.getLevel()),
        () -> assertEquals(JinmeiReason.None, k.getReason()),
        () -> assertEquals(0, k.getNumber()),
        () -> assertEquals(0, k.getYear()));
  }

  private static void checkLoadedKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(meaning, k.getMeaning()),
        () -> assertEquals(reading, k.getReading()),
        // link should always be empty for Loaded Kanji classes
        () -> assertTrue(k.getLink().isEmpty()));
    checkKanjiFields(k);
  }

  private static void checkNumberedKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(kyu, k.getKyu()),
        () -> assertEquals(number, k.getNumber()),
        // values that are not set should have default values
        () -> assertTrue(k.getOldNames().isEmpty()),
        () -> assertFalse(k.hasLinkedReading()));
    checkLoadedKanjiFields(k);
  }

  private static void checkOfficialKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(level, k.getLevel()),
        () -> assertEquals(frequency, k.getFrequency()),
        () -> assertEquals(year, k.getYear()),
        () -> assertTrue(k.getNewName().isEmpty()));
    checkNumberedKanjiFields(k);
  }

  @Nested
  class CreateNumberedKanjiTest {
    @ParameterizedTest
    @EnumSource(value = Grade.class, names = {"None"},
        mode = EnumSource.Mode.EXCLUDE)
    void jouyouKanjiWithValidGrade(Grade grade) {
      final var k =
          new JouyouKanji(name, radical, strokes, meaning, reading, kyu, number,
              level, frequency, year, grade);
      checkOfficialKanjiFields(k);
      assertAll(() -> assertEquals(Type.Jouyou, k.getType()),
          () -> assertEquals(grade, k.getGrade()),
          () -> assertEquals(JinmeiReason.None, k.getReason()));
    }

    @Test
    void jouyouKanjiWithNoGradeError() {
      final var e = assertThrows(DomainException.class,
          () -> new JouyouKanji(name, radical, strokes, meaning, reading, kyu,
              number, level, frequency, year, Grade.None));
      assertEquals("JouyouKanji: must have a valid grade", e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = JinmeiReason.class, names = {"None"},
        mode = EnumSource.Mode.EXCLUDE)
    void jinmeiKanjiWithValidReason(JinmeiReason reason) {
      final var k =
          new JinmeiKanji(name, radical, strokes, meaning, reading, kyu, number,
              level, frequency, year, reason);
      checkOfficialKanjiFields(k);
      assertAll(() -> assertEquals(Type.Jinmei, k.getType()),
          () -> assertEquals(reason, k.getReason()),
          () -> assertEquals(Grade.None, k.getGrade()));
    }

    @Test
    void jinmeiKanjiWithNoReasonError() {
      final var e = assertThrows(DomainException.class,
          () -> new JinmeiKanji(name, radical, strokes, meaning, reading, kyu,
              number, level, frequency, year, JinmeiReason.None));
      assertEquals("JinmeiKanji: must have a valid reason", e.getMessage());
    }

    @Test
    void jinmeiKanjiWithNoYearError() {
      final var e = assertThrows(DomainException.class,
          () -> new JinmeiKanji(name, radical, strokes, meaning, reading, kyu,
              number, level, frequency, 0, JinmeiReason.Names));
      assertEquals("JinmeiKanji: must have a valid year", e.getMessage());
    }

    @Test
    void extraKanji() {
      final var k =
          new ExtraKanji(name, radical, strokes, meaning, reading, kyu, number,
              "");
      checkNumberedKanjiFields(k);
      assertAll(() -> assertEquals(Type.Extra, k.getType()),
          () -> assertFalse(k.getNewName().isPresent()));
    }

    @Test
    void extraKanjiWithNewName() {
      final var newName = "犬";
      final var k =
          new ExtraKanji(name, radical, strokes, meaning, reading, kyu, number,
              newName);
      assertEquals(Optional.of(newName), k.getNewName());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void kanjiNumberMustBeGreaterThanZero(int value) {
      final var e = assertThrows(DomainException.class,
          () -> new ExtraKanji(name, radical, strokes, meaning, reading, kyu,
              value, ""));
      assertEquals("ExtraKanji: number must be greater than zero",
          e.getMessage());
    }
  }

  @Nested
  class CreateLinkedKanjiTest {
    @Test
    void linkedJinmeiKanji() {
      final var link =
          new JouyouKanji(name, radical, strokes, meaning, reading, kyu, number,
              level, frequency, year, Grade.G2);
      final var k = new LinkedJinmeiKanji(name, radical, strokes, link,
          differentFrequency, differentKyu);
      checkLinkedKanjiFields(k, link);
      assertEquals(Type.LinkedJinmei, k.getType());
    }

    @Test
    void linkedOldKanji() {
      final var link =
          new JouyouKanji(name, radical, strokes, meaning, reading, kyu, number,
              level, frequency, year, Grade.G2);
      final var k =
          new LinkedOldKanji(name, radical, strokes, link, differentFrequency,
              differentKyu);
      checkLinkedKanjiFields(k, link);
      assertEquals(Type.LinkedOld, k.getType());
    }

    @Test
    void linkedJinmeiMustLinkToOfficialKanji() {
      final var link =
          new ExtraKanji(name, radical, strokes, meaning, reading, kyu, number,
              "");
      final var e = assertThrows(DomainException.class,
          () -> new LinkedJinmeiKanji(name, radical, strokes, link,
              differentFrequency, differentKyu));
      assertEquals("LinkedJinmeiKanji: link must be JouyouKanji or JinmeiKanji",
          e.getMessage());
    }

    @Test
    void linkedOldMustLinkToJouyouKanji() {
      final var link =
          new ExtraKanji(name, radical, strokes, meaning, reading, kyu, number,
              "");
      final var e = assertThrows(DomainException.class,
          () -> new LinkedOldKanji(name, radical, strokes, link,
              differentFrequency, differentKyu));
      assertEquals("LinkedOldKanji: link must be JouyouKanji", e.getMessage());
    }
  }

  @Nested
  class CreateOtherKanjiTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void frequencyKanji(boolean linkedReadings) {
      final var k =
          new FrequencyKanji(name, radical, strokes, meaning, reading, false,
              List.of(), linkedReadings, kyu, frequency);
      checkLoadedKanjiFields(k);
      assertAll(() -> assertEquals(Type.Frequency, k.getType()),
          () -> assertTrue(k.getOldNames().isEmpty()),
          () -> assertTrue(k.getNewName().isEmpty()),
          () -> assertEquals(linkedReadings, k.hasLinkedReading()),
          () -> assertEquals(kyu, k.getKyu()),
          () -> assertEquals(frequency, k.getFrequency()));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void frequencyMustBeGreaterThanZero(int value) {
      final var e = assertThrows(DomainException.class,
          () -> new FrequencyKanji(name, radical, strokes, meaning, reading,
              false, List.of(), true, kyu, value));
      assertEquals("FrequencyKanji: frequency must be greater than zero",
          e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void kenteiKanji(boolean linkedReadings) {
      final var k =
          new KenteiKanji(name, radical, strokes, meaning, reading, false,
              List.of(), linkedReadings, kyu);
      checkLoadedKanjiFields(k);
      assertAll(() -> assertEquals(Type.Kentei, k.getType()),
          () -> assertTrue(k.getOldNames().isEmpty()),
          () -> assertTrue(k.getNewName().isEmpty()),
          () -> assertEquals(linkedReadings, k.hasLinkedReading()),
          () -> assertEquals(kyu, k.getKyu()),
          () -> assertEquals(0, k.getFrequency()));
    }

    @Test
    void kenteiKanjiWithNoKyuError() {
      final var e = assertThrows(DomainException.class,
          () -> new KenteiKanji(name, radical, strokes, meaning, reading, false,
              List.of(), true, Kyu.None));
      assertEquals("KenteiKanji: must have a valid Kyu", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ucdKanji(boolean linkedReadings) {
      final var k =
          new UcdKanji(name, radical, strokes, meaning, reading, false,
              List.of(), linkedReadings);
      checkLoadedKanjiFields(k);
      assertAll(() -> assertEquals(Type.Ucd, k.getType()),
          () -> assertTrue(k.getOldNames().isEmpty()),
          () -> assertTrue(k.getNewName().isEmpty()),
          () -> assertEquals(linkedReadings, k.hasLinkedReading()),
          () -> assertEquals(Kyu.None, k.getKyu()),
          () -> assertEquals(0, k.getFrequency()));
    }

    @Test
    void ucdKanjiWithOldNames() {
      final var links = List.of("辨", "瓣", "辯");
      final var k =
          new UcdKanji(name, radical, strokes, meaning, reading, true, links,
              false);
      checkLoadedKanjiFields(k);
      assertAll(() -> assertEquals(links, k.getOldNames()),
          () -> assertTrue(k.getNewName().isEmpty()));
    }

    @Test
    void ucdKanjiWithNewName() {
      final var newName = "弁";
      final var k =
          new UcdKanji(name, radical, strokes, meaning, reading, false,
              List.of(newName), false);
      checkLoadedKanjiFields(k);
      assertAll(() -> assertTrue(k.getOldNames().isEmpty()),
          () -> assertEquals(Optional.of(newName), k.getNewName()));
    }
  }

  @Nested
  class EnumTest {
    @Test
    void typeHasExpectedValues() {
      assertArrayEquals(new Type[]{
          Type.Jouyou, Type.Jinmei, Type.LinkedJinmei, Type.LinkedOld,
          Type.Frequency, Type.Extra, Type.Kentei, Type.Ucd
      }, Type.values());
    }

    @Test
    void gradeHasExpectedValues() {
      assertArrayEquals(new Grade[]{
          Grade.G1, Grade.G2, Grade.G3, Grade.G4, Grade.G5, Grade.G6, Grade.S,
          Grade.None
      }, Grade.values());
    }

    @Test
    void levelHasExpectedValues() {
      assertArrayEquals(new Level[]{
          Level.N5, Level.N4, Level.N3, Level.N2, Level.N1, Level.None
      }, Level.values());
    }

    @Test
    void kyuHasExpectedValues() {
      assertArrayEquals(new Kyu[]{
          Kyu.K10, Kyu.K9, Kyu.K8, Kyu.K7, Kyu.K6, Kyu.K5, Kyu.K4, Kyu.K3,
          Kyu.KJ2, Kyu.K2, Kyu.KJ1, Kyu.K1, Kyu.None
      }, Kyu.values());
    }

    @Test
    void reasonHasExpectedValues() {
      assertArrayEquals(new JinmeiReason[]{
          JinmeiReason.Names, JinmeiReason.Print, JinmeiReason.Variant,
          JinmeiReason.Moved, JinmeiReason.Simple, JinmeiReason.Other,
          JinmeiReason.None
      }, JinmeiReason.values());
    }
  }
}
