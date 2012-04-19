package org.mule.module.google.spreadsheet.model;


/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Cell implements Comparable<Cell> {

	private int rowNumber = 0;
	private int columnNumber = 0;
	private String valueOrFormula;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cell) {
			Cell o = (Cell) obj;
			return this.getColumnNumber() == o.getColumnNumber() && this.getRowNumber() == o.getRowNumber();
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getColumnNumber() * this.getRowNumber() * 11;
	}
	
	@Override
	public int compareTo(Cell o) {
		return new Integer(this.getColumnNumber()).compareTo(o.getColumnNumber());
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	public String getValueOrFormula() {
		return valueOrFormula;
	}

	public void setValueOrFormula(String valueOrFormula) {
		this.valueOrFormula = valueOrFormula;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	
}
