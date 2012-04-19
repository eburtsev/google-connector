package org.mule.module.google.spreadsheet.adapter;

import java.util.List;

import org.mule.module.google.spreadsheet.GoogleSpreadSheetModule;
import org.mule.module.google.spreadsheet.model.Row;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchUpdateRowAdapter extends AbstractBatchUpdateAdapter {
	
	public BatchUpdateRowAdapter(GoogleSpreadSheetModule module) {
		super(module);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected List<Row> extractRows(Object payload) {
		return (List<Row>) payload;
	}

}
