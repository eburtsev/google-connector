package org.mule.module.google.spreadsheet.sample.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class EntryToStringTransformer extends AbstractMessageTransformer {

	@Override
	@SuppressWarnings("unchecked")
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		Object payload = message.getPayload();
		
		Collection<BaseEntry<?>> entries = null;
		
		if (payload instanceof BaseEntry) {
			entries = new ArrayList<BaseEntry<?>>(1);
			entries.add((SpreadsheetEntry) payload);
		} else {
			entries = (Collection<BaseEntry<?>>) payload;
		}
		
		StringBuilder builder = new StringBuilder();
		
		for (BaseEntry<?> entry : entries) {
			builder.append(entry.getTitle().getPlainText()).append("\n");
		}
		
		return builder.toString();
	}

}
