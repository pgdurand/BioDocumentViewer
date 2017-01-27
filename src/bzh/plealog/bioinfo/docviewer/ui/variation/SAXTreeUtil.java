package bzh.plealog.bioinfo.docviewer.ui.variation;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;

import bzh.plealog.bioinfo.docviewer.format.DataFormatter;

/**
 * Contains some utility methods to handle Tree models made from XML documents.
 */
public class SAXTreeUtil {
  // codes adapted from:
  // http://www.java2s.com/Code/Java/Swing-JFC/ExpandingorCollapsingAllNodesinaJTreeComponent.htm
  // http://www.java2s.com/Code/Java/XML/XMLTreeView.htm

  /**
   * Convert an XML document to a Tree model.
   * 
   * @param docName
   *          name of document. Used as the label of root node.
   * @param file
   *          the XML file to load
   *
   * @return the root node of a tree
   * 
   * @throws RuntimeException
   *           if something wrong occurs during XML parsing
   */
  public static TreeNode loadXMLDocument(String docName, File file, String rootNode) {
    // Create a tree from XML document
    SAXTreeBuilder saxTree = new SAXTreeBuilder(docName);
    saxTree.setAlternativeStartingNode(rootNode);
    
    try (FileInputStream fis = new FileInputStream(file)) {
      SAXParser saxParser = new SAXParser();
      saxParser.setContentHandler(saxTree);
      saxParser.parse(new InputSource(fis));
    } catch (Exception ex) {
      // handle FileNotFoundException, IOException and SAXParserException at
      // once
      throw new RuntimeException(ex);
    }

    // Create a Tree viewer from Tree Model
    return saxTree.getTree();
  }

  /**
   * Expand all nodes of a tree.
   * 
   * @param tree
   *          the tree to handle
   */
  public static void expandAll(JTree tree) {
    TreeNode root = (TreeNode) tree.getModel().getRoot();
    expandAll(tree, new TreePath(root));
  }

  @SuppressWarnings("rawtypes")
  private static void expandAll(JTree tree, TreePath parent) {
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration e = node.children(); e.hasMoreElements();) {
        TreeNode n = (TreeNode) e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path);
      }
    }
    tree.expandPath(parent);
  }

  public static void setVariationTreeRenderer(JTree tree) {
    tree.setCellRenderer(new VariationTreeRenderer());
  }

  private static class VariationTreeRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 3684498562617794122L;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf,
        int row, boolean hasFocus) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      String s = node.getUserObject().toString();
      ImageIcon icon = DataFormatter.getIcon(s);
      if (icon != null) {
        setLeafIcon(icon);
      } else {
        setLeafIcon(getDefaultLeafIcon());
      }
      super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
      return this;
    }
  }

}
