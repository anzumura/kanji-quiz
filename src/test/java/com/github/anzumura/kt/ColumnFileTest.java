package com.github.anzumura.kt;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.github.anzumura.kt.ColumnFile.Column;
import static org.junit.jupiter.api.Assertions.*;

class ColumnFileTest {
  private static final String testFile = "test.txt";
  private static final Column col1 = new Column("col1"), col2 =
      new Column("col2"), col3 = new Column("col3");

  @TempDir
  private Path tempDir;

  private static String errorMsg(String msg) {
    return msg + " - file: " + testFile;
  }

  private static String errorMsg(String msg, int row) {
    return errorMsg(msg) + ", row: " + row;
  }

  private static String errorMsg(String msg, int row, Column c, String s) {
    return errorMsg(msg, row) + ", column: '" + c + "', value: '" + s + "'";
  }

  private ColumnFile create(
      String delimiter, Set<Column> columns, String... lines) {
    try {
      final var path = Files.createFile(tempDir.resolve(testFile));
      Files.write(path, List.of(lines));
      // use 'null' delimiter to allow testing constructor overloads, i.e., null
      // means use the constructor that doesn't take a delimiter (and instead
      // passes in "\t" in the main code)
      return delimiter == null ? new ColumnFile(path, columns) :
          new ColumnFile(path, columns, delimiter);
    } catch (IOException e) {
      return fail("failed to create file - " + e.getMessage());
    }
  }

  private ColumnFile create(Set<Column> columns, String... lines) {
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

    @Test
    void equalsTest() {
      final var same = "same name";
      final Column c1 = new Column(same), c2 = new Column(same);
      Object x = same;
      assertNotEquals(c1, x); // classes are different
      x = c2;
      assertEquals(c1, x);
      assertNotEquals(c1, new Column("different name"));
    }
  }

  @Nested
  class ConstructorTest {
    @Test
    void createSingleColumnFile() {
      assertEquals(1, create(Set.of(col1), "col1").numColumns());
    }

    @Test
    void createMultiColumnFile() {
      assertEquals(2, create(Set.of(col1, col2), "col1\tcol2").numColumns());
    }

    @Test
    void createSpaceDelimitedFile() {
      assertEquals(2,
          create(" ", Set.of(col1, col2), "col1 col2").numColumns());
    }

    @Test
    void currentRowIsZeroBeforeAnyDataRowsAreProcessed() {
      assertEquals(0, create(Set.of(col1), "col1").currentRow());
    }

    @Test
    void emptyColumnsError() {
      final var e = assertThrows(DomainException.class, () -> create(Set.of()));
      assertEquals("must specify at least one column", e.getMessage());
    }

    @Test
    void missingFileError() {
      final var e = assertThrows(DomainException.class,
          () -> new ColumnFile(Path.of(testFile), Set.of(col1)));
      assertEquals("failed to read header row: " + testFile, e.getMessage());
    }

    @Test
    void missingHeaderRowError() {
      final var e =
          assertThrows(DomainException.class, () -> create(Set.of(col1)));
      assertEquals(errorMsg("missing header row"), e.getMessage());
    }

    @Test
    void duplicateHeaderError() {
      final var e = assertThrows(DomainException.class,
          () -> create(Set.of(col1), "col1\tcol1"));
      assertEquals(errorMsg("duplicate header 'col1'"), e.getMessage());
    }

    @Test
    void unrecognizedHeaderError() {
      final var e = assertThrows(DomainException.class,
          () -> create(Set.of(col1), "col1\tcol2"));
      assertEquals(errorMsg("unrecognized header 'col2'"), e.getMessage());
    }

    @Test
    void oneMissingColumnError() {
      final var e = assertThrows(DomainException.class,
          () -> create(Set.of(col1, col2), "col1"));
      assertEquals(errorMsg("column 'col2' not found"), e.getMessage());
    }

    @Test
    void multipleMissingColumnsError() {
      final var e = assertThrows(DomainException.class,
          () -> create(Set.of(col1, col2, col3), "col2"));
      assertEquals(errorMsg("2 columns not found: 'col1', 'col3'"),
          e.getMessage());
    }
  }

  @Nested
  class NextRowTest {
    @Test
    void calledAfterCloseError() {
      final var f = create(Set.of(col1), "col1");
      assertFalse(f.nextRow());
      final var e = assertThrows(DomainException.class, f::nextRow);
      assertEquals("file: " + testFile + "' has been closed", e.getMessage());
    }

    @Test
    void tooManyColumnsError() {
      final var f = create(Set.of(col1), "col1", "A", "B\tC", "D");
      assertTrue(f.nextRow());
      assertEquals(1, f.currentRow());
      assertEquals("A", f.get(col1));
      // the second row has two values so an exception is thrown, but current
      // row is incremented so that processing can continue after the bad row
      final var e = assertThrows(DomainException.class, f::nextRow);
      assertEquals(errorMsg("too many columns", 2), e.getMessage());
      assertEquals(2, f.currentRow());
      // call nextRow to move to the third row and continue processing
      assertTrue(f.nextRow());
      assertEquals(3, f.currentRow());
      assertEquals("D", f.get(col1));
    }

    @Test
    void notEnoughColumnsError() {
      final var f = create(Set.of(col1, col2), "col1\tcol2", "A", "B\tC");
      final var e = assertThrows(DomainException.class, f::nextRow);
      assertEquals(errorMsg("not enough columns", 1), e.getMessage());
      // call nextRow to move to the second row and continue processing
      assertTrue(f.nextRow());
      assertEquals(2, f.currentRow());
      assertEquals("B", f.get(col1));
      assertEquals("C", f.get(col2));
    }

    @Test
    void failedRead() throws IOException {
      final var path = Files.createFile(tempDir.resolve(testFile));
      Files.write(path, List.of("col1"));
      final var f = new ColumnFile(path, Set.of(col1)) {
        @Override
        protected String readRow() throws IOException {
          throw new IOException("read failed");
        }
      };
      final var e = assertThrows(DomainException.class, f::nextRow);
      assertEquals("failed to read next row: read failed - file: test.txt",
          e.getMessage());
    }

    @Test
    void failedClose() throws IOException {
      final var path = Files.createFile(tempDir.resolve(testFile));
      Files.write(path, List.of("col1"));
      final var f = new ColumnFile(path, Set.of(col1)) {
        @Override
        protected void closeReader() throws IOException {
          throw new IOException("close failed");
        }
      };
      final var e = assertThrows(DomainException.class, f::nextRow);
      assertEquals("failed to close reader: close failed", e.getMessage());
    }
  }

  @Nested
  class GetDataTest {
    @Test
    void stringValue() {
      final var expected = "Val";
      final var f = create(Set.of(col1), "col1", expected);
      assertTrue(f.nextRow());
      assertEquals(expected, f.get(col1));
    }

    @Test
    void canGetValuesAtEndOfFile() {
      final var f = create(Set.of(col1, col2), "col1\tcol2", "A\tB");
      assertTrue(f.nextRow());
      assertEquals(1, f.currentRow());
      assertEquals("A", f.get(col1));
      assertEquals("B", f.get(col2));
      assertFalse(f.nextRow());
      // current row is still the last successfully processed row
      assertEquals(1, f.currentRow());
      assertEquals("A", f.get(col1));
      assertEquals("B", f.get(col2));
    }

    @Test
    void valuesCanBeBlank() {
      final var f =
          create(Set.of(col1, col2, col3), "col1\tcol2\tcol3", "\tB\tC",
              "A\t\tC", "\t\t");
      f.nextRow(); // first value is empty
      assertEquals("", f.get(col1));
      assertEquals("B", f.get(col2));
      assertEquals("C", f.get(col3));
      f.nextRow(); // second value is empty
      assertEquals("A", f.get(col1));
      assertEquals("", f.get(col2));
      assertEquals("C", f.get(col3));
      f.nextRow(); // all values are empty
      assertEquals("", f.get(col1));
      assertEquals("", f.get(col2));
      assertEquals("", f.get(col3));
      // make sure all data has been read
      assertFalse(f.nextRow());
      assertEquals(3, f.currentRow());
    }

    @Test
    void getBeforeNextRowError() {
      final var f = create(Set.of(col1), "col1", "Val");
      final var e = assertThrows(DomainException.class, () -> f.get(col1));
      assertEquals(errorMsg("'nextRow' must be called before calling 'get'"),
          e.getMessage());
    }

    @Test
    void unrecognizedColumnError() {
      final var f = create(Set.of(col1), "col1", "Val");
      assertTrue(f.nextRow());
      final var col = new Column("Created After");
      final var e = assertThrows(DomainException.class, () -> f.get(col));
      assertEquals(errorMsg("unrecognized column 'Created After'", 1),
          e.getMessage());
    }

    @Test
    void invalidColumnError() {
      final var col = new Column("Created Before");
      final var f = create(Set.of(col1), "col1", "Val");
      assertTrue(f.nextRow());
      final var e = assertThrows(DomainException.class, () -> f.get(col));
      assertEquals(errorMsg("invalid column 'Created Before'", 1),
          e.getMessage());
    }

    @Test
    void getUnsignedInt() {
      final var f = create(Set.of(col1, col2), "col1\tcol2", "0\t123");
      f.nextRow();
      assertEquals(0, f.getUnsignedInt(col1));
      assertEquals(123, f.getUnsignedInt(col2));
    }

    @Test
    void getUnsignedIntError() {
      final var f = create(Set.of(col1, col2), "col1\tcol2", "bad\t-123");
      f.nextRow();
      var e = assertThrows(DomainException.class, () -> f.getUnsignedInt(col1));
      assertEquals(errorMsg("convert to unsigned int failed", 1, col1, "bad"),
          e.getMessage());
      e = assertThrows(DomainException.class, () -> f.getUnsignedInt(col2));
      assertEquals(errorMsg("convert to unsigned int failed", 1, col2, "-123"),
          e.getMessage());
    }

    @Test
    void getUnsignedIntWithMaxValue() {
      final var f = create(Set.of(col1), "col1", "0", "123");
      f.nextRow();
      assertEquals(0, f.getUnsignedInt(col1, 0));
      f.nextRow();
      assertEquals(123, f.getUnsignedInt(col1, -1));
      assertEquals(123, f.getUnsignedInt(col1, 123));
      assertEquals(123, f.getUnsignedInt(col1, Integer.MAX_VALUE));
    }

    @Test
    void getUnsignedIntWithMaxValueError() {
      final var f = create(Set.of(col1), "col1", "18", "100");
      f.nextRow();
      var e =
          assertThrows(DomainException.class, () -> f.getUnsignedInt(col1, 0));
      assertEquals(errorMsg("exceeded max value of 0", 1, col1, "18"),
          e.getMessage());
      f.nextRow();
      e = assertThrows(DomainException.class, () -> f.getUnsignedInt(col1, 99));
      assertEquals(errorMsg("exceeded max value of 99", 2, col1, "100"),
          e.getMessage());
    }

    @Test
    void getBoolean() {
      final var f =
          create(Set.of(col1, col2, col3), "col1\tcol2\tcol3", "Y\tT\tx",
              "N\tF\t");
      f.nextRow();
      assertAll(() -> assertTrue(f.getBoolean(col1)),
          () -> assertTrue(f.getBoolean(col2)), () -> {
            final var e =
                assertThrows(DomainException.class, () -> f.getBoolean(col3));
            assertEquals(errorMsg("convert to boolean failed", 1, col3, "x"),
                e.getMessage());
          });
      f.nextRow();
      assertAll(() -> assertFalse(f.getBoolean(col1)),
          () -> assertFalse(f.getBoolean(col2)),
          () -> assertFalse(f.getBoolean(col3)));
    }
  }
}
