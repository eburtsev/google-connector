package org.mule.module.google.spreadsheet.model;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class RowBuilder {

	private Row row = new Row();
	
	public RowBuilder setNumber(int number) {
		row.setRowNumber(number);
		return this;
	}
	
	public RowBuilder addCell(int columnNumber, String valueOrFormula) {
		Cell cell = new Cell();
		cell.setColumnNumber(columnNumber);
		cell.setValueOrFormula(valueOrFormula);
		row.addCell(cell);
		return this;
	}
	
	public Row build() {
		Row retVal = row;
		this.row = new Row();
		
		return retVal;
	}
}
