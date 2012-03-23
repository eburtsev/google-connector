package org.mule.module.google.spreadsheet.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Row {

	/**
	 * Defaults to zero
	 */
	private int rowNumber = 0;
	private List<Cell> cells = new ArrayList<Cell>();

	public List<Cell> getCells() {
		return cells;
	}

	public void setCells(List<Cell> cells) {
		this.cells = cells;
	}
	
	public void addCell(Cell cell){
		this.cells.add(cell);
	}
	
	public void addCell(Cell cell, int index) {
		this.cells.add(index, cell);
	}
	
	public void removeCell(Cell cell) {
		this.cells.remove(cell);
	}
	
	public void removeCell(int index) {
		this.cells.remove(index);
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	
}
