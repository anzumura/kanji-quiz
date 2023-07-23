package kanji_tools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnFile {

  private static final HashMap<String, Integer> allColumns = new HashMap<>();
  private static final int COLUMN_NOT_FOUND = -1;
  private static final String DELIMITER = " ";

  private final String fileName;
  private final BufferedReader reader;
  private final String[] rowValues;
  private final int[] columnToPosition;
  private int currentRow = 0;

  public ColumnFile(String path, List<Column> columns) throws IOException {
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

  private static int getColumnNumber(String name) {
    return allColumns.computeIfAbsent(name, k -> allColumns.size());
  }

  private void processHeaderRow(String row, Map<String, Column> colNames) {
    var pos = 0;
    final var foundCols = new HashSet<String>();
    for (var header : row.split(DELIMITER)) {
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
