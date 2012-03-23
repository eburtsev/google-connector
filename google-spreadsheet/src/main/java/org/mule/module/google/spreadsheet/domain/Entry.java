package org.mule.module.google.spreadsheet.domain;

import java.util.Date;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.TextConstruct;

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
	
	public String getEtag() {
		return delegate.getEtag();
	}

	public String getId() {
		return delegate.getId();
	}

	public String getContent() {
		return delegate.getPlainTextContent();
	}

	public Date getPublished() {
		return new Date(delegate.getPublished().getValue());
	}

	public String getSummary() {
		return delegate.getSummary().getPlainText();
	}

	public String getTitle() {
		return delegate.getTitle().getPlainText();
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
	
	public Date getEdited() {
		return new Date(delegate.getEdited().getValue());
	}

	public void setContent(String content) {
		delegate.setContent(TextConstruct.plainText((content)));
	}

	public void setDraft(Boolean v) {
		delegate.setDraft(v);
	}

	public void setEdited(Date date) {
		delegate.setEdited(new DateTime(date));
	}

	public void setEtag(String v) {
		delegate.setEtag(v);
	}

	public void setId(String v) {
		delegate.setId(v);
	}

	public void setPublished(Date v) {
		delegate.setPublished(new DateTime(v));
	}

	public void setSummary(String v) {
		delegate.setSummary(TextConstruct.plainText(v));
	}

	public void setTitle(String v) {
		delegate.setTitle(TextConstruct.plainText(v));
	}

	public void setUpdated(Date v) {
		delegate.setUpdated(new DateTime(v));
	}

	public void setVersionId(String v) {
		delegate.setVersionId(v);
	}

	public T delegate() {
		return delegate;
	}

}
