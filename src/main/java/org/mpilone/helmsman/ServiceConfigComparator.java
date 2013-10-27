package org.mpilone.helmsman;

import java.util.Comparator;

/**
 * A collection of {@link Comparator}s for sorting {@link ServiceConfig}.
 *
 * @author mpilone
 */
public abstract class ServiceConfigComparator {

  /**
   * A comparator that sorts {@link ServiceConfig} based on priority.
   *
   * @author mpilone
   */
  public static class Order implements Comparator<ServiceConfig> {
    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */

    @Override
    public int compare(ServiceConfig s1, ServiceConfig s2) {
      Integer i1 = new Integer(s1.getOrder());
      Integer i2 = new Integer(s2.getOrder());

      return i1.compareTo(i2);
    }
  }

  /**
   * A comparator that sorts {@link ServiceConfig} based on name.
   *
   * @author mpilone
   */
  public static class Name implements Comparator<ServiceConfig> {
    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */

    @Override
    public int compare(ServiceConfig s1, ServiceConfig s2) {
      return s1.getName().compareTo(s2.getName());
    }
  }

  /**
   * A reusable static instance.
   */
  public static final Order ORDER_COMPARATOR = new Order();

  /**
   * A reusable static instance.
   */
  public static final Name NAME_COMPARATOR = new Name();

}
