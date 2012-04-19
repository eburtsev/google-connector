package org.mule.module.google.spreadsheet.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;


/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public abstract class ModelParser {

	
	public static List<Spreadsheet> parseSpreadsheet(SpreadsheetFeed feed) {
		return parseSpreadsheet(feed.getEntries());
	}
	
	public static List<Spreadsheet> parseSpreadsheet(List<SpreadsheetEntry> entries) {
		int size = entries.size();
		
		if (size > 0) {
			List<Spreadsheet> result = new ArrayList<Spreadsheet>(size);
			
			for (SpreadsheetEntry entry : entries) {
				result.add(new Spreadsheet(entry));
			}
			
			return result;
		}
		
		return new ArrayList<Spreadsheet>();
	}
	
	public static List<Worksheet> parseWorksheet(WorksheetFeed feed) {
		return parseWorksheet(feed.getEntries());
	}
	
	public static List<Worksheet> parseWorksheet(List<WorksheetEntry> entries) {
		int size = entries.size();
		
		if (size > 0) {
			List<Worksheet> result = new ArrayList<Worksheet>(size);
			
			for (WorksheetEntry entry : entries) {
				Worksheet ws = new Worksheet(entry);
				result.add(ws);
			}
			
			return result;
		}
		
		return new ArrayList<Worksheet>();
	}
	
	public static List<Cell> parseCell(CellFeed feed) {
		return parseCell(feed.getEntries());
	}
	
	public static List<Cell> parseCell(List<CellEntry> entries) {
		int size = entries.size();
		
		if (size > 0) {
			List<Cell> result = new ArrayList<Cell>(size);

			for (CellEntry entry : entries) {
				result.add(parseCell(entry));
			}
			
			return result;
		}
		
		return new ArrayList<Cell>();
	}
	
	public static Cell parseCell(CellEntry entry) {
		Cell myCell = new Cell();
		com.google.gdata.data.spreadsheet.Cell googleCell = entry.getCell();
		myCell.setColumnNumber(googleCell.getCol());
		myCell.setRowNumber(googleCell.getRow());
		myCell.setValueOrFormula(googleCell.getInputValue());
		
		return myCell;
	}
	
	public static List<Row> parseRows(CellFeed feed) {
		return parseRows(feed.getEntries());
	}
	
	public static List<Row> parseRows(List<CellEntry> entries) {
		List<Row> retVal = new ArrayList<Row>();
		
		if (entries.isEmpty()) {
			return retVal;
		}
		
		Map<Integer, Row> rows = new HashMap<Integer, Row>();
		
		for (CellEntry entry : entries) {
			com.google.gdata.data.spreadsheet.Cell cell = entry.getCell();
			int rowNumber = cell.getRow();
			
			Row row = rows.get(rowNumber);
			if (row == null) {
				row = new Row();
				row.setRowNumber(rowNumber);
				rows.put(rowNumber, row);
			}
			
			Cell myCell = parseCell(entry);
			row.addCell(myCell, myCell.getColumnNumber() -1);
		}
		
		Collections.sort(retVal);
		return retVal;
		
	}
	
}
