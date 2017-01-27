package bzh.plealog.bioinfo.docviewer.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.plealog.genericapp.api.configuration.DirectoryManager;
import com.plealog.genericapp.api.configuration.DirectoryManagerException;
import com.plealog.genericapp.api.configuration.DirectoryType;

import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;
import bzh.plealog.bioinfo.util.ZipUtil;

/**
 * Implement the structure of directories of the Document Viewer. They are used
 * to store locally some date for the proper work of the tool.
 * 
 * @author Patrick G. Durand
 */
public enum DocViewerDirectoryType implements DirectoryType {

  CONF("conf", 0), DOCUMENTS("documents", 1), FILTER("filter", 2), WEB_TEMPLATE("web", 3);

  private final String _dName;
  private final int _code;

  DocViewerDirectoryType(String dname, int code) {
    _dName = dname;
    _code = code;
  }

  public String getDirectory() {
    return _dName;
  }

  public void prepareDirectory() throws DirectoryManagerException {
    // web-template
    if (_code == 3) {
      deployWebTemplate();
    }
  }

  public static List<DirectoryType> getAllValues(){
    return Arrays.asList(DocViewerDirectoryType.values());
  }
  
  private void deployWebTemplate() {
    String path;

    try {
      path = DirectoryManager.getPath(WEB_TEMPLATE);
    } catch (IOException ex) {
      throw new DirectoryManagerException("Unable to get web template directory: " + ex.toString());
    }
    // TODO: will have to find a way to update web-template... not done now,
    // since there is no reason to update jquery/boostrap/etc.
    if (new File(path + "jquery").exists()) {
      return;
    }

    try (InputStream in = Messages.class.getResourceAsStream("web-renderer.zip");) {
      ZipUtil.unzip(in, path);
    } catch (Exception ex) {
      throw new DirectoryManagerException("Unable to install web template: " + ex.toString());
    }
  }
}