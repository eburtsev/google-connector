package org.mule.module.google.spreadsheet.adapter;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.mule.api.NestedProcessor;
import org.mule.module.google.spreadsheet.GoogleSpreadsheetModuleStub;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.Row;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchUpdateAdaptersTest extends TestCase {

	
	public void testCSVAdapter() throws Exception {
		final String lineSeparator = "\n";
		final String columnSeparator = ",";
		
		NestedProcessor adapter = new BatchUpdateCSVAdapter(new GoogleSpreadsheetModuleStub(), 1, 1, columnSeparator, lineSeparator);
		
		String[] lines = new String[3];
		lines[0] = "id,name,surname";
		lines[1] = "1,john,doe";
		lines[2] = "2,jane,smith";
		
		String payload = String.format("%s\n%s\n%s", lines[0], lines[1], lines[2]);
		
		@SuppressWarnings("unchecked")
		List<Row> rows = (List<Row>) adapter.process(payload);
		
		assertTrue("Was expecting 3 rows", rows.size() == 3);
		
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			assertEquals("row number was not as expected", row.getRowNumber(), i+1);
			
			List<Cell> cells = row.getCells();
			assertEquals("was expecting three cells", cells.size(), 3);
			
			String[] expectedCellValues = lines[i].split(columnSeparator);
			
			for (int j = 0; j < cells.size(); j++) {
				Cell cell = cells.get(j);
				assertEquals("Cell did not had the expected column number", cell.getColumnNumber(), j+1);
				assertEquals("Cell did not had the expected row number", cell.getRowNumber(), i+1);
				assertEquals("cell did not had the expected value", cell.getValueOrFormula(), expectedCellValues[j]);
			}
		}
	}
	
	public void testRowAdapter() throws Exception {
		List<Row> rows = new ArrayList<Row>();
		rows.add(new Row());
		
		NestedProcessor adapter = new BatchUpdateRowAdapter(new GoogleSpreadsheetModuleStub());
		
		@SuppressWarnings("unchecked")
		List<Row> result = (List<Row>) adapter.process(rows);
		
		assertSame("was expecting lists to be the same", result, rows);
		assertEquals("size is changed", result.size(), 1);
				
	}
	
}
