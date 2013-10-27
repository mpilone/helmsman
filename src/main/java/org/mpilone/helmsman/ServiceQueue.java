package org.mpilone.helmsman;

import java.util.*;

/**
 * An ordered queue of services grouped by order level if parallel or
 * individually if not parallel. The queue handles sorting by order (i.e.
 * priority) and service name.
 *
 * @author mpilone
 */
public class ServiceQueue implements Iterable<List<ServiceConfig>> {

  /**
   * The buckets of services that can run in parallel.
   */
  private List<List<ServiceConfig>> buckets;

  /**
   * Constructs the queue which will bucket and sort the services appropriately.
   * If parallelization is enabled, the buckets will be based on service order
   * level. Otherwise each service will be queued separately for serial
   * execution.
   *
   * @param services the services to queue
   * @param parallel true if parallelization is enabled, false otherwise
   */
  public ServiceQueue(List<ServiceConfig> services, boolean parallel) {

    buckets = new ArrayList<List<ServiceConfig>>();

    // If parallel, we need to group services by their order value so they can
    // be run in parallel. If not parallel, we'll put each service in the queue
    // independently.
    Collections.sort(services, ServiceConfigComparator.ORDER_COMPARATOR);

    if (!parallel) {
      for (ServiceConfig service : services) {
        List<ServiceConfig> bucket = new ArrayList<ServiceConfig>();
        bucket.add(service);
        buckets.add(bucket);
      }
    }
    else {
      int current = Integer.MIN_VALUE;
      List<ServiceConfig> bucket = null;

      for (ServiceConfig service : services) {

        if (bucket == null || current != service.getOrder()) {
          current = service.getOrder();
          bucket = new ArrayList<ServiceConfig>();
          buckets.add(bucket);
        }

        bucket.add(service);
      }
    }

    // Within each bucket, sort alphabetically for some consistency.
    for (List<ServiceConfig> bucket : buckets) {
      Collections.sort(bucket, ServiceConfigComparator.NAME_COMPARATOR);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<List<ServiceConfig>> iterator() {
    return Collections.unmodifiableList(buckets).iterator();
  }

  /**
   * Reverses the order of the overall queue and each bucket within the queue.
   * This method should be called before obtaining an iterator.
   */
  public void reverse() {
    Collections.reverse(buckets);
    for (List<ServiceConfig> bucket : buckets) {
      Collections.reverse(bucket);
    }
  }
}
