/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.beamlineexplorer.rcp.wizards;

public class VisitDetails {
				
	String fedid;
	String visit_id;
	String instrument;
	String start_date;
	
	public VisitDetails(String fedid, String visit_id, String instrument, String start_date){
		this.fedid = fedid;
		this.visit_id = visit_id; 
		this.instrument = instrument;
		this.start_date = start_date;
	}
	
	public String getFedid() {
		return fedid;
	}

	public void setFedid(String fedid) {
		this.fedid = fedid;
	}

	public String getVisit_id() {
		return visit_id;
	}

	public void setVisit_id(String visit_id) {
		this.visit_id = visit_id;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	
}
