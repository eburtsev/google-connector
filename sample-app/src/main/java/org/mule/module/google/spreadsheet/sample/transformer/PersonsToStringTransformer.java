package org.mule.module.google.spreadsheet.sample.transformer;

import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.gdata.data.Person;

/**
 * 
 * @author mariano.gonzalez@mulesoft.com
 *
 */
public class PersonsToStringTransformer extends AbstractMessageTransformer {

	@Override
	@SuppressWarnings("unchecked")
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		Collection<Person> entries = (Collection<Person>) message.getPayload();
		StringBuilder builder = new StringBuilder();
		
		for (Person entry : entries) {
			builder.append(entry.getName()).append(" - ").append(entry.getEmail()).append("\n");
		}
		
		return builder.toString();
	}


}
