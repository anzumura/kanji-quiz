package com.github.anzumura.kt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
  private static final int COLUMN_NOT_FOUND = -1, NO_MAX_VALUE = -1;

  private final String fileName;
  private final String delimiter;
  private final BufferedReader reader;
  private final String[] rowValues;
  private final int[] columnToPosition;
  private int currentRow = 0;
  private boolean closed = false;

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
  public ColumnFile(Path path, Set<Column> columns, String delimiter) {
    if (columns.isEmpty())
      throw new DomainException("must specify at least one column");

    fileName = path.getFileName().toString();
    this.delimiter = delimiter;
    rowValues = new String[columns.size()];
    columnToPosition = new int[allColumns.size()];
    Arrays.fill(columnToPosition, COLUMN_NOT_FOUND);

    // process the 'header' row
    try {
      reader = Files.newBufferedReader(path);
      var headerRow = reader.readLine();
      if (headerRow == null)
        throw error("missing header row");
      processHeaderRow(headerRow, columns);
    } catch (IOException e) {
      throw new DomainException("failed to read header row: " + e.getMessage());
    }
  }

  /**
   * calls {@link #ColumnFile(Path, Set, String)} with delimiter set to 'tab'
   *
   * @param path    text file to be read and processed
   * @param columns set of columns in the file
   */
  public ColumnFile(Path path, Set<Column> columns) {
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
   * read next row, this method must be called before calling get methods. If
   * there's no more rows then false is returned and the file is closed - thus
   * calling nextRow again after the file is closed raises an exception.
   *
   * @return true if a row was read or false if there is no more data
   * @throws DomainException if reading the next row fails or has incorrect
   *                         number of columns
   */
  public boolean nextRow() {
    if (closed)
      throw new DomainException("file: " + fileName + "' has been closed");
    if (processNextRow())
      return true;
    try {
      closeReader();
      closed = true;
    } catch (IOException e) {
      throw new DomainException("failed to close reader: " + e.getMessage());
    }
    return false;
  }

  /**
   * @param column column contained in this file
   * @return string value for the given {@code column} in current row
   * @throws DomainException if nextRow hasn't been called yet or the given
   *                         column isn't part of this file
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

  /**
   * @param column column contained in this file
   * @return unsigned int value for the given {@code column} in current row
   * @throws DomainException if {@link #get} fails or value can't be converted
   *                         to an unsigned int
   */
  public int getUnsignedInt(Column column) {
    return processUnsignedInt(get(column), column, NO_MAX_VALUE);
  }

  /**
   * @param column   column contained in this file
   * @param maxValue maximum value allowed (check is only applied if maxValue is
   *                 non-negative)
   * @return unsigned int value for the given {@code column} in current row
   * @throws DomainException if unable to get unsigned int less than or equal to
   *                         {@code maxValue}
   */
  public int getUnsignedInt(Column column, int maxValue) {
    return processUnsignedInt(get(column), column, maxValue);
  }

  /**
   * @param column column contained in this file
   * @return true for "Y" or "T", false for "N", "F" or ""
   * @throws DomainException if {@link #get} fails or value is unrecognized
   */
  public boolean getBoolean(Column column) {
    final var s = get(column);
    return switch (s) {
      case "Y", "T" -> true;
      case "N", "F", "" -> false;
      default -> throw error("convert to boolean failed", column, s);
    };
  }

  protected String readRow() throws IOException {
    return reader.readLine();
  }

  protected void closeReader() throws IOException {
    reader.close();
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

  private boolean processNextRow() {
    try {
      final var row = readRow();
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

  private int processUnsignedInt(String s, Column column, int max) {
    int result;
    try {
      result = Integer.parseUnsignedInt(s);
    } catch (NumberFormatException e) {
      throw error("convert to unsigned int failed", column, s);
    }
    if (max >= 0 && max < result)
      throw error("exceeded max value of " + max, column, s);
    return result;
  }

  private DomainException error(String msg) {
    return new DomainException(errorMsg(msg));
  }

  private DomainException error(String msg, Column column, String s) {
    return new DomainException(
        errorMsg(msg) + ", column: '" + column + "', " + "value: '" + s + "'");
  }

  private String errorMsg(String msg) {
    var result = msg + " - file: " + fileName;
    if (currentRow > 0)
      result += ", row: " + currentRow;
    return result;
  }

  /**
   * represents a column in a {@code ColumnFile}. Instances are used to get
   * values from each row and the same Column instance can be used in multiple
   * ColumnFiles.
   */
  public static final class Column {
    private final String name;
    private final int number;

    /**
     * @param name name of column used in file header rows
     */
    public Column(String name) {
      this.name = name;
      this.number = getColumnNumber(name);
    }

    /**
     * @return column name
     */
    public String getName() {
      return name;
    }

    /**
     * @return column number (numbers are globally unique per name)
     */
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
