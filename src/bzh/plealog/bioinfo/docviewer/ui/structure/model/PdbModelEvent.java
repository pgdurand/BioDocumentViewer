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

import java.util.EventObject;

/**
 * Setup an in-house PDB model event suitable for this viewer.
 * 
 * @author Patrick G. Durand
 * @since 2009
 */
public class PdbModelEvent extends EventObject{
  private static final long serialVersionUID = -5495737420356250154L;
  public static final int MODEL_CLEARED = 0;
	public static final int CHAIN_ADDED   = 1;
	
	private int           _eventType;
	private PdbSequence _pdbSeq;
	
	public PdbModelEvent(PdbModel src){
		super(src);
	}
	public PdbModelEvent(PdbModel src, int type) {
		this(src);
		_eventType = type;
	}
	public PdbModelEvent(PdbModel src, int type, PdbSequence pdbSeq) {
		this(src, type);
		setPdbSequence(pdbSeq);
	}
	public int getEventType(){
		return _eventType;
	}
	public void setPdbSequence(PdbSequence pdbSeq){
		_pdbSeq = pdbSeq;
	}
	public PdbSequence getPdbSequence(){
		return _pdbSeq;
	}
}
