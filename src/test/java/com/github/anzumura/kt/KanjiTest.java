package com.github.anzumura.kt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.github.anzumura.kt.Kanji.*;
import static org.junit.jupiter.api.Assertions.*;

class KanjiTest {
  // sample Kanji fields
  private final String name = "海";
  private final String radical = "水";
  private final int strokes = 9;
  private final String meaning = "sea";
  private final String reading = "カイ、うみ";
  private final Kyu kyu = Kyu.K9;
  private final Kyu differentKyu = Kyu.KJ1;
  private final int number = 182;
  private final Level level = Level.N4;
  private final int frequency = 200;
  private final int differentFrequency = 2489;
  private final int year = 2023;

  private void checkKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(name, k.getName()),
        () -> assertEquals(radical, k.getRadical()),
        () -> assertEquals(strokes, k.getStrokes()));
  }

  private void checkLinkedKanjiFields(Kanji k, Kanji link) {
    assertAll(() -> assertTrue(k.getOldNames().isEmpty()),
        () -> assertEquals(Optional.of(link.getName()), k.getNewName()),
        () -> assertEquals(link.getReading(), k.getReading()),
        () -> assertEquals(link.getMeaning(), k.getMeaning()),
        () -> assertEquals(Optional.of(link), k.getLink()),
        () -> assertTrue(k.hasLinkedReading()),
        () -> assertEquals(differentFrequency, k.getFrequency()),
        () -> assertEquals(differentKyu, k.getKyu()));
  }

  private void checkLoadedKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(meaning, k.getMeaning()),
        () -> assertEquals(reading, k.getReading()),
        // link should always be empty for Loaded Kanji classes
        () -> assertTrue(k.getLink().isEmpty()));
    checkKanjiFields(k);
  }

  private void checkNumberedKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(kyu, k.getKyu()),
        () -> assertEquals(number, k.getNumber()),
        // values that are not set should have default values
        () -> assertTrue(k.getOldNames().isEmpty()),
        () -> assertFalse(k.hasLinkedReading()));
    checkLoadedKanjiFields(k);
  }

  private void checkOfficialKanjiFields(Kanji k) {
    assertAll(() -> assertEquals(level, k.getLevel()),
        () -> assertEquals(frequency, k.getFrequency()),
        () -> assertEquals(year, k.getYear()),
        () -> assertTrue(k.getNewName().isEmpty()));
    checkNumberedKanjiFields(k);
  }

  @Test
  void jouyouKanji() {
    final var grade = Grade.G2;
    final var k =
        new JouyouKanji(name, radical, strokes, meaning, reading, kyu, number,
            level, frequency, year, grade);
    checkOfficialKanjiFields(k);
    assertAll(() -> assertEquals(Type.Jouyou, k.getType()),
        () -> assertEquals(grade, k.getGrade()),
        () -> assertEquals(JinmeiReason.None, k.getReason()));
  }

  @Test
  void jinmeiKanji() {
    final var reason = JinmeiReason.Names;
    final var k =
        new JinmeiKanji(name, radical, strokes, meaning, reading, kyu, number,
            level, frequency, year, reason);
    checkOfficialKanjiFields(k);
    assertAll(() -> assertEquals(Type.Jinmei, k.getType()),
        () -> assertEquals(reason, k.getReason()),
        () -> assertEquals(Grade.None, k.getGrade()));
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

  @Test
  void frequencyKanji() {
    final var linkedReadings = true;
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

  @Test
  void kenteiKanji() {
    final var linkedReadings = true;
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
  void ucdKanji() {
    final var linkedReadings = true;
    final var k =
        new UcdKanji(name, radical, strokes, meaning, reading, false, List.of(),
            linkedReadings);
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
    final var k = new UcdKanji(name, radical, strokes, meaning, reading, false,
        List.of(newName), false);
    checkLoadedKanjiFields(k);
    assertAll(() -> assertTrue(k.getOldNames().isEmpty()),
        () -> assertEquals(Optional.of(newName), k.getNewName()));
  }

  @Test
  void linkedJinmeiKanji() {
    final var link =
        new JouyouKanji(name, radical, strokes, meaning, reading, kyu, number,
            level, frequency, year, Grade.G2);
    final var k =
        new LinkedJinmeiKanji(name, radical, strokes, link, differentFrequency,
            differentKyu);
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
}
