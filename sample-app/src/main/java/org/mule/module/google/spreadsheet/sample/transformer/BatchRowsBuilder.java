package org.mule.module.google.spreadsheet.sample.transformer;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.Row;
import org.mule.module.google.spreadsheet.model.RowBuilder;
import org.mule.transformer.AbstractMessageTransformer;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchRowsBuilder extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		List<Row> rows = new ArrayList<Row>();
		
		Row row = new Row();
		row.setRowNumber(1);
		Cell cell = new Cell();
		cell.setColumnNumber(1);
		cell.setValueOrFormula("batch 1");
		
		
		RowBuilder builder = new RowBuilder();
		
		rows.add(builder.setNumber(1)
				.addCell(1, "batch 1")
				.addCell(2, "batch 2")
				.build());
		
		rows.add(builder.setNumber(2)
				.addCell(1, "batch 3")
				.addCell(2, "batch 4")
				.build()
				);
		
		return rows;
	}

}
