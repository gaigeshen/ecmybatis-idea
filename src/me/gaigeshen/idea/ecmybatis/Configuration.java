package me.gaigeshen.idea.ecmybatis;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author gaigeshen
 * @since 02/20 2019
 */
@State(name = "ecmybatis", storages = { @Storage("configuration.xml" )})
public class Configuration implements PersistentStateComponent<Configuration> {

  private String url;
  private String user;
  private String password;

  @Nullable
  @Override
  public Configuration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull Configuration configuration) {
    url = configuration.url;
    user = configuration.user;
    password = configuration.password;
  }

  /**
   * 获取配置对象
   *
   * @param project 项目
   * @return 配置对象
   */
  public static Configuration getInstance(Project project) {
    return ServiceManager.getService(project, Configuration.class);
  }

  /**
   * 创建配置对象
   *
   * @param url 链接地址
   * @param user 用户名
   * @param password 用户密码
   * @return 配置对象
   */
  public static Configuration create(String url, String user, String password) {
    Configuration configuration = new Configuration();
    configuration.setUrl(url);
    configuration.setUser(user);
    configuration.setPassword(password);
    return configuration;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
