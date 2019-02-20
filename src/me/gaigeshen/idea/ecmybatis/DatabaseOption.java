package me.gaigeshen.idea.ecmybatis;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author gaigeshen
 * @since 02/20 2019
 */
@State(name = "ecmybatis", storages = { @Storage("configuration.xml" )})
public class DatabaseOption implements PersistentStateComponent<DatabaseOption> {

  private String url;
  private String user;
  private String password;

  @Nullable
  @Override
  public DatabaseOption getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull DatabaseOption option) {
    url = option.url;
    user = option.user;
    password = option.password;
  }

  /**
   * 获取配置对象
   *
   * @return 配置对象
   */
  public static DatabaseOption getInstance() {
    return ServiceManager.getService(DatabaseOption.class);
  }

  /**
   * 获取配置对象
   *
   * @param project 项目
   * @return 配置对象
   */
  public static DatabaseOption getInstance(Project project) {
    return ServiceManager.getService(project, DatabaseOption.class);
  }

  /**
   * 创建配置对象
   *
   * @param url 链接地址
   * @param user 用户名
   * @param password 用户密码
   * @return 配置对象
   */
  public static DatabaseOption create(String url, String user, String password) {
    DatabaseOption option = new DatabaseOption();
    option.setUrl(url);
    option.setUser(user);
    option.setPassword(password);
    return option;
  }

  /**
   * 检查是否缺少配置选项值
   *
   * @return 是否缺少配置选项值
   */
  public boolean checkIfMissingValue() {
    // 链接地址和用户名必须要有
    if (StringUtils.isBlank(url) || StringUtils.isBlank(user)) {
      return false;
    }
    return true;
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
