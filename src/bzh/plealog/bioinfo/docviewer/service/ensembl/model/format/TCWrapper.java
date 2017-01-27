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
package bzh.plealog.bioinfo.docviewer.service.ensembl.model.format;

import bzh.plealog.bioinfo.docviewer.service.ensembl.model.data.TranscriptConsequences;

/**
 * This is a data wrapper class used for the purpose of data formatting. It
 * avoids exhibiting XML/Java serialization objects and protect underlying
 * formatting engine against null or undefined values.
 * 
 * @author Patrick G. Durand
 */
public class TCWrapper {

  private TranscriptConsequences _tc;
  
  @SuppressWarnings("unused")
  private TCWrapper(){}
  
  public TCWrapper(TranscriptConsequences tc){
    _tc = tc;
  }
  
  public String getGeneID(){
    return _tc.getGene_id();
  }
  
  public String getGeneName(){
    return _tc.getGene_symbol();
  }
  
  public String getTransriptID(){
    return _tc.getTranscript_id();
  }
  
  public String getStrand(){
    return _tc.getStrand() < 0 ? "-" : "+";
  }
  
  public String getConsequence(){
    return _tc.getConsequenceTermsStr();
  }
  
  public String getCodons(){
    return _tc.getCodons()==null?DIWrapper.UNKNWON:_tc.getCodons();
  }
  
  public String getAminoAcid(){
    return _tc.getAmino_acids()==null?DIWrapper.UNKNWON:_tc.getAmino_acids();
  }
  
  public String getSIFTscoreStr(){
    return _tc.getSift_score()<0?DIWrapper.UNKNWON:String.valueOf(_tc.getSift_score());
  }

  public String getSIFTprediction(){
    return _tc.getSift_prediction()==null?DIWrapper.UNKNWON:_tc.getSift_prediction();
  }
  
  public String getPolyphenScoreStr(){
    return _tc.getPolyphen_score()<0?DIWrapper.UNKNWON:String.valueOf(_tc.getPolyphen_score());
  }
  
  public String getPolyphenPrediction(){
    return _tc.getPolyphen_prediction()==null?DIWrapper.UNKNWON:_tc.getPolyphen_prediction();
  }
}
