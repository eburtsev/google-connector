package org.mule.module.google.spreadsheet.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.module.google.spreadsheet.GoogleSpreadSheetModule;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.Row;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchUpdateCSVAdapter extends AbstractBatchUpdateAdapter {

	private static final Logger logger = Logger.getLogger(BatchUpdateCSVAdapter.class);
	
	private int startingRow;
	private int startingColumn;
	private String columnSeparator;
	private String lineSeparator;
	
	public BatchUpdateCSVAdapter(GoogleSpreadSheetModule module, int startingRow, int startingColumn, String columnSeparator, String lineSeparator) {
		super(module);
		this.startingColumn = startingColumn;
		this.startingRow = startingRow;
		this.columnSeparator = columnSeparator;
		this.lineSeparator = lineSeparator;
	}
	
	@Override
	protected List<Row> extractRows(Object payload) {
		
		if (!(payload instanceof String)) {
			throw new IllegalArgumentException("Was expecting Payload to be a String");
		}
		
		String input = (String) payload;
		
		if (StringUtils.isEmpty(input)) {
			throw new IllegalArgumentException("input is empty");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("extracting rows using lineSeparator: %s, columnSepartor:%s from payload:\n%s", lineSeparator, columnSeparator, payload));
		}
		
		String[] lines = input.split(this.lineSeparator);
		
		List<Row> rows = new ArrayList<Row>(lines.length);
		int lineNumber = this.startingRow;
		
		for (String line : lines) {
			Row row = new Row();
			row.setRowNumber(lineNumber);
			
			String[] columns = line.split(this.columnSeparator);
			int columnNumber = this.startingColumn;
			
			for (String column : columns) {
				Cell cell = new Cell();
				cell.setValueOrFormula(column);
				cell.setColumnNumber(columnNumber);
				cell.setRowNumber(lineNumber);
				columnNumber++;
				row.addCell(cell);
			}
			
			rows.add(row);
			lineNumber++;
		}
		
		return rows;
	}

}
