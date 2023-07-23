package kanji_tools.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static kanji_tools.utils.ColumnFile.Column;
import static org.junit.jupiter.api.Assertions.*;

class ColumnFileTest {

  private static final String testFile = "test.txt";
  private static final String fileMsg = " - file: " + testFile;
  private static final Column col1 = new Column("col1"), col2 = new Column(
      "col2"), col3 = new Column("col3");

  @TempDir
  private Path tempDir;

  private ColumnFile create(List<Column> columns, List<String> contents) {
    try {
      var path = Files.createFile(tempDir.resolve(testFile));
      Files.write(path, contents);
      return new ColumnFile(path.toString(), columns);
    } catch (IOException e) {
      return fail("failed to create file - " + e.getMessage());
    }
  }

  @Nested
  class ConstructorTest {
    @Test
    void emptyColumnsError() {
      var e = assertThrows(IllegalArgumentException.class,
          () -> create(List.of(), List.of()));
      assertEquals("must specify at least one column", e.getMessage());
    }

    @Test
    void duplicateColumnError() {
      var e = assertThrows(IllegalArgumentException.class,
          () -> create(List.of(col1, col2, col1), List.of()));
      assertEquals("duplicate column 'col1'", e.getMessage());
    }

    @Test
    void missingFileError() {
      var e = assertThrows(IOException.class,
          () -> new ColumnFile(testFile, List.of(col1)));
      assertEquals(testFile + " (No such file or directory)", e.getMessage());
    }

    @Test
    void missingHeaderRowError() {
      var e = assertThrows(DomainException.class,
          () -> create(List.of(col1), List.of()));
      assertEquals("missing header row" + fileMsg, e.getMessage());
    }

    @Test
    void duplicateHeaderError() {
      var e = assertThrows(DomainException.class,
          () -> create(List.of(col1), List.of("col1 col1")));
      assertEquals("duplicate header 'col1'" + fileMsg, e.getMessage());
    }

    @Test
    void unrecognizedHeaderError() {
      var e = assertThrows(DomainException.class,
          () -> create(List.of(col1), List.of("col1 col2")));
      assertEquals("unrecognized header 'col2'" + fileMsg, e.getMessage());
    }

    @Test
    void oneMissingColumnError() {
      var e = assertThrows(DomainException.class,
          () -> create(List.of(col1, col2), List.of("col1")));
      assertEquals("column 'col2' not found" + fileMsg, e.getMessage());
    }

    @Test
    void multipleMissingColumnsError() {
      var e = assertThrows(DomainException.class,
          () -> create(List.of(col1, col2, col3), List.of("col2")));
      assertEquals("2 columns not found: 'col1', 'col3'" + fileMsg,
          e.getMessage());
    }
  }

  @Nested
  class ColumnTest {
    @Test
    void getName() {
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
    void numberIsUniquePerName() {
      var colA = new Column("colA");
      assertEquals(colA.getNumber(), new Column(colA.getName()).getNumber());
      assertNotEquals(colA.getNumber(), new Column("colB").getNumber());
    }

    @Test
    void toStringReturnsName() {
      var col = new Column("col");
      assertEquals(col.getName(), col.toString());
    }
  }
}