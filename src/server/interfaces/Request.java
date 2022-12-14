package server.interfaces;

import java.io.Closeable;
import java.io.File;

public interface Request extends Translator, Closeable {

  public String getMethod();

  public String getPath();

  public String getParam(String key);

  public void setParam(String key, String value);

  public File getTempFile();

}
