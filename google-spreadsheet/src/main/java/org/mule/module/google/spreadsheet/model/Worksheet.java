package org.mule.module.google.spreadsheet.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class Worksheet extends Entry<WorksheetEntry>{

	private Spreadsheet spreadsheet;
	private int index = 0;
	private List<Row> rows = new ArrayList<Row>();
	
	public Worksheet() {
		this(new WorksheetEntry(), null);
	}
	
	public Worksheet(WorksheetEntry delegate, Spreadsheet spreadsheet) {
		super(delegate);
		this.setSpreadsheet(spreadsheet);
	}
	
	public void addRow(Row row) {
		row.setWorksheet(this);
		this.rows.add(row);
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
	
	public Date getUpdated() {
		return new Date(this.delegate().getUpdated().getValue());
	}
	
	public void setUpdated(Date v) {
		this.delegate().setUpdated(new DateTime(v));
	}

	public String getTitle() {
		return this.delegate().getTitle().getPlainText();
	}

	public void setTitle(String title) {
		this.delegate().setTitle(new PlainTextConstruct(title));
	}

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void setSpreadsheet(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
		for (Row row : rows) {
			row.setWorksheet(this);
		}
	}
	
	
	
}
