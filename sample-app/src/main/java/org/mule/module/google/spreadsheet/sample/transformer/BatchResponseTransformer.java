package org.mule.module.google.spreadsheet.sample.transformer;

import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class BatchResponseTransformer extends AbstractMessageTransformer {

	@Override
	@SuppressWarnings("unchecked")
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		Collection<CellEntry> entries = (Collection<CellEntry>) message.getPayload();
		
		// Print any errors that may have happened.
        boolean isSuccess = true;
        StringBuilder builder = new StringBuilder();
        for (CellEntry entry : entries) {
          String batchId = BatchUtils.getBatchId(entry);
          if (!BatchUtils.isSuccess(entry)) {
            isSuccess = false;
            BatchStatus status = BatchUtils.getBatchStatus(entry);
            builder.append("\n" + batchId + " failed (" + status.getReason() + ") " + status.getContent());
          }
        }
        if (isSuccess) {
          builder.append("Batch operations successful.");
        }
        
        return builder.toString();
	}

}
