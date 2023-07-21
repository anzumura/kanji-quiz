package kanji_tools.utils;

import org.junit.jupiter.api.Test;

import static kanji_tools.utils.ColumnFile.Column;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ColumnFileTest {
  @Test
  void columnGetName() {
    var sut = new Column("name-test");
    assertEquals("name-test", sut.getName());
  }

  @Test
  void newColumnsGetIncrementingNumbers() {
    var a = new Column("new-a");
    var b = new Column("new-b");
    var c = new Column("new-c");
    assertEquals(a.getNumber() + 1, b.getNumber());
    assertEquals(b.getNumber() + 1, c.getNumber());
  }

  @Test
  void columnNumberIsUniquePerName() {
    var colA = new Column("colA");
    assertEquals(colA.getNumber(), new Column(colA.getName()).getNumber());
    assertNotEquals(colA.getNumber(), new Column("colB").getNumber());
  }
}