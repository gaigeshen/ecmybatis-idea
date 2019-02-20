package me.gaigeshen.idea.ecmybatis.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import me.gaigeshen.idea.ecmybatis.DatabaseOption;

import java.util.List;
import java.util.Map;


/**
 * 点击菜单弹出生成文件对话框
 *
 * @author gaigeshen
 * @since 02/20 2019
 */
public class GenerateDialogAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    DatabaseOption option = DatabaseOption.getInstance();
    if (!option.checkIfMissingValue()) {
      Messages.showWarningDialog("Missing database options, please check", "Warning");
      return;
    }
    String url = option.getUrl();
    String user = option.getUser();
    String password = option.getPassword();
    SelectDatabaseTableDialog dialog = new SelectDatabaseTableDialog(url, user, password);
    if (dialog.showAndGet()) {
      Map<String, List<String>> selectedTableFields = dialog.selectedTableFields();
      System.out.println(selectedTableFields);
    }
  }
}
