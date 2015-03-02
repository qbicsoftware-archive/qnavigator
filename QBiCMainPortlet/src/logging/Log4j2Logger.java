package logging;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;

public class Log4j2Logger implements Logger {

  org.apache.logging.log4j.Logger logger;

  public Log4j2Logger(Class<?> c) {
    logger = LogManager.getLogger(c);
  }

  @Override
  public void debug(String message) {
    logger.debug(message);

  }

  @Override
  public void info(String message) {
    logger.info(message);

  }

  @Override
  public void warn(String message) {
    logger.warn(message);

  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void error(String message, Throwable t) {
    t.setStackTrace(Arrays.copyOfRange(t.getStackTrace(), 0, 10));
    logger.error(message, t);

  }
}
