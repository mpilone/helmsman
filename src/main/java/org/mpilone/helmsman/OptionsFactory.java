package org.mpilone.helmsman;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Simple factory for building options.
 *
 * @author mpilone
 */
public class OptionsFactory {

  public final static String OPT_CONFIG_DIR = "configDir";
  public final static String OPT_VERBOSE = "verbose";
  public final static String OPT_HELP = "help";
  public final static String OPT_PARALLEL = "parallel";
  public final static String OPT_QUIET = "quiet";
  public final static String OPT_START = "start";
  public final static String OPT_STOP = "stop";
  public final static String OPT_STATUS = "status";
  public final static String OPT_RESTART = "restart";
  public final static String OPT_BOUNCE = "bounce";
  public final static String OPT_SERVICES = "services";
  public final static String OPT_NOT_SERVICES = "not-services";
  public final static String OPT_GROUP = "group";
  public final static String OPT_NOT_GROUP = "not-group";
  public final static String OPT_LIST_GROUPS = "list-groups";

  /**
   * Constructs the definitions of supported command line options.
   *
   * @return the command line option definitions
   */
  public static Options buildOptions() {
    Options options = new Options();

    // General
    Option opt
        = new Option("c", OPT_CONFIG_DIR, true, "Sets the directory that contains "
            + "configuration files. Defaults to <basedir>/../config.");
    opt.setArgs(1);
    opt.setArgName("directory");
    options.addOption(opt);

    opt = new Option("h", OPT_HELP, false, "Displays this help.");
    options.addOption(opt);

    opt = new Option("v", OPT_VERBOSE, false, "Enabled verbose output.");
    options.addOption(opt);

    opt
        = new Option("q", OPT_QUIET, false, "Do not attempt to confirm when "
            + "performing an action on all services.");
    options.addOption(opt);

    opt
        = new Option("p", OPT_PARALLEL, false, "Enables parallel execution across "
            + "services with the specified thread count. (default: available CPUs)");
    opt.setArgs(1);
    opt.setOptionalArg(true);
    opt.setArgName("thread count");
    options.addOption(opt);

    // Command group
    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);

    opt
        = new Option("t", OPT_START, false,
            "Starts named services or all services in the selected group.");
    optionGroup.addOption(opt);

    opt
        = new Option("o", OPT_STOP, false,
            "Stops named services or all services in the selected group.");
    optionGroup.addOption(opt);

    opt
        = new Option("a", OPT_STATUS, false,
            "Displays the status of the given service "
            + "or all services in the selected group.");
    optionGroup.addOption(opt);

    opt
        = new Option("r", OPT_RESTART, false,
            "Restarts named services or all services in the selected group.");
    optionGroup.addOption(opt);

    opt = new Option("b", OPT_BOUNCE, false, "An alias for restart.");
    optionGroup.addOption(opt);

    opt
        = new Option("l", OPT_LIST_GROUPS, false,
            "Lists all the defined groups based on the configured services.");
    optionGroup.addOption(opt);

    options.addOptionGroup(optionGroup);

    // Target group
    optionGroup = new OptionGroup();

    opt
        = new Option("s", OPT_SERVICES, true,
            "The names of services to apply the action to.");
    opt.setArgName("service names");
    opt.setArgs(25);
    optionGroup.addOption(opt);

    opt
        = new Option("m", OPT_NOT_SERVICES, true,
            "The names of services to not apply the action to. This option "
            + "selects services that are not in the given list.");
    opt.setArgName("service names");
    opt.setArgs(25);
    optionGroup.addOption(opt);

    opt
        = new Option("g", OPT_GROUP, true,
            "The name of the group to apply the action to. This option "
            + "selects services that are in the given group.");
    opt.setArgs(1);
    opt.setArgName("group name");
    optionGroup.addOption(opt);

    opt
        = new Option("n", OPT_NOT_GROUP, true,
            "The name of the group to not apply the action to. This option "
            + "selects services that are not in the given group.");
    opt.setArgs(1);
    opt.setArgName("group name");
    optionGroup.addOption(opt);

    options.addOptionGroup(optionGroup);

    return options;
  }

}
