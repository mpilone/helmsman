package org.mpilone.helmsman;

import java.util.Map;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.mpilone.helmsman.UserIo.Level;

import static java.lang.String.format;

/**
 * A task that executes a service based on the service configuration.
 *
 * @author mpilone
 */
public interface ServiceTask {

  /**
   * Executes the task which runs asynchronously. This method returns
   * immediately even if the task is still executing.
   */
  void execute();

  /**
   * Waits for the task to complete if it has not yet completed.
   *
   * @param timeout the number of milliseconds to wait
   */
  void waitFor(int timeout);

  /**
   * Returns true if the task is complete.
   *
   * @return true if the task is complete, false otherwise
   */
  boolean isComplete();

  /**
   * Returns true if the task is complete and successful, false otherwise.
   *
   * @return true if successful, false otherwise
   */
  boolean isSuccess();

  /**
   * A task that executes a delegate task and negates the success state.
   *
   * @author mpilone
   */
  public static class Not implements ServiceTask {

    /**
     * The delegate task to execute.
     */
    private ServiceTask task;

    /**
     * Constructs the task.
     *
     * @param task the delegate task to execute
     */
    public Not(ServiceTask task) {
      super();
      this.task = task;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#execute()
     */
    @Override
    public void execute() {
      task.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#waitFor(int)
     */
    @Override
    public void waitFor(int timeout) {
      task.waitFor(timeout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isComplete()
     */
    @Override
    public boolean isComplete() {
      return task.isComplete();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isSuccess()
     */
    @Override
    public boolean isSuccess() {
      return !task.isSuccess();
    }
  }

  /**
   * A task that executes the left operand task and if it fails, executes the
   * right operand task. Therefore, the right task is only executed if the left
   * task fails. The result is true if the left task is successful or the result
   * of the right task.
   *
   * @author mpilone
   */
  public static class Or implements ServiceTask {

    private ServiceTask leftTask;
    private ServiceTask rightTask;
    private ServiceTask task;

    /**
     * Constructs the task.
     *
     * @param leftTask the left operand task
     * @param rightTask the right operand task
     */
    public Or(ServiceTask leftTask, ServiceTask rightTask) {
      super();
      this.leftTask = leftTask;
      this.rightTask = rightTask;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#execute()
     */
    @Override
    public void execute() {
      task = leftTask;
      task.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#waitFor(int)
     */
    @Override
    public void waitFor(int timeout) {
      task.waitFor(timeout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isComplete()
     */
    @Override
    public boolean isComplete() {

      if (task.isComplete()) {
        if (!task.isSuccess()) {
          task = rightTask;
          task.execute();
        }
      }

      return task.isComplete();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isSuccess()
     */
    @Override
    public boolean isSuccess() {
      return task.isSuccess();
    }
  }

  /**
   * A task that executes the left operand task and if it succeeds, executes the
   * right operand task. Therefore, the right task is only executed if the left
   * task succeeds. The result is false if the left task fails or the result of
   * the right task.
   *
   * @author mpilone
   */
  public static class And implements ServiceTask {

    private ServiceTask leftTask;
    private ServiceTask rightTask;
    private ServiceTask task;

    /**
     * Constructs the task.
     *
     * @param leftTask the left operand task
     * @param rightTask the right operand task
     */
    public And(ServiceTask leftTask, ServiceTask rightTask) {
      super();
      this.leftTask = leftTask;
      this.rightTask = rightTask;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#execute()
     */
    @Override
    public void execute() {
      task = leftTask;
      task.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#waitFor(int)
     */
    @Override
    public void waitFor(int timeout) {
      task.waitFor(timeout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isComplete()
     */
    @Override
    public boolean isComplete() {

      if (task.isComplete()) {
        if (task.isSuccess()) {
          task = rightTask;
          task.execute();
        }
      }

      return task.isComplete();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isSuccess()
     */
    @Override
    public boolean isSuccess() {
      return task.isSuccess();
    }
  }

  /**
   * A task which executes the {@link ServiceConfig#getScript()} with a command
   * argument (e.g. status, start, or stop). The result of the task is the
   * success state of the executed script process.
   *
   * @author mpilone
   */
  public static class Command implements ServiceTask {

    /**
     * The User IO to write all user output.
     */
    private UserIo userIo;

    /**
     * The output stream to use for all service script execution.
     */
    private ServiceOutputStream SERVICE_OUT;

    /**
     * The executor running the script process.
     */
    private Executor executor;

    /**
     * The result handler monitoring the script process.
     */
    private DefaultExecuteResultHandler resultHandler;

    /**
     * The command argument to pass to the script.
     */
    private String command;

    /**
     * The service configuration to execute.
     */
    private ServiceConfig service;

    /**
     * The flag which indicates success which will be null until the service
     * script completes (or raises an exception).
     */
    private Boolean success;

    /**
     * Constructs the task which will run the service's script with the given
     * command argument.
     *
     * @param service the service configuration to execute
     * @param command the command argument
     * @param userIo used for debugging output
     */
    public Command(ServiceConfig service, String command, UserIo userIo) {
      this.service = service;
      this.command = command;
      this.userIo = userIo;

      SERVICE_OUT = new ServiceOutputStream(userIo);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#execute()
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void execute() {
      userIo.println(
          format("Executing script [%s] with command [%s].", service.getScript(),
              command), UserIo.Level.DEBUG);

      try {
        // Create the command to run.
        org.apache.commons.exec.CommandLine cmdLine
            = new org.apache.commons.exec.CommandLine(service.getScript());
        cmdLine.addArgument(command);

        // Merge the current environment with the service specific environment.
        Map env = EnvironmentUtils.getProcEnvironment();
        env.putAll(service.getEnvironment());

        // Remove generated environment variables. This seems like a hack, but
        // the JVM appears to generate values for these variables even if they
        // are not set in the processes original environment. The variables
        // cause problems if they are passed to sub-processes, especially if the
        // sub-process is in a different version of the JVM. Refer to
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4953367.
        env.remove("NLSPATH");
        env.remove("XFILESEARCHPATH");

        resultHandler = new DefaultExecuteResultHandler();

        ExecuteWatchdog watchdog
            = new ExecuteWatchdog(service.getTimeout() * 1000);

        // Create and configure the executor.
        executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(SERVICE_OUT,
            SERVICE_OUT));
        executor.setExitValue(0);
        executor.setWatchdog(watchdog);

        // Execute the script.
        executor.execute(cmdLine, env, resultHandler);
      }
      catch (Exception ex) {
        userIo.println(
            "Exception while executing service script: " + ex.getMessage(),
            UserIo.Level.DEBUG);
        userIo.print(ex, Level.DEBUG);

        success = Boolean.FALSE;
      }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#waitFor(int)
     */
    @Override
    public void waitFor(int timeout) {
      try {
        // Wait for the script to finish or timeout.
        if (!resultHandler.hasResult()) {
          resultHandler.waitFor(timeout);
        }
      }
      catch (Exception ex) {
        userIo.println(
            "Exception while executing service script: " + ex.getMessage(),
            UserIo.Level.DEBUG);
        userIo.print(ex, Level.DEBUG);

        success = Boolean.FALSE;
      }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isComplete()
     */
    @Override
    public boolean isComplete() {

      if (success == null) {
        try {
          if (resultHandler.hasResult()) {
            success = !executor.isFailure(resultHandler.getExitValue());

            // Log the exception if there was one.
            if (!success && resultHandler.getException() != null) {
              userIo.print(resultHandler.getException(), Level.DEBUG);
            }
          }
        }
        catch (Exception ex) {
          userIo.println(
              "Exception while executing service script: " + ex.getMessage(),
              UserIo.Level.DEBUG);
          userIo.print(ex, Level.DEBUG);

          success = Boolean.FALSE;
        }
      }

      return success != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mpilone.helsman.ServiceTask#isSuccess()
     */
    @Override
    public boolean isSuccess() {
      return (success != null) ? success : false;
    }
  }
}
