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
package bzh.plealog.bioinfo.docviewer.service.ncbi.model.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.api.filter.BRule;
import bzh.plealog.bioinfo.filter.implem.BRuleImplem;
import bzh.plealog.bioinfo.io.filter.BFilterIO;
import bzh.plealog.bioinfo.io.filter.BRuleIO;

/**
 * This is the implementation of a BFilter aims at modeling an NCBI Entrez
 * query.
 * 
 * @author Patrick G. Durand
 */
public class EntrezQueryExpressionBuilder implements BFilter {
  private String name_;
  private String description_;
  private boolean exclusive_;
  private ArrayList<BRule> rules_;
  private EntrezQueryModel filterModel_;
  private String entrezQueryRepr_;
  private boolean compiled_ = false;

  // This is the data model date format
  private final SimpleDateFormat _dateParser = new SimpleDateFormat("yyyyMMdd");
  // This the expected Entrez date format
  private final SimpleDateFormat _dateFormatter = new SimpleDateFormat("yyyy/MM/dd");

  public EntrezQueryExpressionBuilder(EntrezQueryModel fModel) {
    filterModel_ = fModel;
    rules_ = new ArrayList<>();
    description_ = "no description";
  }

  public EntrezQueryExpressionBuilder(EntrezQueryModel fModel, BFilterIO filter) {
    this(fModel);

    setName(filter.getName());
    setDescription(filter.getDescription());
    setExclusive(filter.isExclusive());

    Iterator<BRuleIO> iter = filter.getRules().iterator();
    while (iter.hasNext()) {
      rules_.add(new BRuleImplem((BRuleIO) iter.next()));
    }
  }

  public EntrezQueryExpressionBuilder(EntrezQueryModel fModel, String filterName) {
    this(fModel);
    setName(filterName);
  }

  private void formatEntrezField(StringBuffer buf, String value, String field) throws Exception {
    if (field.equals("PDAT") || field.equals("MDAT")) {
      buf.append(_dateFormatter.format(_dateParser.parse(value)));
    } else {
      buf.append(value);
    }
    if (field.equals("ALL") == false) {
      // to understand the above "if", try: (at least on May 2009)
      // http://www.ncbi.nlm.nih.gov/protein/?term=t+cell+receptor
      // and
      // http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=protein&term=t-cell+receptor[All]
      // conclusion:results are not the same ! NCBI portal actually does not use
      // the [All] tag !!!
      buf.append("[");
      buf.append(field);
      buf.append("]");
    }
  }

  /**
   * Use the NCBI Entrez query specifications to convert a BRule object into an
   * Entrez expression.
   */
  @SuppressWarnings("rawtypes")
  private String formatRule(BRule rule) throws Exception {
    StringBuffer buf;
    String       str;
    
    buf = new StringBuffer();
    if (rule.getOperator().equals("!:") || rule.getOperator().equals("!=")) {
      buf.append("NOT ");
      formatEntrezField(buf, rule.getValue().toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
    } else if (rule.getOperator().equals("[]")) {
      formatEntrezField(buf, ((List) rule.getValue()).get(0).toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
      buf.append(":");
      formatEntrezField(buf, ((List) rule.getValue()).get(1).toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
    } else if (rule.getOperator().indexOf(">") != -1) {// for numeric only
      formatEntrezField(buf, rule.getValue().toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
      buf.append(":");
      formatEntrezField(buf, String.valueOf(Integer.MAX_VALUE),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
    } else if (rule.getOperator().indexOf("<") != -1) {// for numeric only
      formatEntrezField(buf, String.valueOf(0), filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
      buf.append(":");
      formatEntrezField(buf, rule.getValue().toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
    } else {
      formatEntrezField(buf, rule.getValue().toString(),
          filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName());
    }
    str = buf.toString();
    EZLogger.debug("rule: "+str);  
    return str;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void compile() throws BFilterException {
    StringBuffer buf;
    BRule rule;
    int i, size;

    if (compiled_)
      return;

    EZLogger.debug("Format expression:");
    if (rules_.isEmpty()) {
      entrezQueryRepr_ = "empty";
      compiled_ = true;
      return;
    }

    buf = new StringBuffer();
    size = rules_.size();
    for (i = 0; i < size; i++) {
      rule = (BRule) rules_.get(i);
      try {
        buf.append(formatRule(rule));
      } catch (Exception e) {
        throw new BFilterException("Unable to format value:" + rule.getValue());
      }
      if ((i + 1) < size) {
        buf.append(exclusive_ ? " AND " : " OR ");
      }
    }
    // as requested by NCBI documentation, replace any space chars with a +
    // see https://www.ncbi.nlm.nih.gov/books/NBK25497/#chapter2.Usage_Guidelines_and_Requiremen
    entrezQueryRepr_ = buf.toString().replaceAll("\\ ", "+");
    EZLogger.debug("Full expression is: "+entrezQueryRepr_);
    compiled_ = true;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void add(BRule rule) throws BFilterException {
    rules_.add(rule);
    compiled_ = false;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void remove(BRule rule) {
    rules_.remove(rule);
    compiled_ = false;
  }

  /**
   * Implementation of BFilter interface.
   */
  public Iterator<BRule> getRules() {
    return rules_.iterator();
  }

  /**
   * Implementation of BFilter interface.
   */
  public int size() {
    return rules_.size();
  }

  /**
   * Implementation of BFilter interface.
   */
  public String getName() {
    return name_;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void setName(String filterName) {
    name_ = filterName;
  }

  /**
   * Implementation of BFilter interface.
   */
  public String getDescription() {
    return description_;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void setDescription(String filterDescription) {
    description_ = filterDescription;
  }

  /**
   * Implementation of BFilter interface.
   */
  public boolean getExclusive() {
    return exclusive_;
  }

  /**
   * Implementation of BFilter interface.
   */
  public void setExclusive(boolean val) {
    exclusive_ = val;
  }

  /**
   * Implementation of BFilter interface.
   */
  public String getHtmlString() {
    StringBuffer buf;
    int i, size;

    buf = new StringBuffer("<html><body>");
    size = rules_.size();
    if (size > 0) {
      for (i = 0; i < size; i++) {
        buf.append(((BRule) rules_.get(i)).getHtmlString(filterModel_));
        if ((i + 1) < size) {
          buf.append(exclusive_ ? " <br><i>and</i> " : " <br><i>or</i> ");
        }
      }
    } else {
      buf.append("empty");
    }
    buf.append("</body></html>");
    return buf.toString();
  }

  public String getTxtString() {
    StringBuffer buf;
    int i, size;

    buf = new StringBuffer();
    size = rules_.size();
    if (size > 0) {
      for (i = 0; i < size; i++) {
        buf.append(((BRule) rules_.get(i)).getTxtString(filterModel_));
        if ((i + 1) < size) {
          buf.append(exclusive_ ? " and " : " or ");
        }
      }
    } else {
      buf.append("empty");
    }
    return buf.toString();
  }

  public String toString() {
    if (compiled_)
      return entrezQueryRepr_;
    else
      return "filter not compiled.";
  }

  public Object clone() {
    EntrezQueryExpressionBuilder filter = new EntrezQueryExpressionBuilder(filterModel_);
    filter.copy(this);
    return filter;
  }

  public SROutput execute(SROutput bo) throws BFilterException {
    return null;
  }

  protected void copy(BFilter src) {
    Iterator<BRule> iter;

    this.setName(src.getName());
    this.setDescription(src.getDescription());
    this.setExclusive(src.getExclusive());
    iter = src.getRules();
    while (iter.hasNext()) {
      this.add((BRule) ((BRule) iter.next()).clone());
    }
  }
}
