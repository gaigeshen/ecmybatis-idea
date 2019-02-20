package me.gaigeshen.idea.ecmybatis;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 配置面板
 *
 * @author gaigeshen
 * @since 02/20 2019
 */
public class DatabaseOptionConfigurable implements Configurable {

  private JTextField txtUrl;
  private JTextField txtUser;
  private JTextField txtPassword;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Ecmybatis";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    DatabaseOption option = DatabaseOption.getInstance();
    JPanel panel = new JPanel(new GridLayout(3, 2));

    panel.add(new JLabel("Url:"));
    panel.add(txtUrl = new JTextField(option.getUrl()));

    panel.add(new JLabel("User:"));
    panel.add(txtUser = new JTextField(option.getUser()));

    panel.add(new JLabel("Password:"));
    panel.add(txtPassword = new JTextField(option.getPassword()));

    return panel;
  }

  @Override
  public boolean isModified() {
    String url = txtUrl.getText().trim();
    String user = txtUser.getText().trim();
    String password = txtPassword.getText().trim();
    DatabaseOption option = DatabaseOption.getInstance();
    return !url.equals(option.getUrl())
            || !user.equals(option.getUser())
            || !password.equals(option.getPassword());
  }

  @Override
  public void apply() throws ConfigurationException {
    DatabaseOption option = DatabaseOption.getInstance();
    option.setUrl(txtUrl.getText().trim());
    option.setUser(txtUser.getText().trim());
    option.setPassword(txtPassword.getText().trim());
  }
}
