package org.mpilone.helmsman;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.*;
import org.apache.commons.cli.*;
import org.mpilone.helmsman.UserIo.Level;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.mpilone.helmsman.OptionsFactory.*;
import static org.mpilone.helmsman.Strings.*;

/**
 * Air Control provides basic run level type controls of start, stop, and status
 * for a collection of services.
 */
public class Helmsman {

  /**
   * The width to pad command output when starting or stopping a command.
   */
  private static final int PADDING_WIDTH = 50;

  /**
   * The map of service names to service configurations.
   */
  private Map<String, ServiceConfig> serviceMap
      = new HashMap<String, ServiceConfig>();

  /**
   * The number of threads to use when executing in parallel. Anything less than
   * 2 indicates no parallelization.
   */
  private int threadCount = 1;

  /**
   * The global variables configured for replacement in service properties.
   */
  private Map<String, String> variables = new HashMap<String, String>();

  /**
   * The User IO to write all user output.
   */
  private final UserIo userIo = new UserIo();

  /**
   * The definition of supported command line options.
   */
  private final static Options OPTIONS = buildOptions();

  /**
   * Main entry point into the application.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    String baseDir = System.getProperty("basedir", ".");

    new Helmsman(baseDir, args);
  }

  /**
   * Constructs the main air control application.
   *
   * @param baseDir the base directory of the application to use as a default if
   * a specific configuration directory isn't specified
   * @param args the command line arguments
   */
  public Helmsman(String baseDir, String[] args) {

    // Parse the command line.
    org.apache.commons.cli.CommandLine cmd = null;
    try {
      CommandLineParser parser = new GnuParser();
      cmd = parser.parse(OPTIONS, args);

      // We shouldn't have any trailing arguments. If we do, the user did
      // something wrong.
      if (!cmd.getArgList().isEmpty()) {
        throw new RuntimeException("Unrecognized options: " + cmd.getArgList());
      }
    }
    catch (Exception ex) {
      userIo.println(ex.getMessage() + "\n");
      userIo.print(ex, Level.DEBUG);

      printHelp();
      return;
    }

    // Process the general OPTIONS.
    if (cmd.hasOption(OPT_VERBOSE)) {
      userIo.setLevel(Level.DEBUG);
    }
    if (cmd.hasOption(OPT_HELP)) {
      printHelp();
      return;
    }
    if (cmd.hasOption(OPT_PARALLEL)) {

      String value = cmd.getOptionValue(OPT_PARALLEL);
      if (value != null && !value.trim().isEmpty()) {
        threadCount = Math.max(Integer.parseInt(value), 1);
      }
      else {
        threadCount = Runtime.getRuntime().availableProcessors();
      }

      userIo.println(
          format("Enabling parallel execution with %d threads.", threadCount),
          Level.DEBUG);
    }

    // Parse the configuration.
    try {
      String configDir = baseDir + "/../config/";
      if (cmd.hasOption(OPT_CONFIG_DIR)) {
        configDir = cmd.getOptionValue(OPT_CONFIG_DIR);
      }
      parseConfig(configDir);
    }
    catch (Exception ex) {
      userIo.println("Failed to parse configuration file: " + ex.getMessage(),
          UserIo.Level.ERROR);
      userIo.print(ex, UserIo.Level.DEBUG);
      return;
    }

    // Process the group and services options.
    boolean confirmationNeeded = false;
    List<ServiceConfig> services = new ArrayList<ServiceConfig>();
    if (cmd.hasOption(OPT_GROUP)) {
      String group = cmd.getOptionValue(OPT_GROUP);
      for (ServiceConfig service : serviceMap.values()) {
        if (service.getGroups().contains(group)) {
          services.add(service);
        }
      }
    }
    else if (cmd.hasOption(OPT_NOT_GROUP)) {
      String group = cmd.getOptionValue(OPT_NOT_GROUP);
      for (ServiceConfig service : serviceMap.values()) {
        if (!service.getGroups().contains(group)) {
          services.add(service);
        }
      }
    }
    else if (cmd.hasOption(OPT_SERVICES)) {
      String[] serviceNames = cmd.getOptionValues(OPT_SERVICES);
      for (String serviceName : serviceNames) {
        ServiceConfig service = serviceMap.get(serviceName);
        if (service != null) {
          services.add(service);
        }
        else {
          userIo.println(format("Ignoring unknown service [%s].", serviceName));
        }
      }
    }
    else if (cmd.hasOption(OPT_NOT_SERVICES)) {
      List<String> serviceNames = asList(cmd.getOptionValues(OPT_NOT_SERVICES));
      for (ServiceConfig service : serviceMap.values()) {
        if (!serviceNames.contains(service.getName())) {
          services.add(service);
        }
      }
    }
    else {
      services.addAll(serviceMap.values());
      confirmationNeeded = !cmd.hasOption(OPT_QUIET);
    }

    // Process the command options.
    if (cmd.hasOption(OPT_START)) {
      if (confirmationNeeded
          && !userIo.confirm("Are you sure you want to start all services?")) {
        userIo.println("Aborting at user request.");
        return;
      }

      cmdStart(services);
    }
    else if (cmd.hasOption(OPT_STOP)) {
      if (confirmationNeeded
          && !userIo.confirm("Are you sure you want to stop all services?")) {
        userIo.println("Aborting at user request.");
        return;
      }

      cmdStop(services);
    }
    else if (cmd.hasOption(OPT_RESTART) || cmd.hasOption(OPT_BOUNCE)) {
      if (confirmationNeeded
          && !userIo.confirm("Are you sure you want to restart all services?")) {
        userIo.println("Aborting at user request.");
        return;
      }
      confirmationNeeded = false;

      cmdStop(services);
      cmdStart(services);
    }
    else if (cmd.hasOption(OPT_STATUS)) {
      cmdStatus(services);
    }
    else if (cmd.hasOption(OPT_LIST_GROUPS)) {
      cmdListGroups(services);
    }
  }

  /**
   * Prints the status of all the given services.
   *
   * @param services the services to print the status of
   */
  private void cmdStatus(List<ServiceConfig> services) {

    ServiceQueue queue = new ServiceQueue(services, threadCount > 1);

    for (List<ServiceConfig> bucket : queue) {

      Map<String, ServiceTask> tasks = new LinkedHashMap<String, ServiceTask>();
      for (ServiceConfig service : bucket) {
        tasks.put(service.getName(), new ServiceTask.Command(service, "status",
            userIo));
      }

      userIo.print(padRight(
          "Checking the status of " + join(summarize(tasks.keySet(), 3)), ".",
          PADDING_WIDTH));

      Map<String, Boolean> results = executeTasks(tasks, threadCount);
      printResults(results, "UP", "DOWN", threadCount > 1);
    }
  }

  /**
   * Executes the given tasks, in order, using up to the given thread count. The
   * result of each task is returned in a map of task name to result. The tasks
   * will be executed in the order of the task name key set iterator
   * (potentially in parallel).
   *
   * @param tasks the tasks to execute
   * @param threadCount the number of threads to use (must be at least 1)
   * @return the map of task name to success status
   */
  private Map<String, Boolean> executeTasks(Map<String, ServiceTask> tasks,
      int threadCount) {

    Iterator<String> taskNameIter = tasks.keySet().iterator();
    List<String> executing = new ArrayList<String>();
    Map<String, Boolean> results = new HashMap<String, Boolean>();

    while (results.size() != tasks.size()) {

      // Start tasks if possible.
      if (executing.size() < threadCount && taskNameIter.hasNext()) {
        String name = taskNameIter.next();
        ServiceTask task = tasks.get(name);

        task.execute();
        executing.add(name);
      }

      // Check status, wait for the first task.
      boolean first = true;
      for (Iterator<String> iter = executing.iterator(); iter.hasNext();) {
        String name = iter.next();
        ServiceTask task = tasks.get(name);

        if (first) {
          task.waitFor(2000);
          first = false;
          userIo.print(".");
        }

        if (task.isComplete()) {
          iter.remove();
          results.put(name, task.isSuccess());
        }
      }
    }

    return results;
  }

  /**
   * Stops all the given services.
   *
   * @param services the services to stop
   */
  private void cmdStop(List<ServiceConfig> services) {

    ServiceQueue queue = new ServiceQueue(services, threadCount > 1);
    queue.reverse();

    for (List<ServiceConfig> bucket : queue) {

      Map<String, ServiceTask> tasks = new LinkedHashMap<String, ServiceTask>();
      for (ServiceConfig service : bucket) {
        tasks.put(service.getName(), new ServiceTask.Or(new ServiceTask.Not(
            new ServiceTask.Command(service, "status", userIo)),
            new ServiceTask.Command(service, "stop", userIo)));
      }

      userIo.print(padRight("Stopping " + join(summarize(tasks.keySet(), 3)),
          ".", PADDING_WIDTH));

      Map<String, Boolean> results = executeTasks(tasks, threadCount);
      printResults(results, "DOWN", "FAILED", threadCount > 1);
    }
  }

  /**
   * Lists all the groups for each service and then a summary of all groups
   * defined.
   *
   * @param services the services to list
   */
  private void cmdListGroups(List<ServiceConfig> services) {
    Collections.sort(services, ServiceConfigComparator.ORDER_COMPARATOR);

    userIo.println("Groups by service:");
    Set<String> groups = new HashSet<String>();
    for (ServiceConfig service : services) {

      userIo.println(padRight(service.getName(), ".", PADDING_WIDTH)
          + alphaSort(service.getGroups()));

      groups.addAll(service.getGroups());
    }

    userIo.println("\nGroups summary: " + alphaSort(groups));
  }

  /**
   * Starts all the given services.
   *
   * @param services the services to start
   */
  private void cmdStart(List<ServiceConfig> services) {

    ServiceQueue queue = new ServiceQueue(services, threadCount > 1);

    for (List<ServiceConfig> bucket : queue) {

      Map<String, ServiceTask> tasks = new LinkedHashMap<String, ServiceTask>();
      for (ServiceConfig service : bucket) {
        tasks.put(service.getName(), new ServiceTask.Or(
            new ServiceTask.Command(service, "status", userIo),
            new ServiceTask.Command(service, "start", userIo)));
      }

      userIo.print(padRight("Starting " + join(summarize(tasks.keySet(), 3)),
          ".", PADDING_WIDTH));

      Map<String, Boolean> results = executeTasks(tasks, threadCount);
      printResults(results, "UP", "FAILED", threadCount > 1);
    }
  }

  /**
   * Prints the given results. If parallel, each result will be printed on a new
   * line, prefixed with the task name. If not parallel, the result will be
   * printed on the current line with no task name prefix.
   *
   * @param results the results to print
   * @param successCaption the caption if the task was successful
   * @param failCaption the caption if the task failed
   * @param parallelFormat true if the results should be printed in parallel
   * format, false otherwise
   */
  private void printResults(Map<String, Boolean> results,
      String successCaption, String failCaption, boolean parallelFormat) {

    if (parallelFormat) {
      userIo.println("done");

      for (Map.Entry<String, Boolean> result : results.entrySet()) {
        String resultCaption = result.getValue() ? successCaption : failCaption;

        String caption
            = format("\t%s%s", padRight(result.getKey(), ".", 20), resultCaption);

        userIo.println(caption);
      }
    }
    else {
      String resultCaption
          = results.values().iterator().next() ? successCaption : failCaption;

      userIo.println(resultCaption);
    }
  }

  /**
   * Prints the help/usage information to stdout.
   */
  private void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("helmsman", OPTIONS, true);
  }

  /**
   * Parses the configuration file and populates the global configuration and
   * service configurations.
   *
   * @param configDir the configuration directory to search for properties files
   * @throws Exception if parsing the configuration fails or it is invalid
   */
  private void parseConfig(String configDir) throws Exception {
    Properties config = new Properties();

    // Load base configuration
    File file = new File(new File(configDir), "base.properties");
    userIo.println("Loading configuration: " + file.getAbsolutePath(),
        UserIo.Level.DEBUG);
    Reader reader = new FileReader(file);
    config.load(reader);
    reader.close();

    // Load the machine specific configuration.
    String hostname = InetAddress.getLocalHost().getHostName();
    file = new File(new File(configDir), hostname + ".properties");
    userIo.println(
        "Checking for machine configuration: " + file.getAbsolutePath(),
        UserIo.Level.DEBUG);

    if (file.exists()) {
      userIo.println("Loading configuration: " + file.getAbsolutePath(),
          UserIo.Level.DEBUG);
      reader = new FileReader(file);
      config.load(reader);
      reader.close();
    }

    // Process the configuration.
    for (Enumeration<?> enumeration = config.propertyNames(); enumeration
        .hasMoreElements();) {
      String name = (String) enumeration.nextElement();
      String value = config.getProperty(name);

      if (name.equals("global.services")) {
        List<String> serviceNames = asList(value.split(","));

        for (String serviceName : serviceNames) {
          ServiceConfig serviceConfig = new ServiceConfig();
          serviceConfig.setName(serviceName.trim());
          serviceMap.put(serviceName, serviceConfig);
        }
      }
      else if (name.startsWith("global.var.")) {
        variables.put(name.split("\\.")[2], value);
      }
      else if (name.startsWith("service.")) {
        // Ignore for now
      }
      else {
        userIo.println(format(
            "Ignoring unrecognized configuration property [%s].", name));
      }
    }

    // Read the services configuration.
    for (Enumeration<?> enumeration = config.propertyNames(); enumeration
        .hasMoreElements();) {
      String name = (String) enumeration.nextElement();
      String value = config.getProperty(name).trim();

      if (name.startsWith("service.")) {

        String[] nameParts = name.split("\\.");

        // Make sure this service is supported in this configuration.
        ServiceConfig service = serviceMap.get(nameParts[1]);

        if (service != null) {
          userIo.println(format("Processing service property [%s].", name),
              UserIo.Level.DEBUG);

          if ("environment".equals(nameParts[2])) {
            service.getEnvironment().put(nameParts[3], value);
          }
          else if ("order".equals(nameParts[2])) {
            service.setOrder(Integer.parseInt(value));
          }
          else if ("script".equals(nameParts[2])) {
            service.setScript(replaceVariables(value, variables));
          }
          else if ("timeout".equals(nameParts[2])) {
            service.setTimeout(Integer.parseInt(value));
          }
          else if ("groups".equals(nameParts[2])) {
            for (String group : value.split(",")) {
              if (!group.trim().isEmpty()) {
                service.getGroups().add(group.trim());
              }
            }
          }
        }
        else {
          userIo.println(format("Ignoring service property [%s] "
              + "for unsupported service.", name), UserIo.Level.DEBUG);
        }
      }
    }

    // Validate the services configuration.
    for (ServiceConfig service : serviceMap.values()) {
      if (service.getScript() == null) {
        throw new RuntimeException(format(
            "Service [%s] does not have a valid script defined.",
            service.getName()));
      }
    }
  }

}
