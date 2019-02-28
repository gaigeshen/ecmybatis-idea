package me.gaigeshen.idea.ecmybatis;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * @author gaigeshen
 * @since 02/27 2019
 */
public class ConfigureBasePackageDialog extends DialogWrapper {

  private ComboBox<Project> projectComboBox;
  private ComboBox<Module> moduleComboBox;
  private JTextField basePackageField;
  private JTextField domainPackageField;
  private JTextField daoPackageField;

  private Project project;
  private Module module;
  private PsiPackage basePackage;
  private PsiPackage domainPackage;
  private PsiPackage daoPackage;

  public ConfigureBasePackageDialog() {
    super(true);
    init();
    setTitle("Configure Base Packages");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;

    initializeProjectsField(panel, gbc);
    initializeModulesField(panel, gbc);
    initializeBasePackageField(panel, gbc);
    initializeDomainPackageField(panel, gbc);
    initializeDaoPackageField(panel, gbc);

    return panel;
  }

  private void initializeProjectsField(JComponent container, GridBagConstraints gbc) {
    JLabel label = new JLabel("Project:");
    projectComboBox = new ComboBox<>();
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    projectComboBox.setModel(new DefaultComboBoxModel<>(projects));
    Project project = (Project) projectComboBox.getSelectedItem();
    if (project != null) {
      this.project = project;
    }
    gbc.gridx = 0;
    gbc.gridy = 0;
    container.add(label, gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    container.add(projectComboBox, gbc);
    projectComboBox.addItemListener(e -> {
      this.project = (Project) e.getItem();
      moduleComboBox.removeAllItems();
      moduleComboBox.setModel(new DefaultComboBoxModel<>(ModuleManager.getInstance(this.project).getModules()));
    });
  }

  private void initializeModulesField(JPanel container, GridBagConstraints gbc) {
    JLabel label = new JLabel("Module:");
    moduleComboBox = new ComboBox<>();
    gbc.gridx = 0;
    gbc.gridy = 1;
    container.add(label, gbc);
    gbc.gridx = 1;
    gbc.gridy = 1;
    container.add(moduleComboBox, gbc);
    moduleComboBox.addItemListener(e -> {
      module = (Module) e.getItem();
      basePackageField.setText("");
      domainPackageField.setText("");
      daoPackageField.setText("");
    });
    Project selected = (Project) projectComboBox.getModel().getSelectedItem();
    if (selected != null) {
      moduleComboBox.setModel(new DefaultComboBoxModel<>(ModuleManager.getInstance(selected).getModules()));
      Module module = (Module) moduleComboBox.getSelectedItem();
      if (module != null) {
        this.module = module;
      }
    }
  }

  private void initializeBasePackageField(JPanel container, GridBagConstraints gbc) {
    JLabel label = new JLabel("Base Package:");
    basePackageField = new JTextField();
    basePackageField.setEditable(false);
    JButton basePackageButton = new JButton("Browse...");
    bindButtonClickEvent(basePackageButton, "Choose Base Package", pkg -> {
      basePackage = pkg;
      if (basePackage != null) {
        String packageName = basePackage.getQualifiedName();
        basePackageField.setText(packageName.isEmpty() ? "(default)" : packageName);
      }
    });
    gbc.gridx = 0;
    gbc.gridy = 2;
    container.add(label, gbc);
    gbc.gridx = 1;
    gbc.gridy = 2;
    container.add(basePackageField, gbc);
    gbc.gridx = 2;
    gbc.gridy = 2;
    container.add(basePackageButton, gbc);
  }

  private void initializeDomainPackageField(JPanel container, GridBagConstraints gbc) {
    JLabel label = new JLabel("Domain Package:");
    domainPackageField = new JTextField();
    domainPackageField.setEditable(false);
    JButton domainPackageButton = new JButton("Browse...");
    bindButtonClickEvent(domainPackageButton, "Choose Domain Package", pkg -> {
      domainPackage = pkg;
      if (domainPackage != null) {
        String packageName = domainPackage.getQualifiedName();
        domainPackageField.setText(packageName.isEmpty() ? "(default)" : packageName);
      }
    });
    gbc.gridx = 0;
    gbc.gridy = 3;
    container.add(label, gbc);
    gbc.gridx = 1;
    gbc.gridy = 3;
    container.add(domainPackageField, gbc);
    gbc.gridx = 2;
    gbc.gridy = 3;
    container.add(domainPackageButton, gbc);
  }

  private void initializeDaoPackageField(JPanel container, GridBagConstraints gbc) {
    JLabel label = new JLabel("Dao Package:");
    daoPackageField = new JTextField();
    daoPackageField.setEditable(false);
    JButton daoPackageButton = new JButton("Browse...");
    bindButtonClickEvent(daoPackageButton, "Choose Dao Package", pkg -> {
      daoPackage = pkg;
      if (daoPackage != null) {
        String packageName = daoPackage.getQualifiedName();
        daoPackageField.setText(packageName.isEmpty() ? "(default)" : packageName);
      }
    });
    gbc.gridx = 0;
    gbc.gridy = 4;
    container.add(label, gbc);
    gbc.gridx = 1;
    gbc.gridy = 4;
    container.add(daoPackageField, gbc);
    gbc.gridx = 2;
    gbc.gridy = 4;
    container.add(daoPackageButton, gbc);
  }

  private void bindButtonClickEvent(JButton button, String dialogTitla, Consumer<PsiPackage> packageConsumer) {
    button.addActionListener(e -> {
      if (module == null) {
        Messages.showWarningDialog("Please selecte module", "Warning");
        return;
      }
      PackageChooserDialog dialog = new PackageChooserDialog(dialogTitla, module);
      if (dialog.showAndGet()) {
        packageConsumer.accept(dialog.getSelectedPackage());
      }
    });
  }

  public Project getProject() {
    return project;
  }

  public Module getModule() {
    return module;
  }

  public PsiPackage getBasePackage() {
    return basePackage;
  }

  public PsiPackage getDomainPackage() {
    return domainPackage;
  }

  public PsiPackage getDaoPackage() {
    return daoPackage;
  }

}
