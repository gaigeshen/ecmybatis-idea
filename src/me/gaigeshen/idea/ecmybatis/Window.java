package me.gaigeshen.idea.ecmybatis;

import com.google.common.collect.Sets;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.scopes.ModulesScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import me.gaigeshen.idea.ecmybatis.util.DatabaseUtils;
import me.gaigeshen.idea.ecmybatis.util.NameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

/**
 * @author gaigeshen
 * @since 02/22 2019
 */
public class Window implements ToolWindowFactory {

  private static String MODEL_CONTENT;
  private static final String MODEL_BASE = "BaseModel.java";
  private static final String MODEL_BASE_SIMPLE = "BaseModel";
  private static final String MODEL_CONTENT_FILE = "/Model1.java.txt";

  private static String DAO_CONTENT;
  private static final String DAO_BASE = "BaseDao.java";
  private static final String DAO_BASE_SIMPLE = "BaseDao";
  private static final String DAO_CONTENT_FILE = "/Dao1.java.txt";

  private static String MAPPER_CONTENT;
  private static final String MAPPER_CONTENT_FILE = "/Dao.xml.txt";

  private TreeTable treeTable;

  static {
    initializeModelContent();
    initializeDaoContent();
    initializeMapperContent();
  }

  /**
   * 初始化实体类的模板内容
   */
  private static void initializeModelContent() {
    if (MODEL_CONTENT == null) {
      try (InputStream in = Window.class.getResourceAsStream(MODEL_CONTENT_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          MODEL_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize model content", e);
      }
    }
  }

  /**
   * 初始化数据访问类的模板内容
   */
  private static void initializeDaoContent() {
    if (DAO_CONTENT == null) {
      try (InputStream in = Window.class.getResourceAsStream(DAO_CONTENT_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          DAO_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize dao content", e);
      }
    }
  }

  /**
   * 初始化映射文件的模板内容
   */
  private static void initializeMapperContent() {
    if (MAPPER_CONTENT == null) {
      try (InputStream in = Window.class.getResourceAsStream(MAPPER_CONTENT_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          MAPPER_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize mapper content", e);
      }
    }
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow wnd) {
    ContentManager contentManager = wnd.getContentManager();
    contentManager.addContent(createContent(contentManager, project));
  }

  private Content createContent(ContentManager manager, Project project) {
    TreeTableModel model = new ListTreeTableModelOnColumns(null,
            new ColumnInfo[]{new TreeColumnInfo("Table & Column"), new ColumnInfo<TableOrColumnNode, String>("Description") {
              @Nullable
              @Override
              public String valueOf(TableOrColumnNode node) {
                Object data = node.getData();
                if (data instanceof Table) {
                  return ((Table) data).getFieldsDescription();
                }
                if (data instanceof Column) {
                  return ((Column) data).getDescription();
                }
                return "";
              }
            }});

    treeTable = new TreeTable(model);
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setPreferredSize(new Dimension(0, 0));
    treeTable.getTableHeader().setVisible(false);
    treeTable.getTableHeader().setDefaultRenderer(renderer);
    JBScrollPane pane = new JBScrollPane(treeTable);

    DefaultActionGroup actionGroup = new DefaultActionGroup(new ConfigureDatabaseOptionAction(), new LoadAction(), new GenerateBaseClassAction(), new GenerateAction());
    ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("WindowToolbar", actionGroup, true);
    toolbar.setTargetComponent(treeTable);

    SimpleToolWindowPanel windowPanel = new SimpleToolWindowPanel(true, true);
    windowPanel.setContent(pane);
    windowPanel.setToolbar(toolbar.getComponent());

    Content content = manager.getFactory().createContent(windowPanel, null, false);
    return content;
  }

  private TableOrColumnNode createTableOrColumnNodes(DatabaseOption option) {
    List<String> tableNames = null;
    try {
      tableNames = DatabaseUtils.tableNames(option.getUrl(), option.getUser(), option.getPassword());
    } catch (SQLException e) {
      e.printStackTrace();
    }
    List<TableOrColumnNode> tables = new ArrayList<>(tableNames.size());
    TableOrColumnNode root = new TableOrColumnNode(null, null, tables);

    for (String tableName : tableNames) {
      Map<String, String[]> columnTypes = null;
      try {
        columnTypes = DatabaseUtils.columnTypes(option.getUrl(), option.getUser(), option.getPassword(), tableName);
      } catch (SQLException e) {
        e.printStackTrace();
      }

      List<TableOrColumnNode> columns = new ArrayList<>(columnTypes.size());

      TableOrColumnNode table = new TableOrColumnNode(new Table(tableName, null), root, columns);
      tables.add(table);

      for (Map.Entry<String, String[]> entry : columnTypes.entrySet()) {
        String[] commentAndType = entry.getValue();
        columns.add(new TableOrColumnNode(new Column(tableName, entry.getKey(), commentAndType[0], commentAndType[1]), table, Collections.emptyList()));
      }
    }
    return root;
  }

  private class ConfigureDatabaseOptionAction extends AnAction {
    ConfigureDatabaseOptionAction() {
      super("Configure", "Configure database option", AllIcons.Actions.Edit);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      DatabaseOption option = DatabaseOptionStore.get();
      ConfigureDatabaseOptionDialog dialog = ConfigureDatabaseOptionDialog.createWithValue(option);
      if (dialog.showAndGet()) {
        if (!dialog.isOptionsValid()) {
          Messages.showWarningDialog("Url or user canot be blank", "Warning");
          return;
        }
        DatabaseOptionStore.set(new DatabaseOption(dialog.getUrl(), dialog.getUser(), dialog.getPassword()));
      }
    }
  }

  private class LoadAction extends AnAction {

    LoadAction() {
      super("Load", "Load or reload tables", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
      treeTable.clearSelection();
      DatabaseOption option = DatabaseOptionStore.get();
      if (!option.isValid()) {
        Messages.showWarningDialog("Please configure database option", "Warning");
        return;
      }
      treeTable.setModel(new ListTreeTableModelOnColumns(createTableOrColumnNodes(option),
              new ColumnInfo[]{
                      new TreeColumnInfo("Table & Column"),
                      new ColumnInfo<TableOrColumnNode, String>("Description") {
                        @Nullable
                        @Override
                        public String valueOf(TableOrColumnNode node) {
                          Object data = node.getData();
                          if (data instanceof Table) {
                            return ((Table) data).getFieldsDescription();
                          }
                          if (data instanceof Column) {
                            return ((Column) data).getDescription();
                          }
                          return "";
                        }
                      }}));
      treeTable.setRootVisible(false);
    }
  }

  private class GenerateBaseClassAction extends AnAction {
    public GenerateBaseClassAction() {
      super("Initialize", "Generate base classes like BaseDao, BaseModel etc...", AllIcons.Actions.GroupByPackage);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      ConfigureBasePackageDialog dialog = new ConfigureBasePackageDialog();
      if (dialog.showAndGet()) {
        Module module = dialog.getModule();
        PsiPackage basePackage = dialog.getBasePackage();
        PsiPackage domainPackage = dialog.getDomainPackage();
        PsiPackage daoPackage = dialog.getDaoPackage();
        if (basePackage == null) {
          Messages.showWarningDialog("No selected base package", "Warning");
          return;
        }
        if (domainPackage == null) {
          Messages.showWarningDialog("No selected domain package", "Warning");
          return;
        }if (daoPackage == null) {
          Messages.showWarningDialog("No selected dao package", "Warning");
          return;
        }
        Initializer.create().generate(module, basePackage, domainPackage, daoPackage);
      }
    }
  }

  private class GenerateAction extends AnAction {
    GenerateAction() {
      super("Generate", "Show dialog for generate files", AllIcons.Actions.GroupByFile);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
      int selectedRow = treeTable.getSelectedRow();
      if (selectedRow > -1) {
        TableOrColumnNode node = (TableOrColumnNode) treeTable.getModel().getValueAt(selectedRow, 0);
        Object data = node.getData();
        if (data instanceof Table) {
          List<Column> columns = new ArrayList<>();
          Enumeration children = node.children();
          while (children.hasMoreElements()) {
            Column child = (Column) ((TableOrColumnNode) children.nextElement()).getData();
            columns.add(child);
          }
          ConfigurePackageDialog dialog = new ConfigurePackageDialog();
          if (dialog.showAndGet()) {
            Module module = dialog.getModule();
            PsiPackage domainPackage = dialog.getDomainPackage();
            PsiPackage daoPackage = dialog.getDaoPackage();
            VirtualFile mapperDirectory = dialog.getMapperDirectory();
            String tableNameCamel = NameUtils.underlineToCamel(((Table) data).getName());
            String typeName = StringUtils.upperCase(StringUtils.left(tableNameCamel, 1)) + tableNameCamel.substring(1);
            generateFiles(module, domainPackage, daoPackage, mapperDirectory, typeName, columns);
          }
          return;
        }
      }
      Messages.showWarningDialog("No selected database table", "Warning");
    }
  }

  /**
   *
   * @param module
   * @param domainPackage
   * @param daoPackage
   * @param mapperDirectory
   * @param typeName
   * @param columns
   */
  private void generateFiles(Module module, PsiPackage domainPackage, PsiPackage daoPackage, VirtualFile mapperDirectory, String typeName, List<Column> columns) {
    if (!generateDomainClass(module, domainPackage, typeName, columns)) {
      return;
    }
    if (!generateDaoClass(module, daoPackage, typeName)) {
      return;
    }
    if (!generateMapper(module, daoPackage, mapperDirectory, typeName, columns)) {
      return;
    }
  }

  /**
   *
   * @param module
   * @param domainPackage
   * @param typeName
   * @param columns
   * @return
   */
  private boolean generateDomainClass(Module module, PsiPackage domainPackage, String typeName, List<Column> columns) {
    if (domainPackage == null || !domainPackage.isValid()) {
      Messages.showWarningDialog("Domain package invalid", "Warning");
      return false;
    }
    if (isFileExists(module, typeName + ".java")) {
      return true;
    }
    PsiFile[] psiFiles = FilenameIndex.getFilesByName(module.getProject(), MODEL_BASE, new ModulesScope(Sets.newHashSet(module), module.getProject()));
    if (psiFiles.length == 0) {
      Messages.showWarningDialog("Could not found BaseModel.java", "Warning");
      return false;
    }
    PsiJavaFile baseModel = (PsiJavaFile) psiFiles[0];
    String baseModelPackageName = baseModel.getPackageName();
    String content = MODEL_CONTENT
            .replaceAll("_package_", domainPackage.getQualifiedName())
            .replaceAll("_baseModel_", baseModelPackageName + "." + MODEL_BASE_SIMPLE)
            .replaceAll("_typeName_", typeName);
    StringBuilder fields = new StringBuilder();
    columns.forEach(col -> {
      if (!col.getColumnName().equals("id")) {
        if (fields.length() != 0) {
          fields.append("\n  ");
        }
        fields.append("private ")
              .append(col.getJavaType()).append(" ")
              .append(col.getPropertyName())
              .append(";")
              .append(" //")
              .append(col.getDescription());
      }
    });
    content = content.replaceAll("_modelFields_", fields.toString());
    PsiFile model = PsiFileFactory.getInstance(module.getProject()).createFileFromText(typeName + ".java", StdFileTypes.JAVA, content);
    PsiDirectory domainPackageDirectory = PackageUtil.findPossiblePackageDirectoryInModule(module, domainPackage.getQualifiedName());
    if (domainPackageDirectory == null) {
      Messages.showWarningDialog("The domain package is missing or invalid", "Warning");
      return false;
    }
    WriteCommandAction.runWriteCommandAction(module.getProject(), () -> {
      domainPackageDirectory.add(model);
    });
    return true;
  }

  /**
   *
   * @param module
   * @param daoPackage
   * @param typeName
   * @return
   */
  private boolean generateDaoClass(Module module, PsiPackage daoPackage, String typeName) {
    if (daoPackage == null || !daoPackage.isValid()) {
      Messages.showWarningDialog("Dao package invalid", "Warning");
      return false;
    }
    if (isFileExists(module, typeName + "Dao.java")) {
      return true;
    }
    PsiFile[] psiFiles = FilenameIndex.getFilesByName(module.getProject(), DAO_BASE, new ModulesScope(Sets.newHashSet(module), module.getProject()));
    if (psiFiles.length == 0) {
      Messages.showWarningDialog("Could not found " + DAO_BASE, "Warning");
      return false;
    }
    PsiJavaFile baseDao = (PsiJavaFile) psiFiles[0];
    psiFiles = FilenameIndex.getFilesByName(module.getProject(), typeName + ".java", new ModulesScope(Sets.newHashSet(module), module.getProject()));
    if (psiFiles.length == 0) {
      Messages.showWarningDialog("Could not found " + typeName + ".java", "Warning");
      return false;
    }
    PsiJavaFile type = (PsiJavaFile) psiFiles[0];
    String content = DAO_CONTENT
            .replaceAll("_package_", daoPackage.getQualifiedName())
            .replaceAll("_typeName_", typeName)
            .replaceAll("_type_", type.getPackageName() + "." + typeName)
            .replaceAll("_baseDao_", baseDao.getPackageName() + "." + DAO_BASE_SIMPLE);
    PsiFile dao = PsiFileFactory.getInstance(module.getProject()).createFileFromText(typeName + "Dao.java", StdFileTypes.JAVA, content);
    PsiDirectory daoPackageDirectory = PackageUtil.findPossiblePackageDirectoryInModule(module, daoPackage.getQualifiedName());
    if (daoPackageDirectory == null) {
      Messages.showWarningDialog("The dao package is missing or invalid", "Warning");
      return false;
    }
    WriteCommandAction.runWriteCommandAction(module.getProject(), () -> {
      daoPackageDirectory.add(dao);
    });
    return true;
  }

  /**
   *
   * @param module
   * @param daoPackage
   * @param mapperDirectory
   * @param typeName
   * @param columns
   */
  private boolean generateMapper(Module module, PsiPackage daoPackage, VirtualFile mapperDirectory, String typeName, List<Column> columns) {
    if (!mapperDirectory.exists()) {
      Messages.showWarningDialog("The mapper directory is missing or invalid", "Warning");
      return false;
    }
    if (isFileExists(module, typeName + "Dao.xml")) {
      return true;
    }

    StringBuilder fields = new StringBuilder("<id property=\"id\" column=\"id\"></id>");
    columns.forEach(col -> {
      if (!col.getColumnName().equals("id")) {
        fields.append("\n    ")
              .append("<result property=\"")
              .append(col.getPropertyName())
              .append("\" column=\"")
              .append(col.getColumnName())
              .append("\"/>");
      }
    });
    String content = MAPPER_CONTENT
            .replaceAll("_namespace_", daoPackage.getQualifiedName() + "." + typeName + "Dao")
            .replaceAll("_table_", NameUtils.camelToUnderline(typeName).substring(1))
            .replaceAll("_fields_", fieldsFromColumns(columns))
            .replaceAll("_type_", typeName)
            .replaceAll("_properties_", fields.toString());

    PsiFile daoXmlFile = PsiFileFactory.getInstance(daoPackage.getProject()).createFileFromText(typeName + "Dao.xml", StdFileTypes.XML, content);

    return WriteAction.compute(() -> {
      boolean success = true;
      try {
        VirtualFile file = mapperDirectory.createChildData(daoXmlFile, typeName + "Dao.xml");
        file.setBinaryContent(content.getBytes(Charset.forName("utf-8")));
      } catch (IOException e) {
        success = false;
      }
      return success;
    });
  }

  /**
   * 生成逗号分隔的所有数据库字段
   *
   * @param columns 数据库列
   * @return 逗号分隔的字符串
   */
  private String fieldsFromColumns(List<Column> columns) {
    StringBuilder fields = new StringBuilder();
    for (Column col : columns) {
      fields.append(", ").append(col.getColumnName());
    }
    return fields.substring(2);
  }

  /**
   * 检查文件是否存在
   *
   * @param module 模块
   * @param fileName 文件名称
   * @return 返回该文件是否存在
   */
  private boolean isFileExists(Module module, String fileName) {
    PsiFile[] psiFiles = FilenameIndex.getFilesByName(module.getProject(), fileName,
            new ModulesScope(Sets.newHashSet(module), module.getProject()));
    return psiFiles.length > 0;
  }

}
