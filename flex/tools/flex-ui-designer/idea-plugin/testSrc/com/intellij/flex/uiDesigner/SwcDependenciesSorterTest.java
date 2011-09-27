package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Flex(version="4.5")
public class SwcDependenciesSorterTest extends MxmlTestBase {
  @Override
  protected String generateSdkName(String version, boolean air) {
    return getName();
  }

  @Override
  protected void modifySdk(final Sdk sdk, SdkModificator sdkModificator) {
    // must be added before super (i. e. before framework.swc)
    if (getName().equals("testDelete")) {
      addLibrary(sdkModificator, getTestDataPath() + "/flash-integration_4.1.swc");
    }
    else if (getName().equals("testIgnoreSwcWithoutLibraryFile")) {
      addLibrary(sdkModificator, getTestDataPath() + "/swcWithoutLibrarySwf.swc");
    }
    
    super.modifySdk(sdk, sdkModificator);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      addLibrary(sdkModificator, getTestDataPath() + "/spark_dmv_4.5.swc");
    }
    else if (getName().equals("testResolveToClassWithBiggestTimestamp")) {
      final String path = getTestDataPath() + "/ResolveToClassWithBiggestTimestamp/bin/";
      addLibrary(sdkModificator, path + "lib_1.swc");
      addLibrary(sdkModificator, path + "lib_2.swc");
    }

    Disposer.register(myModule, new Disposable() {
      @Override
      public void dispose() {
        AccessToken token = WriteAction.start();
        try {
          final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
          projectJdkTable.removeJdk(sdk);
        }
        finally {
          token.finish();
        }
      }
    });
  }

  @Override
  protected void assertAfterInitLibrarySets(XmlFile[] unregisteredDocumentReferences) throws IOException {
    super.assertAfterInitLibrarySets(unregisteredDocumentReferences);

    if (getName().equals("testDeleteIfAllDefitionsHaveUnresolvedDependencies")) {
      for (LibrarySetItem item : myLibraries()) {
        assertFalse(item.library.getPath().contains("spark_dmv"));
      }
    }
  }

  private List<LibrarySetItem> myLibraries() {
    return client.getRegisteredProjects().getInfo(myProject).getSdkLibrarySet().getItems();
  }

  public void testDeleteIfAllDefitionsHaveUnresolvedDependencies() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }
  
  @SuppressWarnings({"UnusedDeclaration"})
  @Flex(version="4.1")
  public void _TODO_testDelete() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testResolveToClassWithBiggestTimestamp() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  public void testIgnoreSwcWithoutLibraryFile() throws Exception {
    testFile(SPARK_COMPONENTS_FILE);
  }

  @Override
  protected void tearDown() throws Exception {
    if (appDir != null && appDir.exists()) {
      LibrarySet sdkLibrarySet = client.getRegisteredProjects().getInfo(myProject).getSdkLibrarySet();
      for (LibrarySetItem item : myLibraries()) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, item.library.getPath() + (item.filtered ? "_" + sdkLibrarySet.getId() + ".swf" : ".swf")).delete();
      }
    }

    super.tearDown();
  }
}