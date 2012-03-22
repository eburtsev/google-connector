package org.mule.module.google.spreadsheet;

import java.util.Map;

import org.mule.api.NestedProcessor;

/**
 * Simple abstract implementation of {@org.mule.api.NestedProcessor}
 * that only requires the implementation of process()
 * 
 * @author mariano.gonzalez@mulesoft.com
 */
public class ImportDelimitedCellUpdate implements NestedProcessor {

	private int row;
	private int col;
	private String formulaOrValue;
	private GoogleSpreadSheetModule module;
	private Object payload;
	
	public ImportDelimitedCellUpdate(int row, int col, String formulaOrValue, GoogleSpreadSheetModule module, Object payload) {
		this.row = row;
		this.col = col;
		this.formulaOrValue = formulaOrValue;
		this.module = module;
		this.payload = payload;
	}

	@Override
	public Object process() throws Exception {
		this.module.cellValue(this.row, this.col, this.formulaOrValue);
		return this.payload;
	}
	
	@Override
	public Object processWithExtraProperties(Map<String, Object> properties) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object process(Object payload, Map<String, Object> properties) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object process(Object payload) throws Exception {
		throw new UnsupportedOperationException();
	}

}
