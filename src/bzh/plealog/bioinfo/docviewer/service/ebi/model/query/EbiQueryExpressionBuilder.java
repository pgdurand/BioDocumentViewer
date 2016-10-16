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
package bzh.plealog.bioinfo.docviewer.service.ebi.model.query;

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
 * This is the implementation of a BFilter aims at modeling an EBI_Eye
 * query.
 * 
 * @author Patrick G. Durand
 */
public class EbiQueryExpressionBuilder implements BFilter {
  private String name_;
  private String description_;
  private boolean exclusive_;
  private ArrayList<BRule> rules_;
  private EbiQueryModel filterModel_;
  private String ebieyeQueryRepr_;
  private boolean compiled_ = false;

  // This is the data model date format
  private final SimpleDateFormat _dateParser = new SimpleDateFormat("yyyyMMdd");
  // This the expected EBI date format
  private final SimpleDateFormat _dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  public EbiQueryExpressionBuilder(EbiQueryModel fModel) {
    filterModel_ = fModel;
    rules_ = new ArrayList<>();
    description_ = "no description";
  }

  public EbiQueryExpressionBuilder(EbiQueryModel fModel, BFilterIO filter) {
    this(fModel);

    setName(filter.getName());
    setDescription(filter.getDescription());
    setExclusive(filter.isExclusive());

    Iterator<BRuleIO> iter = filter.getRules().iterator();
    while (iter.hasNext()) {
      rules_.add(new BRuleImplem((BRuleIO) iter.next()));
    }
  }

  public EbiQueryExpressionBuilder(EbiQueryModel fModel, String filterName) {
    this(fModel);
    setName(filterName);
  }

  private String getFormattedField(String field, String value) throws Exception{
    if (field.contains("_date")){
      return _dateFormatter.format(_dateParser.parse(value));
    }
    else{
      return value;
    }
  }
  
  private void formatEbiField(StringBuffer buf, Object value, String field, String operator) throws Exception {
    if("ALL".equals(field)){
      //ALL: all fields search: we just provide the string, no particular reformatting
      buf.append(value.toString());
    }
    else if("status".equals(field)){
      //control user entry; we only allow valid term
      if (value.toString().toLowerCase().equals("reviewed")){
        buf.append("status:(Reviewed)");
      }
      else if (value.toString().toLowerCase().equals("unreviewed")){
        buf.append("status:(Unreviewed)");
      }
    }
    else{ 
      buf.append(field);
      if (value instanceof List<?>){
        //range (for dates and length): field:[a TO b]
        buf.append(":[");
        buf.append(getFormattedField(field, ((List<?>) value).get(0).toString()));
        buf.append(" TO ");
        buf.append(getFormattedField(field, ((List<?>) value).get(1).toString()));
        buf.append("]");
      }
      else{
        buf.append(":(");
        buf.append(getFormattedField(field, value.toString()));
        buf.append(")");
      }
    }
  }

  /**
   * Use the EBI-Eye query specifications to convert a BRule object into an
   * EBI_eye expression.
   */
  private String formatRule(BRule rule) throws Exception {
    StringBuffer buf;
    String       str;
    
    buf = new StringBuffer();
    formatEbiField(
        buf, 
        rule.getValue(),
        filterModel_.getAccessorEntry(rule.getAccessor()).getAccessorName(),
        rule.getOperator());
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
      ebieyeQueryRepr_ = "empty";
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
    ebieyeQueryRepr_ = buf.toString().replaceAll("\\ ", "+");
    EZLogger.debug("Full expression is: "+ebieyeQueryRepr_);
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
      return ebieyeQueryRepr_;
    else
      return "filter not compiled.";
  }

  public Object clone() {
    EbiQueryExpressionBuilder filter = new EbiQueryExpressionBuilder(filterModel_);
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
