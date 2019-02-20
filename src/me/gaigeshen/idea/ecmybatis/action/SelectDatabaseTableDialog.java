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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    setTitle("Select database table...");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    tree = new Tree(createTreeNodes(new DefaultMutableTreeNode()));
    tree.addTreeSelectionListener(e -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
      if (node != null) {
        TreeNode treeNode = (TreeNode) node.getUserObject();
        selected = new HashMap<>();
        selected.put(treeNode.getName(), treeNode.getColumns());
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
      node.add(new DefaultMutableTreeNode(new TreeNode(n, fs)));
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

  /**
   * 树节点数据
   *
   * @author gaigeshen
   * @since 02/20 2019
   */
  private class TreeNode {
    private final String name;
    private final List<String> columns;
    private TreeNode(String name, List<String> columns) {
      this.name = name;
      this.columns = columns;
    }
    public String getName() {
      return name;
    }
    public List<String> getColumns() {
      return columns;
    }
  }



}
