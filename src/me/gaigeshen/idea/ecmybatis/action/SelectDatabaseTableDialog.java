package me.gaigeshen.idea.ecmybatis.action;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import me.gaigeshen.idea.ecmybatis.util.DatabaseUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.sql.SQLException;
import java.util.*;

/**
 * @author gaigeshen
 * @since 02/20 2019
 */
public class SelectDatabaseTableDialog extends DialogWrapper {

  private final String url;
  private final String user;
  private final String password;

  private Tree tree;
  private Map<String, List<String>> selected;

  protected SelectDatabaseTableDialog(String url, String user, String password) {
    super(true);
    this.url = url;
    this.user = user;
    this.password = password;
    init();
    setTitle("Select Database Table...");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    String databaseName = DatabaseUtils.databaseName(url);
    tree = new Tree(createTreeNodes(new DefaultMutableTreeNode(databaseName)));
    tree.addTreeSelectionListener(e -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
      // 只有选中了数据库表节点才处理
      if (node != null && !node.isLeaf()) {
        List<String> fields = new ArrayList<>();
        selected = new HashMap<>();
        selected.put((String) node.getUserObject(), fields);
        // 填充该数据库表的所有字段
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
          fields.add((String) ((DefaultMutableTreeNode)children.nextElement()).getUserObject());
        }
      } else {
        selected = null;
      }
    });
    return new JBScrollPane(tree);
  }

  /**
   * 创建树节点
   *
   * @param node 根节点
   * @return 根节点
   */
  private DefaultMutableTreeNode createTreeNodes(DefaultMutableTreeNode node) {
    Map<String, List<String>> tableFields = null;
    try {
      tableFields = DatabaseUtils.tableFields(url, user, password);
    } catch (SQLException e) {
      Messages.showWarningDialog("Could not get table fields\n" + e.getMessage(), "Warning");
      return node;
    }
    tableFields.forEach((n, fs) -> {
      DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(n);
      fs.forEach(f -> {
        tableNode.add(new DefaultMutableTreeNode(f));
      });
      node.add(tableNode);
    });
    return node;
  }

  /**
   * 返回已经选择的数据库表列数据
   *
   * @return 已经选择的数据库表列数据
   */
  public Map<String, List<String>> selectedTableFields() {
    if (selected == null) {
      return Collections.emptyMap();
    }
    return selected;
  }

}
