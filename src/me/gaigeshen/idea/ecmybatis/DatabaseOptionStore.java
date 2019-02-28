package me.gaigeshen.idea.ecmybatis;

import com.intellij.ide.util.PropertiesComponent;

/**
 * @author gaigeshen
 * @since 02/23 2019
 */
public class DatabaseOptionStore {

  public static final String OPTION_URL = "ecmybatis.db.url";
  public static final String OPTION_USER = "ecmybatis.db.user";
  public static final String OPTION_PASSWORD = "ecmybatis.db.password";

  /**
   *
   * @return
   */
  public static DatabaseOption get() {
    PropertiesComponent properties = PropertiesComponent.getInstance();
    return new DatabaseOption(
            properties.getValue(OPTION_URL, ""),
            properties.getValue(OPTION_USER, ""),
            properties.getValue(OPTION_PASSWORD, ""));
  }

  /**
   *
   * @param option
   */
  public static void set(DatabaseOption option) {
    PropertiesComponent properties = PropertiesComponent.getInstance();
    properties.setValue(OPTION_URL, option.getUrl());
    properties.setValue(OPTION_USER, option.getUser());
    properties.setValue(OPTION_PASSWORD, option.getPassword());
  }
}
