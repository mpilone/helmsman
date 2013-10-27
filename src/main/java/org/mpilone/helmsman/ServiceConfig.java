package org.mpilone.helmsman;

import java.util.*;

/**
 * Configuration of a single service to be managed.
 *
 * @author mpilone
 */
public class ServiceConfig {

  private String script;
  private final Map<String, String> environment = new HashMap<String, String>();
  private final List<String> groups = new ArrayList<String>();
  private String name;
  private int order = 1;
  private int timeout = 300;

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getTimeout() {
    return timeout;
  }

  public Map<String, String> getEnvironment() {
    return environment;
  }

  public List<String> getGroups() {
    return groups;
  }

  public String getName() {
    return name;
  }

  public int getOrder() {
    return order;
  }

  public String getScript() {
    return script;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public void setOrder(int priority) {
    this.order = priority;
  }

}
