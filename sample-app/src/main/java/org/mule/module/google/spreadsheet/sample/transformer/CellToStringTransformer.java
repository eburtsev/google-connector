package org.mule.module.google.spreadsheet.sample.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.transformer.AbstractMessageTransformer;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class CellToStringTransformer extends AbstractMessageTransformer {

	@Override
	@SuppressWarnings("unchecked")
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		Object payload = message.getPayload();
		
		Collection<Cell> cells = null;
		
		if (payload instanceof Cell) {
			cells = new ArrayList<Cell>(1);
			cells.add((Cell) payload);
		} else {
			cells = (Collection<Cell>) payload;
		}
		
		StringBuilder builder = new StringBuilder();
		
		for (Cell cell : cells) {
			builder.append(String.format("R%dC%d:%S\n", cell.getRowNumber(), cell.getColumnNumber(), cell.getValueOrFormula()));
		}
		
		return builder.toString();
	}

}
