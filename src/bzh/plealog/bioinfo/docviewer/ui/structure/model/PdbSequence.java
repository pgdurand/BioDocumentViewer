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
package bzh.plealog.bioinfo.docviewer.ui.structure.model;

import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;

/**
 * Setup an in-house PDB sequence model event suitable for this viewer. Actually it simply wraps
 * standard DSequence and FeatureTable within a convenient POJO.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class PdbSequence {
	private DSequence    _sequence;
	private String       _chainCode;
	private FeatureTable _fTable;
	
	public PdbSequence(DSequence sequence) {
		super();
		_sequence = sequence;
	}
	public DSequence getSequence() {
		return _sequence;
	}
	public void setSequence(DSequence sequence) {
		_sequence = sequence;
	}
	public String getChainCode() {
		return _chainCode;
	}
	public void setChainCode(String chainCode) {
		_chainCode = chainCode;
	}
	
	public FeatureTable getFTable() {
		return _fTable;
	}
	public void setFTable(FeatureTable table) {
		_fTable = table;
	}
	public String toString(){
		return getChainCode();
	}
}
