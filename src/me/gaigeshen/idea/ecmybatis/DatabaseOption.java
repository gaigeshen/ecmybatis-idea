package me.gaigeshen.idea.ecmybatis;

import org.apache.commons.lang3.StringUtils;

/**
 * @author gaigeshen
 * @since 02/23 2019
 */
public class DatabaseOption {
  private final String url;
  private final String user;
  private final String password;

  public DatabaseOption(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
  }

  public boolean isValid() {
    return StringUtils.isNotBlank(getUrl()) && StringUtils.isNotBlank(getUser());
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
