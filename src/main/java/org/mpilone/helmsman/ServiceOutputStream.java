package org.mpilone.helmsman;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A simple output stream that writes all output using the {@link UserIo} at the
 * {@link UserIo.Level#DEBUG} level.
 *
 * @author mpilone
 */
public class ServiceOutputStream extends OutputStream {

  /**
   * The user IO to write to.
   */
  private UserIo userIo;

  /**
   * The UTF-8 character set.
   */
  private final static Charset UTF_8 = Charset.forName("UTF-8");

  /**
   * Constructs the output stream.
   *
   * @param out the user IO to write to
   */
  public ServiceOutputStream(UserIo out) {
    this.userIo = out;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write(int value) throws IOException {
    userIo.print(new String(new byte[]{(byte) value}, UTF_8),
        UserIo.Level.DEBUG);
  }

}
