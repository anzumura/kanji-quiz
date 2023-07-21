package kanji_tools.utils;

import java.util.HashMap;

public class ColumnFile {

  public static final class Column {
    public Column(String name) {
      this.name = name;
      this.number = getColumnNumber(name);
    }

    public String getName() { return name; }
    public int getNumber() { return number; }

    @Override public boolean equals(Object other) {
      return other instanceof Column x && number == x.number;
    }

    private final String name;
    private final int number;
  }

  private static int getColumnNumber(String name) {
    return allColumns.computeIfAbsent(name, k -> allColumns.size());
  }

  private static final HashMap<String, Integer> allColumns = new HashMap<>();
}
