package logging;

import java.util.Arrays;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class LiferayLogger implements Logger {

  private Log liferayLog;
  public LiferayLogger(Class<?> c) {
    liferayLog = LogFactoryUtil.getLog(c);
  }

  @Override
  public void debug(String message) {
    liferayLog.debug(message);
  }

  @Override
  public void info(String message) {
    liferayLog.info(message);

  }

  @Override
  public void warn(String message) {
    liferayLog.warn(message);

  }

  @Override
  public void error(String message) {
    liferayLog.error(message);

  }

  @Override
  public void error(String message, Throwable t) {
    // do not fill logfile with millions of lines per error, please
      t.setStackTrace(Arrays.copyOfRange(t.getStackTrace(), 0, 10));
      liferayLog.error(message, t);
  }
}
