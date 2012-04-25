package org.mule.module.google.spreadsheet.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
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
		
		InputStream in = this.getClass().getResourceAsStream("/delimited.csv");
		
		assertNotNull("could not get delimited file", in);
		
		String payload = IOUtils.toString(in);
		String[] lines = payload.split(lineSeparator);

		@SuppressWarnings("unchecked")
		List<Row> rows = (List<Row>) adapter.process(payload);
		
		assertTrue(String.format("Was expecting %d rows", lines.length), rows.size() == lines.length);
		
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			String[] expectedCellValues = lines[i].split(columnSeparator);
			
			assertEquals("row number was not as expected", row.getRowNumber(), i+1);
			List<Cell> cells = row.getCells();
			assertEquals(String.format("Was expecting %d cells", expectedCellValues.length), cells.size(), expectedCellValues.length);
			
			
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
	
	public void testSingleAdapter() throws Exception {
		final int row = 2;
		final int column = 5;
		final String formulaOrValue = "testing!";
		
		NestedProcessor adapter = new BatchUpdateSingleCellAdapter(new GoogleSpreadsheetModuleStub(), row, column);
		
		@SuppressWarnings("unchecked")
		List<Row> result = (List<Row>) adapter.process(formulaOrValue);
		
		assertTrue(result.size() == 1);
		
		Row r = result.get(0);
		assertEquals(r.getRowNumber(), row);
		assertTrue(r.getCells().size() == 1);
		
		Cell cell = r.getCells().get(0);
		assertEquals(cell.getRowNumber(), row);
		assertEquals(cell.getColumnNumber(), column);
		assertEquals(cell.getValueOrFormula(), formulaOrValue);
	}
}
