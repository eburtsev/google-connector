package org.mule.module.google.spreadsheet.model;

import java.util.ArrayList;
import java.util.List;

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
	
	public static List<Worksheet> parseWorksheet(WorksheetFeed feed, Spreadsheet spreadsheet) {
		return parseWorksheet(feed.getEntries(), spreadsheet);
	}
	
	public static List<Worksheet> parseWorksheet(List<WorksheetEntry> entries, Spreadsheet spreadsheet) {
		int size = entries.size();
		
		if (size > 0) {
			List<Worksheet> result = new ArrayList<Worksheet>(size);
			
			for (WorksheetEntry entry : entries) {
				Worksheet ws = new Worksheet(entry, spreadsheet);
				ws.setSpreadsheet(spreadsheet);
				result.add(ws);
			}
			
			return result;
		}
		
		return new ArrayList<Worksheet>();
	}
	
	public static List<Cell> parseCell(CellFeed feed, Worksheet worksheet) {
		return parseCell(feed.getEntries(), worksheet);
	}
	
	public static List<Cell> parseCell(List<CellEntry> entries, Worksheet worksheet) {
		int size = entries.size();
		
		if (size > 0) {
			List<Cell> result = new ArrayList<Cell>(size);

			for (CellEntry entry : entries) {
				result.add(new Cell(entry));
			}
			
			return result;
		}
		
		return new ArrayList<Cell>();
	}
	
}
