# Helmsman

Helmsman is a simple process control application that can be used to control a set of services. Helmsman strives to be extremely simple, reliable, and easy to deploy. There are many process control applications out there such as Monit, Upstartd, Puppet, Chef, RunDeck, etc., etc. but for small installations these tools tend to be overkill. Helmsman is designed with a simple configuration format and no coding or DSL required.

## Features

* Simple Java properties configuration format
* One jar deployment
* Base configuration shared across all environments/machines
* Per machine configuration overrides
* Simple service start/stop ordering (no dependency model)
* Parallel or serial service execution

## Concepts

Helsmans is a Java application that takes a bunch of command line arguments to control a configured set of services.

### Commands

The basic commands for all services are start, stop, and status. The service (or script) must return a 0 on success, 1 on failure. This is similar to the classic System V init script style. A couple sample scripts are included in the support directory but in most cases the scripts will come from packages like Java Service Wrapper (JSW) or Yet Another Java Service Wrapper (YAJSW).

### Groups

Services can be put into groups to support easily starting or stopping a specific group of services (or all services not in a group). For example, you may have a "critical" group which contains all the services that must remain running even during a deployment.

### Order

All services are assigned an "order" value which indicates the startup and shutdown order. Services are started from lowest value to highest value and stopped in the reverse order. Services with the same order value are executed in parallel when parallel execution is enabled.

### Configuration

A base.properties configuration file which lists the services and a default configuration for each service. An optional configuration file matching the hostname on a machine will be loaded to override the base configuration.

## Usage

Helmsman ships as an executable jar but normally it is executed via a simple shell script. A sample shell script is included in the support directory.

    usage: helmsman -a | -b | -l | -o | -r | -t  [-c <directory>] [-g <group
           name> | -m <service names> | -n <group name> | -s <service names>]
           [-h]     [-p <thread count>] [-q]    [-v]
     -a,--status                         Displays the status of the given
                                         service or all services in the
                                         selected group.
     -b,--bounce                         An alias for restart.
     -c,--configDir <directory>          Sets the directory that contains
                                         configuration files. Defaults to
                                         <basedir>/../config.
     -g,--group <group name>             The name of the group to apply the
                                         action to. This option selects
                                         services that are in the given group.
     -h,--help                           Displays this help.
     -l,--list-groups                    Lists all the defined groups based on
                                         the configured services.
     -m,--not-services <service names>   The names of services to not apply
                                         the action to. This option selects
                                         services that are not in the given
                                         list.
     -n,--not-group <group name>         The name of the group to not apply
                                         the action to. This option selects
                                         services that are not in the given
                                         group.
     -o,--stop                           Stops named services or all services
                                         in the selected group.
     -p,--parallel <thread count>        Enables parallel execution across
                                         services with the specified thread
                                         count. (default: available CPUs)
     -q,--quiet                          Do not attempt to confirm when
                                         performing an action on all services.
     -r,--restart                        Restarts named services or all
                                         services in the selected group.
     -s,--services <service names>       The names of services to apply the
                                         action to.
     -t,--start                          Starts named services or all services
                                         in the selected group.
     -v,--verbose                        Enabled verbose output.

## Getting Builds

The source, javadoc, and binaries are available in the 
[mpilone/mvn-repo](https://github.com/mpilone/mvn-repo) GitHub repository. You
can configure Maven or Ivy to directly grab the dependencies by adding the repository:

    <repositories>
         <repository>
             <id>mpilone-snapshots</id>
             <url>https://github.com/mpilone/mvn-repo/raw/master/snapshots</url>
         </repository>
         <repository>
             <id>mpilone-releases</id>
             <url>https://github.com/mpilone/mvn-repo/raw/master/releases</url>
         </repository>
     </repositories>

And then adding the dependency:

    <dependency>
        <groupId>org.mpilone</groupId>
        <artifactId>helmsman</artifactId>
        <version>1.0.0</version>
    </dependency>

You can also just grab the full, executeable helmsman-1.0.0-jar-with-dependencies.jar archive and run it immediately as a standalone application from [mpilone/mvn-repo](https://github.com/mpilone/mvn-repo/tree/master/releases/org/mpilone/helmsman/) with a command like:

    java -jar helmsman-1.0.0-jar-with-dependencies.jar -h
