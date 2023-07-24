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
  private static final Column col1 = new Column("col1"), col2 = new Column(
      "col2"), col3 = new Column("col3");

  @TempDir
  private Path tempDir;

  private static String errorMsg(String msg) {
    return msg + " - file: " + testFile;
  }

  private static String errorMsg(String msg, int row) {
    return errorMsg(msg) + ", row: " + row;
  }

  private ColumnFile create(
      String delimiter, List<Column> columns, String... lines) {
    try {
      final var path = Files.createFile(tempDir.resolve(testFile));
      Files.write(path, List.of(lines));
      // use 'null' delimiter to allow testing constructor overloads, i.e., null
      // means use the constructor that doesn't take a delimiter (and instead
      // passes in "\t" in the main code)
      return delimiter == null ? new ColumnFile(path.toString(),
          columns) : new ColumnFile(path.toString(), columns, delimiter);
    } catch (IOException e) {
      return fail("failed to create file - " + e.getMessage());
    }
  }

  private ColumnFile create(List<Column> columns, String... lines) {
    return create(null, columns, lines);
  }

  @Nested
  class ColumnTest {
    @Test
    void getName() {
      assertEquals("col1", col1.getName());
    }

    @Test
    void newColumnsGetIncrementingNumbers() {
      assertEquals(col1.getNumber() + 1, col2.getNumber());
      assertEquals(col2.getNumber() + 1, col3.getNumber());
    }

    @Test
    void numberIsUniquePerName() {
      assertEquals(col1.getNumber(), new Column(col1.getName()).getNumber());
      assertNotEquals(col2.getNumber(), new Column("col22").getNumber());
    }

    @Test
    void toStringReturnsName() {
      assertEquals(col1.getName(), col1.toString());
    }
  }

  @Nested
  class ConstructorTest {
    @Test
    void createSingleColumnFile() {
      assertEquals(1, create(List.of(col1), "col1").numColumns());
    }

    @Test
    void createMultiColumnFile() {
      assertEquals(2, create(List.of(col1, col2), "col1\tcol2").numColumns());
    }

    @Test
    void createSpaceDelimitedFile() {
      assertEquals(2,
          create(" ", List.of(col1, col2), "col1 col2").numColumns());
    }

    @Test
    void currentRowIsZeroBeforeAnyDataRowsAreProcessed() {
      assertEquals(0, create(List.of(col1), "col1").currentRow());
    }

    @Test
    void emptyColumnsError() {
      final var e = assertThrows(IllegalArgumentException.class,
          () -> create(List.of()));
      assertEquals("must specify at least one column", e.getMessage());
    }

    @Test
    void duplicateColumnError() {
      final var e = assertThrows(IllegalArgumentException.class,
          () -> create(List.of(col1, col2, col1)));
      assertEquals("duplicate column 'col1'", e.getMessage());
    }

    @Test
    void missingFileError() {
      final var e = assertThrows(IOException.class,
          () -> new ColumnFile(testFile, List.of(col1)));
      assertEquals(testFile + " (No such file or directory)", e.getMessage());
    }

    @Test
    void missingHeaderRowError() {
      final var e = assertThrows(DomainException.class,
          () -> create(List.of(col1)));
      assertEquals(errorMsg("missing header row"), e.getMessage());
    }

    @Test
    void duplicateHeaderError() {
      final var e = assertThrows(DomainException.class,
          () -> create(List.of(col1), "col1\tcol1"));
      assertEquals(errorMsg("duplicate header 'col1'"), e.getMessage());
    }

    @Test
    void unrecognizedHeaderError() {
      final var e = assertThrows(DomainException.class,
          () -> create(List.of(col1), "col1\tcol2"));
      assertEquals(errorMsg("unrecognized header 'col2'"), e.getMessage());
    }

    @Test
    void oneMissingColumnError() {
      final var e = assertThrows(DomainException.class,
          () -> create(List.of(col1, col2), "col1"));
      assertEquals(errorMsg("column 'col2' not found"), e.getMessage());
    }

    @Test
    void multipleMissingColumnsError() {
      final var e = assertThrows(DomainException.class,
          () -> create(List.of(col1, col2, col3), "col2"));
      assertEquals(errorMsg("2 columns not found: 'col1', 'col3'"),
          e.getMessage());
    }
  }

  @Nested
  class GetDataTest {
    @Test
    void getStringValue() {
      final var expected = "Val";
      final var file = create(List.of(col1), "col1", expected);
      assertTrue(file.nextRow());
      assertEquals(expected, file.get(col1));
    }

    @Test
    void nextRowReturnsFalseAtEOF() {
      final var file = create(List.of(col1, col2), "col1\tcol2", "A\tB");
      assertTrue(file.nextRow());
      assertEquals(1, file.currentRow());
      assertEquals("A", file.get(col1));
      assertEquals("B", file.get(col2));
      assertFalse(file.nextRow());
      // current row is still the last successfully processed row
      assertEquals(1, file.currentRow());
      assertEquals("A", file.get(col1));
      assertEquals("B", file.get(col2));
      assertFalse(file.nextRow());
    }

    @Test
    void valuesCanBeBlank() {
      final var file = create(List.of(col1, col2, col3), "col1\tcol2\tcol3",
          "\tB\tC", "A\t\tC", "\t\t");
      file.nextRow(); // first value is empty
      assertEquals("", file.get(col1));
      assertEquals("B", file.get(col2));
      assertEquals("C", file.get(col3));
      file.nextRow(); // second value is empty
      assertEquals("A", file.get(col1));
      assertEquals("", file.get(col2));
      assertEquals("C", file.get(col3));
      file.nextRow(); // all values are empty
      assertEquals("", file.get(col1));
      assertEquals("", file.get(col2));
      assertEquals("", file.get(col3));
      // make sure all data has been read
      assertFalse(file.nextRow());
      assertEquals(3, file.currentRow());
    }

    @Test
    void nextRowErrorForRowWithTooManyColumns() {
      final var file = create(List.of(col1), "col1", "A", "B\tC", "D");
      assertTrue(file.nextRow());
      assertEquals(1, file.currentRow());
      assertEquals("A", file.get(col1));
      // the second row has two values so an exception is thrown, but current
      // row is incremented so that processing can continue after the bad row
      final var e = assertThrows(DomainException.class, file::nextRow);
      assertEquals(errorMsg("too many columns", 2), e.getMessage());
      assertEquals(2, file.currentRow());
      // call nextRow to move to the third row and continue processing
      assertTrue(file.nextRow());
      assertEquals(3, file.currentRow());
      assertEquals("D", file.get(col1));
    }

    @Test
    void nextRowErrorForRowWithNotEnoughColumns() {
      final var file = create(List.of(col1, col2), "col1\tcol2", "A", "B\tC");
      final var e = assertThrows(DomainException.class, file::nextRow);
      assertEquals(errorMsg("not enough columns", 1), e.getMessage());
      // call nextRow to move to the second row and continue processing
      assertTrue(file.nextRow());
      assertEquals(2, file.currentRow());
      assertEquals("B", file.get(col1));
      assertEquals("C", file.get(col2));
    }

    @Test
    void getBeforeNextRowError() {
      final var file = create(List.of(col1), "col1", "Val");
      final var e = assertThrows(DomainException.class, () -> file.get(col1));
      assertEquals(errorMsg("'nextRow' must be called before calling 'get'"),
          e.getMessage());
    }

    @Test
    void getUnrecognizedColumnError() {
      final var file = create(List.of(col1), "col1", "Val");
      assertTrue(file.nextRow());
      final var col = new Column("Created After");
      final var e = assertThrows(DomainException.class, () -> file.get(col));
      assertEquals(errorMsg("unrecognized column 'Created After'", 1),
          e.getMessage());
    }

    @Test
    void getInvalidColumnError() {
      final var col = new Column("Created Before");
      final var file = create(List.of(col1), "col1", "Val");
      assertTrue(file.nextRow());
      final var e = assertThrows(DomainException.class, () -> file.get(col));
      assertEquals(errorMsg("invalid column 'Created Before'", 1),
          e.getMessage());
    }
  }
}
