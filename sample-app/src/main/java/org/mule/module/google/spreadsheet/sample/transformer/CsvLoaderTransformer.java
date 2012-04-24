package org.mule.module.google.spreadsheet.sample.transformer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class CsvLoaderTransformer extends AbstractMessageTransformer {

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		InputStream in = null;
		
		try {
			in = this.getClass().getResourceAsStream("/delimited.csv");
			assert in != null : "Could not load csv";
			
			String csv = IOUtils.toString(in);
			
			return csv;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
		}
	}

}
