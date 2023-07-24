package kanji_tools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
   * create a ColumnFile and processes the first 'header' row. The list of
   * columns doesn't need to be in any particular order as long as each column
   * exists in the file.
   *
   * @param path      text file to be read and processed
   * @param columns   list of columns in the file (can be specified in any
   *                  order)
   * @param delimiter column delimiter
   * @throws IOException              if path cannot be opened or read from
   * @throws IllegalArgumentException if column list is empty or has duplicates
   * @throws DomainException          if the text file is missing a header
   *                                  row or the header row doesn't have the
   *                                  same columns as the column list provided
   */
  public ColumnFile(String path, List<Column> columns, String delimiter) throws
      IOException {
    // check columns is non-empty and doesn't contain duplicates
    if (columns.isEmpty())
      throw new IllegalArgumentException("must specify at least one column");
    final var colNames = new HashMap<String, Column>();
    for (var c : columns)
      if (colNames.put(c.getName(), c) != null)
        throw new IllegalArgumentException("duplicate column '" + c + "'");
    final var file = new File(path);

    // set instance fields
    fileName = file.getName();
    this.delimiter = delimiter;
    reader = new BufferedReader(new FileReader(file));
    rowValues = new String[columns.size()];
    columnToPosition = new int[allColumns.size()];
    Arrays.fill(columnToPosition, COLUMN_NOT_FOUND);

    // process the 'header' row
    final var headerRow = reader.readLine();
    if (headerRow == null)
      throw error("missing header row");
    processHeaderRow(headerRow, colNames);
  }

  /**
   * constructor for a tab-delimited ColumnFile
   *
   * @see #ColumnFile(String, List, String) for details
   */
  public ColumnFile(String path, List<Column> columns) throws IOException {
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
   * @throws DomainException if the next row cannot be read or has the wrong
   *                         number of columns
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

  private void processHeaderRow(String row, Map<String, Column> colNames) {
    var pos = 0;
    final var foundCols = new HashSet<String>();
    for (var header : row.split(delimiter)) {
      if (!foundCols.add(header))
        throw error("duplicate header '" + header + "'");
      final var c = colNames.remove(header);
      if (c == null)
        throw error("unrecognized header '" + header + "'");
      columnToPosition[c.getNumber()] = pos++;
    }
    if (colNames.size() == 1)
      throw error(
          "column '" + colNames.keySet().iterator().next() + "' not found");
    if (colNames.size() > 1)
      throw error(
          colNames.size() + " columns not found: '" + colNames.keySet().stream()
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
