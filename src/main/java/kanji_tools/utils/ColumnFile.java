package kanji_tools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * class for loading data from a delimiter separated file with a header row
 * containing the column names
 */
public class ColumnFile {

  private static final HashMap<String, Integer> allColumns = new HashMap<>();
  private static final int COLUMN_NOT_FOUND = -1;

  private final String fileName;
  private final String delimiter;
  private final BufferedReader reader;
  private final String[] rowValues;
  private final int[] columnToPosition;
  private int currentRow = 0;

  /**
   * create a ColumnFile and processes the first 'header' row. Column order in
   * the file is determined by reading the 'header' row - each header name must
   * be unique and exactly match the name of a Column in {@code columns}.
   *
   * @param path      text file to be read and processed
   * @param columns   set of columns in the file
   * @param delimiter column delimiter
   * @throws DomainException path doesn't exist or failed to read headers or
   *                         headers don't match {@code columns}
   */
  public ColumnFile(String path, Set<Column> columns, String delimiter) {
    if (columns.isEmpty())
      throw new DomainException("must specify at least one column");

    final var file = new File(path);
    fileName = file.getName();
    this.delimiter = delimiter;
    rowValues = new String[columns.size()];
    columnToPosition = new int[allColumns.size()];
    Arrays.fill(columnToPosition, COLUMN_NOT_FOUND);

    // process the 'header' row
    try {
      reader = new BufferedReader(new FileReader(file));
      var headerRow = reader.readLine();
      if (headerRow == null)
        throw error("missing header row");
      processHeaderRow(headerRow, columns);
    } catch (IOException e) {
      throw new DomainException("failed to read header row: " + e.getMessage());
    }
  }

  /**
   * constructor for a tab-delimited ColumnFile
   *
   * @see #ColumnFile(String, Set, String) for details
   */
  public ColumnFile(String path, Set<Column> columns) {
    this(path, columns, "\t");
  }

  private static int getColumnNumber(String name) {
    return allColumns.computeIfAbsent(name, k -> allColumns.size());
  }

  /**
   * @return number of columns in this file
   */
  public int numColumns() {
    return rowValues.length;
  }

  /**
   * @return current row number, 0 indicated no data rows have been read yet
   */
  public int currentRow() {
    return currentRow;
  }

  /**
   * read the next row, this method must be called before using get methods
   *
   * @return true if a row was successfully read
   * @throws DomainException can't read next row or incorrect number of columns
   */
  public boolean nextRow() {
    try {
      final var row = reader.readLine();
      if (row != null) {
        ++currentRow;
        var pos = 0;
        // pass -1 to split to force it to keep empty strings at the end
        for (var field : row.split(delimiter, -1)) {
          if (pos == numColumns())
            throw error("too many columns");
          rowValues[pos++] = field;
        }
        if (pos < numColumns())
          throw error("not enough columns");
        return true;
      }
    } catch (IOException e) {
      throw error("failed to read next row: " + e.getMessage());
    }
    return false;
  }

  /**
   * @return the value for the given Column for the current row
   * @throws DomainException if nextRow hasn't been called yet or the given
   *                         column isn't part of this file, i.e., it wasn't
   *                         passed into the ctor
   */
  public String get(Column column) {
    if (currentRow == 0)
      throw error("'nextRow' must be called before calling 'get'");
    if (column.getNumber() >= columnToPosition.length)
      throw error("unrecognized column '" + column + "'");
    final var pos = columnToPosition[column.getNumber()];
    if (pos == COLUMN_NOT_FOUND)
      throw error("invalid column '" + column + "'");
    return rowValues[pos];
  }

  private void processHeaderRow(String row, Set<Column> columns) {
    final var cols = columns.stream()
        .collect(Collectors.toMap(Column::getName, Function.identity()));
    final var foundCols = new HashSet<String>();
    var pos = 0;
    for (var header : row.split(delimiter)) {
      if (!foundCols.add(header))
        throw error("duplicate header '" + header + "'");
      final var c = cols.remove(header);
      if (c == null)
        throw error("unrecognized header '" + header + "'");
      columnToPosition[c.getNumber()] = pos++;
    }
    if (cols.size() == 1)
      throw error("column '" + cols.keySet().iterator().next() + "' not found");
    if (cols.size() > 1)
      throw error(cols.size() + " columns not found: '" + cols.keySet().stream()
          .sorted().collect(Collectors.joining("', '")) + "'");
  }

  private DomainException error(String msg) {
    var result = msg + " - file: " + fileName;
    if (currentRow > 0)
      result += ", row: " + currentRow;
    return new DomainException(result);
  }

  public static final class Column {
    private final String name;
    private final int number;

    public Column(String name) {
      this.name = name;
      this.number = getColumnNumber(name);
    }

    public String getName() {
      return name;
    }

    public int getNumber() {
      return number;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Column x && number == x.number;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
