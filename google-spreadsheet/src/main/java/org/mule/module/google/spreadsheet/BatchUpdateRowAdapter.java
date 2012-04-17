package org.mule.module.google.spreadsheet;

import java.util.Map;

import org.mule.api.NestedProcessor;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.Row;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchUpdateRowAdapter implements NestedProcessor {

	private GoogleSpreadSheetModule module;
	
	public BatchUpdateRowAdapter(GoogleSpreadSheetModule module) {
		this.module = module;
	}
	
	@Override
	public Object process(Object payload) throws Exception {
		Row row = (Row) payload;
		
		int rowNumber = row.getRowNumber();
		for (Cell cell : row.getCells()) {
			this.module.cellValue(rowNumber, cell.getColumnNumber(), cell.getFormula());
		}
		
		return row;
	}
	
	@Override
	public Object process() throws Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object processWithExtraProperties(Map<String, Object> properties) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object process(Object payload, Map<String, Object> properties) throws Exception {
		throw new UnsupportedOperationException();
	}

}
