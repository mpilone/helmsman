package org.mpilone.helmsman;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User input/output utility methods.
 *
 * @author mpilone
 */
public class UserIo {

  /**
   * Log levels.
   *
   * @author mpilone
   */
  public static enum Level {

    DEBUG, INFO, ERROR
  }

  /**
   * The lowest level message to write out to the user.
   */
  private Level level = Level.INFO;

  /**
   * Displays a confirmation prompt to the user and reads a yes/no response.
   *
   * @param prompt the prompt to display
   * @return true if the user answered yes, false otherwise
   */
  public boolean confirm(String prompt) {

    while (true) {
      try {
        System.out.print(prompt + " [y/n]: ");
        char c = (char) System.in.read();
        if (c == 'y') {
          return true;
        }
        else if (c == 'n') {
          return false;
        }
      }
      catch (Exception ex) {
        throw new RuntimeException("Error prompting user.", ex);
      }
    }
  }

  /**
   * Sets the lowest level message to output to the user.
   *
   * @param level the new log level
   */
  public void setLevel(Level level) {
    this.level = level;
  }

  /**
   * Prints the given exception to the user.
   *
   * @param ex the exception to print
   * @param level the level of message
   */
  public void print(Exception ex, Level level) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    ex.printStackTrace(writer);
    writer.close();

    println(stringWriter.toString(), level);
  }

  /**
   * Prints a blank line at the INFO level.
   */
  public void println() {
    println("", Level.INFO);
  }

  /**
   * Prints the given message at the given level with a trailing newline.
   *
   * @param msg the message to print
   * @param level the level of the message
   */
  public void println(String msg, Level level) {
    print(msg + "\n", level);
  }

  /**
   * Prints the given message to the user.
   *
   * @param msg the message to print
   * @param level the level of message
   */
  public synchronized void print(String msg, Level level) {
    switch (this.level) {
      case DEBUG:
        System.out.print(msg);
        break;

      case INFO:
        if (level == Level.INFO || level == Level.ERROR) {
          System.out.print(msg);
        }
        break;

      case ERROR:
        System.out.print(msg);
        break;

    }
  }

  /**
   * Prints the given message to the user at the INFO level.
   *
   * @param msg the message to print
   */
  public void print(String msg) {
    print(msg, Level.INFO);
  }

  /**
   * Prints the given message to the user at the INFO level with a trailing
   * newline.
   *
   * @param msg the message to print
   */
  public void println(String msg) {
    println(msg, Level.INFO);
  }
}
