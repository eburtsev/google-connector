package org.mule.module.google.spreadsheet.model;

import java.util.Date;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.DateTime;

/**
 * Base class for types 
 * They expose the most used information fullfilling the bean stereotype
 * (argumentless constructor plus getters and setters for properties).
 * 
 * It does so by wrapping an instance of google's very own
 * {@link com.google.gdata.data.BaseEntry}. If the basic functionalities exposed
 * by this type or any of its implementors, you can always get the wrapped object
 * by invoking org.mule.module.google.spreadsheet.domain.Entry.delegate()
 * 
 * @author mariano.gonzalez@mulesoft.com
 */
public abstract class Entry<T extends BaseEntry<?>> {

	private T delegate;
	
	public Entry(T delegate) {
		this.delegate = delegate;
	}
	
	public String getId() {
		return delegate.getId();
	}

	public Date getPublished() {
		return new Date(delegate.getPublished().getValue());
	}

	public Date getUpdated() {
		return new Date(delegate.getUpdated().getValue());
	}

	public String getVersionId() {
		return delegate.getVersionId();
	}

	public boolean isDraft() {
		return delegate.isDraft();
	}
	
	public void setDraft(Boolean v) {
		delegate.setDraft(v);
	}

	public void setId(String v) {
		delegate.setId(v);
	}

	public void setPublished(Date v) {
		delegate.setPublished(new DateTime(v));
	}

	public void setVersionId(String v) {
		delegate.setVersionId(v);
	}

	public T delegate() {
		return delegate;
	}

}
