package org.mule.module.google.spreadsheet.adapter;

import java.util.ArrayList;
import java.util.List;

import org.mule.module.google.spreadsheet.GoogleSpreadSheetModule;
import org.mule.module.google.spreadsheet.model.Row;
import org.mule.module.google.spreadsheet.model.RowBuilder;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchUpdateSingleCellAdapter extends AbstractBatchUpdateAdapter {

	private int row;
	private int column;
	
	
	public BatchUpdateSingleCellAdapter(GoogleSpreadSheetModule module, int row, int column) {
		super(module);
		this.row = row;
		this.column= column;
	}
	
	@Override
	protected List<Row> extractRows(Object payload) {
		if (!(payload instanceof String)) {
			throw new IllegalArgumentException("payload was expected to be a string with a formula or value");
		}
		
		String formulaOrValue = (String) payload;
		List<Row> rows = new ArrayList<Row>(1);
		Row row = new RowBuilder()
					.setNumber(this.row)
					.addCell(this.column, formulaOrValue)
					.build();
		rows.add(row);
		
		return rows;
	}

}
