package me.gaigeshen.idea.ecmybatis;

import com.google.common.collect.Sets;
import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.scopes.ModulesScope;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.FilenameIndex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author gaigeshen
 */
public class Initializer {


  private static String SPRING_BEANS_LOCATOR_CONTENT;
  private static final String SPRING_BEANS_LOCATOR_SIMPLE = "SpringBeansLocator";
  private static final String SPRING_BEANS_LOCATOR_FILE = "/base/SpringBeansLocator.java.txt";

  private static String BASE_DAO_CONTENT;
  private static final String BASE_DAO_SIMPLE = "BaseDao";
  private static final String BASE_DAO_FILE = "/base/dao/BaseDao.java.txt";

  private static String CONDITION_CONTENT;
  private static final String CONDITION_SIMPLE = "Condition";
  private static final String CONDITION_FILE = "/base/dao/Condition.java.txt";

  private static String DAO_CONTENT;
  private static final String DAO_SIMPLE = "Dao";
  private static final String DAO_FILE = "/base/dao/Dao.java.txt";

  private static String PAGE_DATA_CONTENT;
  private static final String PAGE_DATA_SIMPLE = "PageData";
  private static final String PAGE_DATA_FILE = "/base/dao/PageData.java.txt";

  private static String RESULT_MAPPINGS_CONTENT;
  private static final String RESULT_MAPPINGS_SIMPLE = "ResultMappings";
  private static final String RESULT_MAPPINGS_FILE = "/base/dao/ResultMappings.java.txt";

  private static String BASE_MODEL_CONTENT;
  private static final String BASE_MODEL_SIMPLE = "BaseModel";
  private static final String BASE_MODEL_FILE = "/base/domain/BaseModel.java.txt";

  private static String MODEL_CONTENT;
  private static final String MODEL_SIMPLE = "Model";
  private static final String MODEL_FILE = "/base/domain/Model.java.txt";

  static {
    initializeSpringBeansLocatorContent();
    initializeBaseDaoContent();
    initializeConditionContent();
    initializeDaoContent();
    initializePageDataContent();
    initializeResultMappingsContent();
    initializeBaseModelContent();
    initializeModelContent();
  }

  private Initializer() {

  }

  public static Initializer create() {
    return new Initializer();
  }

  /**
   * 生成相关的文件
   *
   * @param module 模块
   * @param basePackage 基础包
   * @param domainPackage 领域对象的包
   * @param daoPackage 数据访问对象的包
   */
  public void generate(Module module,
                       PsiPackage basePackage,
                       PsiPackage domainPackage,
                       PsiPackage daoPackage) {

    if (domainPackage.isValid() && daoPackage.isValid()) {
      generateSpringBeansLocator(module, basePackage);
      generateBaseDao(module, daoPackage, domainPackage);
      generateCondition(module, daoPackage);
      generateDao(module, daoPackage, domainPackage);
      generatePageData(module, daoPackage);
      generateResultMappings(module, daoPackage, basePackage);
      generateBaseModel(module, daoPackage, domainPackage);
      generateModel(module, domainPackage);
    } else {
      Messages.showWarningDialog("Domain package or dao package is invalid", "Warning");
    }
  }

  /**
   *
   * @param module
   * @param basepkg
   */
  private void generateSpringBeansLocator(Module module, PsiPackage basepkg) {
    String content = SPRING_BEANS_LOCATOR_CONTENT
            .replaceAll("_basePackage_", basepkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, SPRING_BEANS_LOCATOR_SIMPLE, content, basepkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   * @param domainpkg
   */
  private void generateBaseDao(Module module, PsiPackage daopkg, PsiPackage domainpkg) {
    String content = BASE_DAO_CONTENT
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_domainPackage_", domainpkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, BASE_DAO_SIMPLE, content, daopkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   */
  private void generateCondition(Module module, PsiPackage daopkg) {
    String content = CONDITION_CONTENT
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, CONDITION_SIMPLE, content, daopkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   * @param domainpkg
   */
  private void generateDao(Module module, PsiPackage daopkg, PsiPackage domainpkg) {
    String content = DAO_CONTENT
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_domainPackage_", domainpkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, DAO_SIMPLE, content, daopkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   */
  private void generatePageData(Module module, PsiPackage daopkg) {
    String content = PAGE_DATA_CONTENT
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, PAGE_DATA_SIMPLE, content, daopkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   * @param basepkg
   */
  private void generateResultMappings(Module module, PsiPackage daopkg, PsiPackage basepkg) {
    String content = RESULT_MAPPINGS_CONTENT
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_basePackage_", basepkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, RESULT_MAPPINGS_SIMPLE, content, daopkg);
  }

  /**
   *
   * @param module
   * @param daopkg
   * @param domainpkg
   */
  private void generateBaseModel(Module module, PsiPackage daopkg, PsiPackage domainpkg) {
    String content = BASE_MODEL_CONTENT
            .replaceAll("_domainPackage_", domainpkg.getQualifiedName())
            .replaceAll("_daoPackage_", daopkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, BASE_MODEL_SIMPLE, content, domainpkg);
  }

  /**
   *
   * @param module
   * @param domainpkg
   */
  private void generateModel(Module module, PsiPackage domainpkg) {
    String content = MODEL_CONTENT
            .replaceAll("_domainPackage_", domainpkg.getQualifiedName())
            .replaceAll("_since_", new SimpleDateFormat("MM/dd yyyy").format(new Date()));

    generateFile(module, MODEL_SIMPLE, content, domainpkg);
  }

  private static void initializeSpringBeansLocatorContent() {
    if (SPRING_BEANS_LOCATOR_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(SPRING_BEANS_LOCATOR_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          SPRING_BEANS_LOCATOR_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize spring beans locator content", e);
      }
    }
  }

  private static void initializeBaseDaoContent() {
    if (BASE_DAO_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(BASE_DAO_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          BASE_DAO_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize base dao content", e);
      }
    }
  }

  private static void initializeConditionContent() {
    if (CONDITION_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(CONDITION_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          CONDITION_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize condition content", e);
      }
    }
  }

  private static void initializeDaoContent() {
    if (DAO_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(DAO_FILE)) {
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

  private static void initializePageDataContent() {
    if (PAGE_DATA_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(PAGE_DATA_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          PAGE_DATA_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize page data content", e);
      }
    }
  }

  private static void initializeResultMappingsContent() {
    if (RESULT_MAPPINGS_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(RESULT_MAPPINGS_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          RESULT_MAPPINGS_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize result mappings content", e);
      }
    }
  }

  private static void initializeBaseModelContent() {
    if (BASE_MODEL_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(BASE_MODEL_FILE)) {
        if (in != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();

          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          BASE_MODEL_CONTENT = out.toString("utf-8");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not initialize base model content", e);
      }
    }
  }

  private static void initializeModelContent() {
    if (MODEL_CONTENT == null) {
      try (InputStream in = Initializer.class.getResourceAsStream(MODEL_FILE)) {
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
   * 生成类文件
   *
   * @param module 模块
   * @param className 类名称
   * @param content 类的文本内容
   * @param pkg 所在包
   */
  private void generateFile(Module module,
                            String className,
                            String content,
                            PsiPackage pkg) {
    if (!pkg.isValid()) {
      throw new IllegalStateException("Package is invalid: " + pkg.getName());
    }

    PsiFile[] psiFiles = FilenameIndex.getFilesByName(module.getProject(), className + ".java", new ModulesScope(Sets.newHashSet(module), module.getProject()));
    if (psiFiles.length == 0) { // 已经存在的类不会再次生成
      PsiFile psiFile = PsiFileFactory.getInstance(module.getProject()).createFileFromText(className + ".java", StdFileTypes.JAVA, content);
      PsiDirectory packageDirectory = PackageUtil.findPossiblePackageDirectoryInModule(module, pkg.getQualifiedName());
      if (packageDirectory == null) {
        Messages.showWarningDialog("The package is missing or invalid: " + pkg.getQualifiedName(), "Warning");
        return;
      }
      WriteCommandAction.runWriteCommandAction(module.getProject(), () -> {
        packageDirectory.add(psiFile);
      });
    }
  }

}
