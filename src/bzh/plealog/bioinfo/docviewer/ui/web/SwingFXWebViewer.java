package bzh.plealog.bioinfo.docviewer.ui.web;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

// adapted from: http://docs.oracle.com/javafx/2/swing/SimpleSwingBrowser.java.htm
// see also https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/swing-fx-interoperability.htm
//          for interoperability issues

public class SwingFXWebViewer extends JPanel {

  private static final long serialVersionUID = 4632103616553133109L;
  private JFXPanel _jfxPanel;
  private WebEngine _engine;
  private JLabel _lblStatus;
  private JButton _btnGo;
  private JTextField _txtURL;
  private JProgressBar _progressBar;
  private String _url;
  private boolean _viewerOnly = false;

  /**
   * Constructor.
   */
  public SwingFXWebViewer() {
    initComponents();
  }

  /**
   * Constructor.
   * 
   * @param url
   *          the URL targeting a web document to display.
   */
  public SwingFXWebViewer(String url) {
    this(url, false);
  }

  /**
   * Constructor.
   * 
   * @param url
   *          the URL targeting a web document to display.
   */
  public SwingFXWebViewer(String url, boolean viewerOnly) {
    super();
    _url = url;
    _viewerOnly = viewerOnly;
    initComponents();
  }

  /**
   * Create the user interface.
   */
  private void initComponents() {

    _jfxPanel = new JFXPanel();
    _lblStatus = new JLabel();
    _btnGo = new JButton("Go");
    _txtURL = new JTextField(_url != null ? _url : "");
    _progressBar = new JProgressBar();

    createScene();

    ActionListener al = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        load(_txtURL.getText());
      }
    };

    _btnGo.addActionListener(al);
    _txtURL.addActionListener(al);

    _progressBar.setPreferredSize(new Dimension(150, 18));
    _progressBar.setStringPainted(true);

    JPanel topBar = new JPanel(new BorderLayout(5, 0));
    topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    topBar.add(_txtURL, BorderLayout.CENTER);
    topBar.add(_btnGo, BorderLayout.EAST);

    JPanel statusBar = new JPanel(new BorderLayout(5, 0));
    statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
    statusBar.add(_lblStatus, BorderLayout.CENTER);
    statusBar.add(_progressBar, BorderLayout.EAST);

    this.setLayout(new BorderLayout());
    if (!_viewerOnly) {
      this.add(topBar, BorderLayout.NORTH);
    }
    this.add(_jfxPanel, BorderLayout.CENTER);
    this.add(statusBar, BorderLayout.SOUTH);
  }

  private void createScene() {

    Platform.runLater(new Runnable() {
      @Override
      public void run() {

        WebView view = new WebView();
        _engine = view.getEngine();

        _engine.titleProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                // Not available here: require a JFrame for instance
                // SimpleSwingBrowser.this.setTitle(newValue);
              }
            });
          }
        });

        _engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
          @Override
          public void handle(final WebEvent<String> event) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                _lblStatus.setText(event.getData());
              }
            });
          }
        });

        _engine.locationProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                _txtURL.setText(newValue);
              }
            });
          }
        });

        _engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
          @Override
          public void changed(ObservableValue<? extends Number> observableValue, Number oldValue,
              final Number newValue) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                _progressBar.setValue(newValue.intValue());
              }
            });
          }
        });

        _engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {

          public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
            if (_engine.getLoadWorker().getState() == FAILED) {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  JOptionPane.showMessageDialog(SwingFXWebViewer.this,
                      (value != null) ? _engine.getLocation() + "\n" + value.getMessage()
                          : _engine.getLocation() + "\nUnexpected error.",
                      "Loading error...", JOptionPane.ERROR_MESSAGE);
                }
              });
            }
          }
        });

        _jfxPanel.setScene(new Scene(view));
      }
    });
  }

  /**
   * Load a document.
   * 
   * @param url
   *          the URL targeting a web document to display.
   */
  public void load(String url) {
    _url = url;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        _engine.load(_url);
      }
    });
  }
}
