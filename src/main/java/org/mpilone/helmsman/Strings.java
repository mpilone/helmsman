package org.mpilone.helmsman;

import java.util.*;

import static java.lang.String.format;

/**
 * String utility methods.
 *
 * @author mpilone
 */
public class Strings {

  /**
   * Sorts the given values alphabetically using their natural ordering.
   *
   * @param values the values to sort
   * @return the sorted values
   */
  public static Collection<String> alphaSort(Collection<String> values) {
    List<String> sortedList = new ArrayList<String>(values);
    Collections.sort(sortedList);

    return sortedList;
  }

  /**
   * Joins the given strings into a single string using ", " (comma space) as
   * the separator.
   *
   * @param values the values to join
   * @return the single, joined string
   */
  public static String join(Collection<String> values) {
    StringBuilder b = new StringBuilder();

    for (String value : values) {
      if (b.length() != 0) {
        b.append(", ");
      }
      b.append(value);
    }

    return b.toString();
  }

  /**
   * Summarizes the values into a shorter list if the number of items is more
   * than the desired item count. A summary string will be appended if needed.
   * For example, if values were [foo, bar, poo, moo, goo] and the item count
   * was 2, the resulting list would be [foo, bar, and 3 others]. Therefore the
   * returned list will always be &lt;= (itemCount + 1).
   *
   * @param values the values to summarize
   * @param itemCount the number of original items to keep
   * @return the summarized list
   */
  public static Collection<String> summarize(Collection<String> values,
      int itemCount) {

    Collection<String> result;
    if (values.size() <= itemCount) {
      result = values;
    }
    else {
      result = new ArrayList<String>(itemCount + 1);

      int index = 0;
      for (Iterator<String> iter = values.iterator(); iter.hasNext()
          && index < itemCount; index++) {

        String value = iter.next();
        result.add(value);
      }

      result.add(format("and %d others", values.size() - itemCount));
    }

    return result;
  }

  /**
   * Pads the given string on the right with the given padding until the length
   * of the string is equal to or greater than the given width.
   *
   * @param value the value to pad
   * @param padding the padding character(s)
   * @param width the minimum width
   * @return the padded string
   */
  public static String padRight(String value, String padding, int width) {
    while (value.length() < width) {
      value += padding;
    }

    return value;
  }

  /**
   * Replaces all the variables in the given string with any values found in the
   * map.
   *
   * @param value the string to replace variables in
   * @param variables the variables to use as replacements
   * @return the modified string
   */
  public static String replaceVariables(String value,
      Map<String, String> variables) {
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      value
          = value.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
    }

    return value;
  }

}
