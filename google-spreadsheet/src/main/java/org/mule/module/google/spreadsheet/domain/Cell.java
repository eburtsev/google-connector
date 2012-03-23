package org.mule.module.google.spreadsheet.domain;

import com.google.gdata.data.spreadsheet.CellEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Cell extends Entry<CellEntry>{

	public Cell() {
		this(new CellEntry());
	}
	
	public Cell(CellEntry delegate) {
		super(delegate);
	}
	
	public int getColumnNumber() {
		return this.delegate().getCell().getCol();
	}
	
	public String getFormula() {
		return this.delegate().getCell().getInputValue();
	}
	
	public void setFormula(String value) {
		this.delegate().changeInputValueLocal(value);
	}
	
	public String getValue() {
		return this.delegate().getCell().getValue();
	}
	
	public void setValue(String value) {
		this.setFormula(value);
	}
	
}
