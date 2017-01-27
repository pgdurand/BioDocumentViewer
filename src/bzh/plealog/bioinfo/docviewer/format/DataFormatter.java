/* Copyright (C) 2017 Inria
 * Author: Patrick G. Durand
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
package bzh.plealog.bioinfo.docviewer.format;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.configuration.DirectoryManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.docviewer.config.DocViewerDirectoryType;

/**
 * This class contains methods to prepare documents using templates.
 * 
 * @author Patrick G. Durand, Inria
 */
public class DataFormatter {

  public static enum FORMAT {
    ENSEMBL_VAR_HTML
  }

  public static Hashtable<FORMAT, String> AVAILABLE_FORMATS;

  // use a singleton
  private static VelocityEngine _velocityEngine = null;

  // custom icons to render nice documents (taken from Ensembl)
  private static Hashtable<String, ImageIcon> _iconMap;

  static {
    AVAILABLE_FORMATS = new Hashtable<FORMAT, String>();
    AVAILABLE_FORMATS.put(FORMAT.ENSEMBL_VAR_HTML, "ensembl_var_index.vm");

    // icons:
    // http://www.ensembl.org/info/genome/variation/data_description.html#variation_sets
    _iconMap = new Hashtable<>();
    ImageIcon icon = EZEnvironment.getImageIcon("ens_var_association.png");
    icon.setDescription("ens_var_association.png");
    _iconMap.put("association", icon);

    icon = EZEnvironment.getImageIcon("ens_var_benign.png");
    icon.setDescription("ens_var_benign.png");
    _iconMap.put("benign", icon);

    icon = EZEnvironment.getImageIcon("ens_var_confers_sensitivity.png");
    icon.setDescription("ens_var_confers_sensitivity.png");
    _iconMap.put("confers sensitivity", icon);

    icon = EZEnvironment.getImageIcon("ens_var_drug_response.png");
    icon.setDescription("ens_var_drug_response.png");
    _iconMap.put("drug response", icon);

    icon = EZEnvironment.getImageIcon("ens_var_likely_benign.png");
    icon.setDescription("ens_var_likely_benign.png");
    _iconMap.put("likely benign", icon);

    icon = EZEnvironment.getImageIcon("ens_var_likely_pathogenic.png");
    icon.setDescription("ens_var_likely_pathogenic.png");
    _iconMap.put("likely pathogenic", icon);

    icon = EZEnvironment.getImageIcon("ens_var_uncertain.png");
    icon.setDescription("ens_var_uncertain.png");
    _iconMap.put("not provided", icon);
    _iconMap.put("other", icon);
    _iconMap.put("uncertain significance", icon);

    icon = EZEnvironment.getImageIcon("ens_var_pathogenic.png");
    icon.setDescription("ens_var_pathogenic.png");
    _iconMap.put("pathogenic", icon);

    icon = EZEnvironment.getImageIcon("ens_var_protective.png");
    icon.setDescription("ens_var_protective.png");
    _iconMap.put("protective", icon);

    icon = EZEnvironment.getImageIcon("ens_var_risk_factor.png");
    icon.setDescription("ens_var_risk_factor.png");
    _iconMap.put("risk factor", icon);

    icon = EZEnvironment.getImageIcon("ens_var_Multiple_observations.png");
    icon.setDescription("ens_var_Multiple_observations.png");
    _iconMap.put("Multiple observations", icon);

    icon = EZEnvironment.getImageIcon("ens_var_Frequency.png");
    icon.setDescription("ens_var_Frequency.png");
    _iconMap.put("Frequency", icon);

    icon = EZEnvironment.getImageIcon("ens_var_Cited.png");
    icon.setDescription("ens_var_Cited.png");
    _iconMap.put("Cited", icon);

    icon = EZEnvironment.getImageIcon("ens_var_Phenotype_or_Disease.png");
    icon.setDescription("ens_var_Phenotype_or_Disease.png");
    _iconMap.put("Phenotype or Disease", icon);
    _iconMap.put("Phenotype_or_Disease", icon);

    icon = EZEnvironment.getImageIcon("ens_var_1000_Genomes.png");
    icon.setDescription("ens_var_1000_Genomes.png");
    _iconMap.put("1000 Genomes", icon);
    _iconMap.put("1000Genomes", icon);

    icon = EZEnvironment.getImageIcon("ens_var_ExAC.png");
    icon.setDescription("ens_var_ExAC.png");
    _iconMap.put("ExAC", icon);

    icon = EZEnvironment.getImageIcon("ens_var_HapMap.png");
    icon.setDescription("ens_var_HapMap.png");
    _iconMap.put("HapMap", icon);

    icon = EZEnvironment.getImageIcon("ens_var_ESP.png");
    icon.setDescription("ens_var_ESP.png");
    _iconMap.put("ESP", EZEnvironment.getImageIcon("ens_var_ESP.png"));

  }

  /**
   * No default constructor available.
   */
  private DataFormatter() {
  }

  private static synchronized VelocityEngine prepareEngine() throws Exception {

    if (_velocityEngine != null)
      return _velocityEngine;

    VelocityEngine ve = new VelocityEngine();
    // disable Velocity logs (not useful)
    // ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
    // "org.apache.velocity.runtime.log.NullLogSystem");

    ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, 
        DirectoryManager.getPath(DocViewerDirectoryType.WEB_TEMPLATE));

    // init Velocity using above values
    ve.init();
    _velocityEngine = ve;
    return ve;
  }

  /**
   * Dump some data using a particular Velocity template.
   * 
   * @param outWriter
   *          where to write data. Note that this method calls flush() after
   *          writing, but does NOT close the stream.
   * @param item
   *          the data data to format
   * @param format
   *          the format code
   * 
   * @return true if success, false otherwise
   */
  public static boolean dump(Writer outWriter, Object item, FORMAT format) {
    VelocityEngine ve;
    VelocityContext context;
    Template t;
    StringWriter writer;
    boolean bRet = false;

    if (outWriter == null)
      return bRet;
    try {
      ve = prepareEngine();
      t = ve.getTemplate(AVAILABLE_FORMATS.get(format));
      context = new VelocityContext();
      context.put("item", item);
      writer = new StringWriter();
      t.merge(context, writer);
      outWriter.write(writer.toString());
      outWriter.flush();
      bRet = true;
    } catch (Exception e) {
      EZLogger.warn("unable to format data: " + e);
    }
    return bRet;
  }

  /**
   * Return a icon by name.
   * 
   * @param name
   *          name of the icon
   * 
   * @return an icon or null if not found. It is worth noting that returned Icon
   *         has description field initialized with icon file name.
   */
  public static ImageIcon getIcon(String name) {
    return _iconMap.get(name);
  }

}
