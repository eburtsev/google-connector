package org.mule.module.google.spreadsheet.domain;

import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Worksheet extends Entry<WorksheetEntry>{

	public Worksheet() {
		this(new WorksheetEntry());
	}
	
	public Worksheet(WorksheetEntry delegate) {
		super(delegate);
	}

	public int getRowCount() {
		return this.delegate().getRowCount();
	}

	public void setRowCount(int count) {
		this.delegate().setRowCount(count);
	}

	public int getColCount() {
		return this.delegate().getColCount();
	}

	public void setColCount(int count) {
		this.delegate().setColCount(count);
	}
	
	public String getName() {
		return this.delegate().getTitle().getPlainText(); 
	}
	
	public void setName(String v) {
		this.delegate().setTitle(TextConstruct.plainText(v));
	}
}
