package org.mule.module.google.spreadsheet.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Spreadsheet extends Entry<SpreadsheetEntry> {
	
	private List<Worksheet> worksheets = new ArrayList<Worksheet>();
	
	public Spreadsheet() {
		this(new SpreadsheetEntry());
	}
	
	public Spreadsheet(SpreadsheetEntry delegate) {
		super(delegate);
		
		try {
			List<WorksheetEntry> worksheets = delegate.getWorksheets();
		
			if (worksheets != null) {
				for (WorksheetEntry ws : worksheets) {
					this.worksheets.add(new Worksheet(ws));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not create worksheets", e);
		}
	}

	public List<Worksheet> getWorksheets() {
		return worksheets;
	}

	public void setWorksheets(List<Worksheet> worksheets) {
		this.worksheets = worksheets;
	}
	
	public void addWorksheet(Worksheet ws) {
		this.worksheets.add(ws);
	}
	
	public void addWorksheet(Worksheet ws, int index) {
		this.worksheets.add(index, ws);
	}
	
	public void removeWorksheet(Worksheet ws) {
		this.worksheets.remove(ws);
	}
	
	public void removeWorksheet(int index) {
		this.worksheets.remove(index);
	}
	
	public String getTitle() {
		return this.delegate().getTitle().getPlainText(); 
	}
	
	public void setTitle(String v) {
		this.delegate().setTitle(TextConstruct.plainText(v));
	}

}
