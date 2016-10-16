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
package bzh.plealog.bioinfo.docviewer.ui.structure.jmol;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import bzh.plealog.bioinfo.api.data.feature.Feature;
import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.api.data.sequence.DRulerModel;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceException;
import bzh.plealog.bioinfo.api.data.sequence.DViewerSystem;
import bzh.plealog.bioinfo.docviewer.ui.structure.model.PdbSequence;

/**
 * Amino acide transcoder from JMol representation.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class JMolTranscoder {
	private static final String MAIN_CHAIN = "main";
	private static final Hashtable<String, String> transcoder = new Hashtable<>();

	static {
		//values from JMol: org.jmol.viewer.JmolConstants: 
		//predefinedGroup3Names and predefinedGroup1Names
		transcoder.put("ALA", "A");
		transcoder.put("ARG", "R");
		transcoder.put("ASN", "N");
		transcoder.put("ASP", "D");
		transcoder.put("CYS", "C");
		transcoder.put("GLN", "Q");
		transcoder.put("GLU", "E");
		transcoder.put("GLY", "G");
		transcoder.put("HIS", "H");
		transcoder.put("ILE", "I");
		transcoder.put("LEU", "L");
		transcoder.put("LYS", "K");
		transcoder.put("MET", "M");
		transcoder.put("PHE", "F");
		transcoder.put("PRO", "P");
		transcoder.put("SER", "S");
		transcoder.put("THR", "T");
		transcoder.put("TRP", "W");
		transcoder.put("TYR", "Y");
		transcoder.put("VAL", "V");
		transcoder.put("ASX", "A");
		transcoder.put("GLX", "G");

		transcoder.put("G", "G"); 
		transcoder.put("C", "C");
		transcoder.put("A", "A");
		transcoder.put("T", "T");
		transcoder.put("U", "U");
		transcoder.put("I", "I");
	    
		transcoder.put("DG", "G");
		transcoder.put("DC", "C");
		transcoder.put("DA", "A");
		transcoder.put("DT", "T");
		transcoder.put("DU", "U");
		transcoder.put("DI", "I");
	    
		transcoder.put("+G", "G");
		transcoder.put("+C", "C");
		transcoder.put("+A", "A");
		transcoder.put("+T", "T");
		transcoder.put("+U", "U");
		transcoder.put("+I", "I");
	}

	private static String getOneLetCode(String threeLetCode){
		String val = (String) transcoder.get(threeLetCode);
		return (val!=null ? val:"X");
	}

	private static String getResidueCode(String resInfo){
		String val = "?";
		int    idx1, idx2;
		
		//[GLY]9:A.N # 50
		idx1 = resInfo.indexOf('[');
		idx2 = resInfo.indexOf(']');
		if (idx1>=0 && idx2>=0){
			val = resInfo.substring(idx1+1, idx2);
		}
		return val;
	}
	private static int getResidueCoord(String resInfo){
		String str;
		int    val = 1;
		int    idx1, idx2, idx3, idx4;
		
		//[GLY]9:A.N # 50
		//[GLY]9.N # 50    when no chain
		idx1 = resInfo.indexOf(']');
		idx2 = resInfo.indexOf(':');
		idx3 = resInfo.indexOf('.');
		if (idx1>=0 && idx3>=0){
			try {
				if (idx2>=0){
					str = resInfo.substring(idx1+1, idx2);
				}
				else{
					str = resInfo.substring(idx1+1, idx3);
				}
				//special code when aa number is followed by a letter
				// [ALA]222^B:A.CA 
				idx4 = str.indexOf('^');
				if (idx4!=-1){
					val = Integer.valueOf(str.substring(0, idx4)).intValue();
				}
				else{
					val = Integer.valueOf(str).intValue();
				}
			} catch (NumberFormatException e) {
			}
		}
		return val;
	}
	private static String getChainCode(String resInfo){
		String val = MAIN_CHAIN;
		int    idx2, idx3;
		
		//[GLY]9:A.N # 50
		//[GLY]9.N # 50    when no chain
		idx2 = resInfo.indexOf(':');
		idx3 = resInfo.indexOf('.');
		if (idx2>=0 && idx3>=0){
			val = resInfo.substring(idx2+1, idx3);
		}
		return val;
	}
	
	@SuppressWarnings("rawtypes")
  public static PdbSequence createSequenceFromChain(Vector residues, boolean proteic) 
		throws DSequenceException{
		PdbSequence chain;
		DSequence     dSeq;
		Hashtable     residue;
		StringBuffer  szBuf;
		String        res, str, chainCode = MAIN_CHAIN;
		int[]         coords;
		int           k, size;
		
		szBuf = new StringBuffer();
		size = residues.size();
		coords = new int[size];
		for(k=0;k<size;k++){
			residue = (Hashtable) residues.get(k);
			res = residue.get("atomInfo1").toString();
			str = getOneLetCode(getResidueCode(res));
			coords[k] = getResidueCoord(res);
			if (k==0){
				chainCode = getChainCode(res);
			}
			szBuf.append(str);
		}
		if (szBuf.length()!=0){
			if (proteic){
				dSeq = DViewerSystem.getSequenceFactory().getSequence(
						new StringReader(szBuf.toString()),
			            DViewerSystem.getIUPAC_Protein_Alphabet());
			}
			else{
				dSeq = DViewerSystem.getSequenceFactory().getSequence(
						new StringReader(szBuf.toString()),
			            DViewerSystem.getIUPAC_DNA_Alphabet());
			}
			dSeq.createRulerModel(coords);
			chain = new PdbSequence(dSeq);
			chain.setChainCode(chainCode);
		}
		else{
			chain = null;
		}
		return chain;
	}
	private static void addFeature(String type, FeatureTable fTable,PdbSequence seqChain){
		DSequence seq;
		Feature   feat;
		int       from, to;
		
		seq = seqChain.getSequence();
		from = seq.getRulerModel().getSeqPos(0);
		to = seq.getRulerModel().getSeqPos(seq.size()-1);
		
		feat = fTable.addFeature(type.equals("sheet")?"strand":type, from, to, Feature.PLUS_STRAND);
		feat.addQualifier("Location", from+".."+to);
		feat.addQualifier("sequence", seq.toString());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
  public static void createFeatureTableFromChain(PdbSequence mainChain, String type, FeatureTable ft, 
			Vector residues, boolean proteic){
		DRulerModel   rModel;
		PdbSequence chain;
		Hashtable     residue;
		String        res, chainCode;
		Vector        structure;
		int           k, size, coord, prevCoord=0;
		
		size = residues.size();
		structure = new Vector();
		k=0;
		chainCode = mainChain.getChainCode();
		rModel = mainChain.getSequence().getRulerModel();
		while(true){
			if (k==size){
				if (!structure.isEmpty()){
					chain = JMolTranscoder.createSequenceFromChain(structure, proteic);
					addFeature(type, ft, chain);
				}
				break;
			}
			residue = (Hashtable) residues.get(k);
			res = residue.get("atomInfo1").toString();
			if (!chainCode.equals(getChainCode(res))){
				k++;
				continue;
			}
			coord = rModel.getRulerPos(getResidueCoord(res));
			if (k!=0){
				if ((coord-prevCoord)!=1 && !structure.isEmpty()){
					chain = JMolTranscoder.createSequenceFromChain(structure, proteic);
					addFeature(type, ft, chain);
					structure.clear();
				}
			}
			structure.add(residue);
			prevCoord = coord;
			k++;
		}
	}
}
