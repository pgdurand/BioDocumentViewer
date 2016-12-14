/* Copyright (C) 2006-2016 Patrick G. Durand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/agpl-3.0.txt
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 */
package bzh.plealog.bioinfo.docviewer.ui;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import bzh.plealog.bioinfo.docviewer.api.BankProvider;
import bzh.plealog.bioinfo.docviewer.api.BankType;
import bzh.plealog.bioinfo.docviewer.service.ebi.EbiProvider;
import bzh.plealog.bioinfo.docviewer.service.ensembl.EnsemblProvider;
import bzh.plealog.bioinfo.docviewer.service.ncbi.EntrezProvider;
import bzh.plealog.bioinfo.docviewer.ui.resources.Messages;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileTypes;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.api.log.EZLoggerManager;
import com.plealog.genericapp.api.log.EZLoggerManager.LogLevel;
import com.plealog.genericapp.api.log.EZSingleLineFormatter;

/**
 * Contains some configuration elements of the software.
 * 
 * @author Patrick G. Durand
 * @since 2006
 */
public class DocViewerConfig {

  /**
   * JVM optional argument. Switch debug mode to true or false. If not provided,
   * debug mode is disabled. Sample use: -DV_DEBUG=true
   */
  public static final String                         JVM_ARG_DEBUG        = "V_DEBUG";
  /**
   * JVM optional argument. Provide an email address, usually required by
   * NCBI/EBI to use their web services. It enables Public Institutes to get in
   * touch with users producing too many requests per second... and before
   * black-listing them! If not provided, the software will ask for an email
   * address at runtime. Sample use: -DV_EMAIL=john.doe\@google.com<br>
   * <br>
   * See:<br>
   * https://www.ncbi.nlm.nih.gov/home/about/policies.shtml#scripting<br>
   * http://www.ebi.ac.uk/Tools/webservices/help/faq<br>
   */
  public static final String                         JVM_ARG_EMAIL        = "V_EMAIL";
  /**
   * JVM optional argument. Provide a configuration directory. If not provided,
   * software locates its resources and configuration files within compiled Java
   * codes. Sample use: -DV_CONF=./config
   */
  public static final String                         JVM_ARG_CONF         = "V_CONF";
  /**
   * JVM optional argument. Provide a bank provider. If not provided, software
   * uses NCBI as default. Sample use: -DV_PROVIDER=EBI
   */
  public static final String                         JVM_ARG_BK_PROVIDER  = "V_PROVIDER";

  /**
   * The following enables to add BankProvider as plugins. Simply design a new
   * BankProvider class, then put compiled code in a JAR and add the following
   * attribute in your manifest. Value for this attribute is the full class
   * name, including package name. Several classes can be provide at once by
   * using a comma separated list. <br>
   * <br>
   * Sample MANIFEST.mf line:<br>
   * doc-viewer-bank-provider=com.foo.bar.MyBankProvider
   */
  public static String                               MF_DOC_PROVIDER_ATTR = "doc-viewer-bank-provider";

  public static final String                         FAS_FEXT             = "fas";
  public static final String                         DAT_FEXT             = "dat";
  public static final String                         PNG_FEXT             = "png";

  public static ImageIcon                            WORKING_ICON;
  public static ImageIcon                            DBXPLR_ICON;
  public static ImageIcon                            DNA_ICON;
  public static ImageIcon                            PROTEIN_ICON;
  public static ImageIcon                            STRUCT_ICON;

  private static String                              USER_EMAIL;
  private static BankProvider                        DEFAULT_PROVIDER     = null;

  public static int                                  PAGE_SIZE            = 100;

  private static List<Class<? extends BankProvider>> PROVIDER_LIST;

  static {
    // this is required to enable application finding resources (messages,
    // images, etc)
    EZEnvironment.addResourceLocator(Messages.class);

    EZFileTypes.registerFileType("fas", "Fasta file");
    EZFileTypes.registerFileType("dat", "Full entry");

    WORKING_ICON = EZEnvironment.getImageIcon("circle_all.gif");
    DBXPLR_ICON = EZEnvironment.getImageIcon("dbXplor.png");

    DNA_ICON = EZEnvironment.getImageIcon("dnaIcon.png");
    PROTEIN_ICON = EZEnvironment.getImageIcon("protIcon.png");
    STRUCT_ICON = EZEnvironment.getImageIcon("alphaHelix.png");

    // Enable adding external provider as a plugin.
    // see loadDataProviderFromJarManifest(), below.
    PROVIDER_LIST = new ArrayList<>();
    PROVIDER_LIST.add(EbiProvider.class);
    PROVIDER_LIST.add(EnsemblProvider.class);
    PROVIDER_LIST.add(EntrezProvider.class);
  }

  private static boolean isValidEmail(String email) {
    // For now, I prefer not using strong EmailValidator. It's up to the user
    // to provide a valid email address. In the worst case, if he/she
    // "overloads"
    // NCBI/EBI services with too many requests per second, then:
    // 1. he/she's provided a valid email: NCBI/EBI can contact user
    // 2. no valid email, NCBI/EBI cannot contact user: he/she'll be
    // black-listed!
    // see https://www.ncbi.nlm.nih.gov/home/about/policies.shtml#scripting
    // and http://www.ebi.ac.uk/Tools/webservices/help/faq
    return (email != null && email.indexOf('@') != -1 && email.indexOf('.') != -1);
  }

  /**
   * Get the user email. Such a value is usually required by public web
   * services, such as NCBI and EBI.
   */
  public static String getUserEmail() {
    if (USER_EMAIL != null) {
      return USER_EMAIL;
    }
    // email from command-line; this line enable to override stored
    // value in app property
    String email = System.getProperty(JVM_ARG_EMAIL);
    if (isValidEmail(email)) {
      USER_EMAIL = email;
      EZEnvironment.setApplicationProperty(JVM_ARG_EMAIL, email);
      return USER_EMAIL;
    }
    // email from app properties
    email = EZEnvironment.getApplicationProperty(JVM_ARG_EMAIL);
    if (email != null) {
      USER_EMAIL = email;
      return USER_EMAIL;
    }
    // email from UI
    while (true) {// force the user to get a value
      email = EZEnvironment.inputValueMessage(EZEnvironment.getParentFrame(),
          Messages.getString("DocViewer.email.msg"));
      if (isValidEmail(email)) {
        USER_EMAIL = email;
        EZEnvironment.setApplicationProperty(JVM_ARG_EMAIL, email);
        break;
      }
    }
    return USER_EMAIL;
  }

  public static void setUserMail(String email) {
    if (!isValidEmail(email)) {
      throw new RuntimeException("invalid email");
    }
    USER_EMAIL = email;
  }

  /**
   * Provide the bank service to use to start the software. Default value is
   * NCBI. Rely on the JVM argument BK_PROVIDER. Use: -DBK_PROVIDER=xxx with xxx
   * being a bank provider name. If name is invalid, use NCBI.
   */
  public static BankProvider getBankProvider() {
    if (DEFAULT_PROVIDER == null) {
      String provider = System.getProperty(JVM_ARG_BK_PROVIDER);
      if (provider != null) {
        // Locate BankProviders in available classes
        BankProvider bp = null;
        loadDataProviderFromJarManifest();
        for (Class<? extends BankProvider> bt : PROVIDER_LIST) {
          EZLogger.debug(bt.getName());
          if (bt.isInterface())
            continue;
          try {
            bp = bt.newInstance();
          } catch (InstantiationException e) {
            EZLogger.warn("Unable to instantiate BankProvider:" + bt.getName());
          } catch (IllegalAccessException e) {
            EZLogger.warn("Unable to get access to BankProvider:"
                + bt.getName());
          }
          if (bp != null && bp.getProviderName().equalsIgnoreCase(provider)) {
            DEFAULT_PROVIDER = bp;
            break;
          }
        }
        if (DEFAULT_PROVIDER == null) {
          EZLogger.warn("Unknown BankProvider: " + provider);
        }
      }
    }
    if (DEFAULT_PROVIDER == null) {
      DEFAULT_PROVIDER = new EntrezProvider();
    }
    EZLogger.debug("Bank provider is: " + DEFAULT_PROVIDER.getProviderName());
    return DEFAULT_PROVIDER;
  }

  /**
   * Set the bank service.
   * 
   * @param provider
   *          the bank service to use
   */
  public static void setBankProvider(BankProvider provider) {
    if (provider == null)
      return;
    DEFAULT_PROVIDER = provider;
  }

  /**
   * Set the log level to info or debug. Rely on the JVM argument DV_DEBUG. Use:
   * -DDV_DEBUG=true.
   */
  public static void initLogLevel() {
    String dbg = System.getProperty(JVM_ARG_DEBUG);
    if (dbg != null) {
      EZLoggerManager
          .setLevel(dbg.toLowerCase().equals("true") ? LogLevel.debug
              : LogLevel.info);
    }
    // set the Formatter of the UI logger: in debug mode, provide full
    // class/method names
    EZLoggerManager.setUILoggerFormatter(new EZSingleLineFormatter(
        EZLoggerManager.getLevel() == LogLevel.debug));
  }

  /**
   * Return the software configuration file. By default, there is no such
   * directory available. Has to be setup using JRM argument: DV_CONF, with
   * value targeting a directory.
   */
  public static String getConfigurationPath() {
    String confP = System.getProperty(JVM_ARG_CONF);

    if (confP == null)
      return null;

    return EZFileUtils.terminatePath(confP);
  }

  /**
   * Return the content of the version resource.
   */
  public static Properties getVersionProperties() {
    Properties props = new Properties();
    try (InputStream in = DocViewerConfig.class
        .getResourceAsStream("version.properties");) {
      props.load(in);
      in.close();
    } catch (Exception ex) {// should not happen
      System.err.println("Unable to read props: " + ex.toString());
    }
    return props;
  }

  /**
   * Dump application properties. Only used for debugging purpose.
   */
  public static void dumpApplicationProperties() {

    if (EZLoggerManager.getLevel() != LogLevel.debug)
      return;

    List<String> keys = EZEnvironment.getApplicationPropertyKeys();
    if (keys == null)
      return;
    EZLogger.debug("Environment is: ");
    for (String key : keys) {
      EZLogger.debug(String.format("  %s: %s", key,
          EZEnvironment.getApplicationProperty(key)));
    }
  }

  /**
   * Return the path where filters targeting particular banks can be saved to or
   * loaded from.
   * 
   * @return a path or null if that path cannot be created.
   */
  public static String getFilterStoragePath(BankType bt) {
    String str;
    Properties props;
    File f;

    props = getVersionProperties();

    str = props.getProperty("prg.name");

    f = new File(EZFileUtils.terminatePath(System.getProperty("user.home"))
        + "." + str + File.separator + "filters" + File.separator
        + bt.getProviderName() + File.separator + bt.getCode());
    if (!f.exists()) {
      if (!f.mkdirs()) {
        EZLogger.warn("Unable to create path: " + f.getAbsolutePath());
        return null;
      }
    }
    return f.getAbsolutePath();
  }

  /**
   * Look through all JAR used by this application and locate property defined
   * by constant MF_DOC_PROVIDER_ATTR. If found, then data providers are loaded
   * from provided classes. When providing several classes, use a comma
   * separated list and no spaces in between.
   */
  @SuppressWarnings("unchecked")
  private static void loadDataProviderFromJarManifest() {
    String token, strClassPath, token2, attrValue;
    StringTokenizer tokenizer, tokenizer2;
    Manifest m;
    Attributes attr;
    
    EZLogger.debug("Loading external BankProviders");
    strClassPath = System.getProperty("java.class.path");
    tokenizer = new StringTokenizer(strClassPath, File.pathSeparator);
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (token.endsWith(".jar") == false) {
        continue;
      }
      EZLogger.debug("  Looking for BankProvider in: " + token);
      try (JarFile jr = new JarFile(token)) {
        m = jr.getManifest();
        attr = m.getMainAttributes();
        if (attr != null) {
          attrValue = attr.getValue(MF_DOC_PROVIDER_ATTR);
          if (attrValue != null) {
            tokenizer2 = new StringTokenizer(attrValue, ",");
            while (tokenizer2.hasMoreTokens()) {
              token2 = tokenizer2.nextToken();
              try {
                EZLogger.debug("    found provider: " + token2);
                // we could check that provided class in an appropriate
                // BankProvider using Reflection... well, let JRE does
                // the job: the following may throw a ClassCastException
                PROVIDER_LIST.add((Class<? extends BankProvider>) Class
                    .forName(token2));
                EZLogger.debug("      provider ok");
              } catch (Exception ex) {
                EZLogger.debug("      cannot use it: " + ex.toString());
              }
            }
          }
        }
      } catch (Exception e) {
        EZLogger.debug("    Unable to read: " + token + ": " + e.toString());
      }
    }
  }

}
