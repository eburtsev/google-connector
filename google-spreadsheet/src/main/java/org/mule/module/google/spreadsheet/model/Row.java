package org.mule.module.google.spreadsheet.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Row implements Comparable<Row>{

	/**
	 * Defaults to zero
	 */
	private int rowNumber = 0;
	
	private List<Cell> cells = new ArrayList<Cell>();
	
	public Row(){}
	
	@Override
	public int compareTo(Row o) {
		return new Integer(this.rowNumber).compareTo(o.getRowNumber());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Row) {
			Row o = (Row) obj;
			return this.rowNumber == o.rowNumber;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.rowNumber;
	}
	
	public void sortCells() {
		Collections.sort(this.cells);
	}

	public List<Cell> getCells() {
		return cells;
	}

	public void setCells(List<Cell> cells) {
		this.cells = cells;
		for (Cell cell : cells) {
			cell.setRowNumber(this.rowNumber);
		}
	}
	
	public void addCell(Cell cell){
		cell.setRowNumber(this.rowNumber);
		this.cells.add(cell);
	}
	
	public void addCell(Cell cell, int index) {
		cell.setRowNumber(this.rowNumber);
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
