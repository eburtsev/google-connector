/**
 * Mule Development Kit
 * Copyright 2010-2011 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This file was automatically generated by the Mule Development Kit
 */
package org.mule.module.google.spreadsheet;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.RequestContext;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;
import org.mule.module.google.spreadsheet.adapter.BatchUpdateCSVAdapter;
import org.mule.module.google.spreadsheet.adapter.BatchUpdateRowAdapter;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.*;
import org.mule.module.google.spreadsheet.model.Worksheet;

/**
 * Generic module
 *
 * @author MuleSoft, Inc.
 */
@Module(name = "googlespreadsheet", schemaVersion = "1.0")
public class GoogleSpreadSheetModule {

	private static Logger logger = Logger.getLogger(GoogleSpreadSheetModule.class);
	private static final String BATCH_REQUEST = "BATCH_REQUEST";
	private static final String CELL_FEED_URL = "CELL_FEED_URL";

	/**
	 * The OAuth consumer key
	 */
	@Configurable
	private String username;

	/**
	 * The OAuth consumer secret
	 */
	@Configurable
	private String password;

	@Configurable
	@Optional
	@Default("https://docs.google.com/feeds/private/full https://spreadsheets.google.com/feeds http://spreadsheets.google.com/feeds")
	private String scope;

	private FeedURLFactory factory = FeedURLFactory.getDefault();
	@Configurable
	@Optional
	@Default("Mule-GoogleDocsConnector/1.0")
	private String applicationName;
	private SpreadsheetService ssService = null;
	private DocsService docService = null;

	/**
	 *
	 * Retrieves the spreadsheets that the authenticated user has access to.
	 *
	 * {@sample.xml ../../../doc/GoogleDocs-connector.xml.sample
	 * googledocs:listDocuments}
	 *
	 * @param title document title
	 * @param email user email
	 * @return list of documents
	 * @throws OAuthException
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Processor
	public void share(String title, String email, @Optional @Default("reader") String role) throws OAuthException, IOException, ServiceException {
		if (StringUtils.isEmpty(title) || StringUtils.isEmpty(email))
			return;
		DocumentListFeed feed = this.getDocsService().getFeed(new URL("https://docs.google.com/feeds/default/private/full"), DocumentListFeed.class);
		for (DocumentListEntry e : feed.getEntries()) {
			if (title.equalsIgnoreCase(e.getTitle().getPlainText())) {
				AclEntry acl = new AclEntry();
				acl.setScope(new AclScope(AclScope.Type.USER, email));
				acl.setRole(new AclRole(role));
				getDocsService().insert(new URL(e.getAclFeedLink().getHref()), acl);
			}
		}
	}

	/**
	 *
	 * Retrieves the spreadsheets that the authenticated user has access to.
	 *
     * {@sample.xml ../../../doc/GoogleDocs-connector.xml.sample googledocs:listDocuments}
	 *
	 * @param accessToken
	 * @return list of documents
	 * @throws OAuthException
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Processor
	public List<Spreadsheet> getAllSpreadsheets() throws OAuthException, IOException, ServiceException {
		return ModelParser.parseSpreadsheet(this.getSsService().getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class));
	}

	/**
	 *
	 * @param accessToken
	 * @param secretToken
	 * @param spreadsheet
	 * @throws OAuthException
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Processor
	public void createSpreadsheet(String title) throws OAuthException, IOException, ServiceException {

		com.google.gdata.data.docs.SpreadsheetEntry newEntry = new com.google.gdata.data.docs.SpreadsheetEntry();

		newEntry.setTitle(new PlainTextConstruct(title));
		this.getDocsService().insert(new URL("https://docs.google.com/feeds/default/private/full"), newEntry);
	}

	/**
	 * Lists all the worksheets in the loaded spreadsheet.
	 *
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public List<Worksheet> getAllWorksheets(String spreadsheet,
			@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {
		SpreadsheetEntry ss = this.getSpreadsheetEntry(spreadsheet, spreadsheetIndex);
		return ModelParser.parseWorksheet(ss.getWorksheets());
	}

	/**
	 * Creates a new worksheet in the loaded spreadsheets, using the title and
	 * sizes given.
	 *
	 * @param title a String containing a name for the new worksheet.
	 * @param rowCount the number of rows the new worksheet should have.
	 * @param colCount the number of columns the new worksheet should have.
	 *
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public Worksheet createWorksheet(
			String spreadsheet,
			@Optional @Default("0") int spreadsheetIndex,
			String title,
			int rowCount,
			int colCount) throws IOException, ServiceException {

		SpreadsheetEntry ss = this.getSpreadsheetEntry(spreadsheet, spreadsheetIndex);
		WorksheetEntry ws = new WorksheetEntry();
		ws.setTitle(new PlainTextConstruct(title));
		ws.setRowCount(rowCount);
		ws.setColCount(colCount);
		ws = this.getSsService().insert(ss.getWorksheetFeedUrl(), ws);

		return new Worksheet(ws);
	}

	/**
	 *
	 *
	 * @param oldTitle a String specifying the worksheet to update.
	 * @param newTitle a String containing the new name for the worksheet.
	 * @param rowCount the number of rows the new worksheet should have.
	 * @param colCount the number of columns the new worksheet should have.
	 *
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public void updateWorksheetMetadata(
			String spreadsheet,
			String worksheet,
			@Optional @Default("") String title,
			@Optional Boolean draft,
			@Optional Boolean canEdit,
			@Optional @Default("") String summary,
			@Optional @Default("0") int rowCount,
			@Optional @Default("0") int colCount,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {

		WorksheetEntry ws = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);

		if (!StringUtils.isEmpty(title)) {
			ws.setTitle(new PlainTextConstruct(title));
		}

		if (draft != null) {
			ws.setDraft(draft);
		}

		if (rowCount > 0) {
			ws.setRowCount(rowCount);
		}

		if (colCount > 0) {
			ws.setColCount(colCount);

		}

		if (canEdit) {
			ws.setCanEdit(canEdit);
		}

		if (!StringUtils.isEmpty(summary)) {
			ws.setSummary(new PlainTextConstruct(summary));
		}

		ws.update();
	}

	/**
     * Deletes the worksheet specified by the title parameter. Note that worksheet
     * titles are not unique, so this method just updates the first worksheet it
     * finds.
	 *
	 * @param title a String containing the name of the worksheet to delete.
	 *
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public void deleteWorksheet(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {

		this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).delete();
	}

	/**
	 *
	 * @param accessToken
	 * @param secretToken
	 * @param worksheet
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Processor
	public void setRowValues(
			@Payload List<Row> rows,
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
			@Optional @Default("false") boolean purge) throws Exception {

		if (rows == null || rows.isEmpty()) {
			logger.warn("Worksheet contains no rows... skipping update and possible purge");
			return;
		}

		List<NestedProcessor> processors = new ArrayList<NestedProcessor>(1);
		processors.add(new BatchUpdateRowAdapter(this));

		if (purge) {
			this.purgeWorksheet(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
		}

		this.batchSetCellValue(spreadsheet, worksheet, rows, spreadsheetIndex, worksheetIndex, processors);
	}

	@Processor
	public void setCsvValues(
			String spreadsheet,
			String worksheet,
			@Optional @Default("#[payload:]") String csv,
			@Optional @Default("1") int startingRow,
			@Optional @Default("1") int startingColumn,
			@Optional @Default("\n") String lineSeparator,
			@Optional @Default(",") String columnSeparator,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
			@Optional @Default("false") boolean purge) throws Exception {

		if (StringUtils.isEmpty(csv)) {
			if (logger.isDebugEnabled()) {
				logger.debug("received empty csv value... exiting without updating values nor purging");
			}
			return;
		}

		List<NestedProcessor> processors = new ArrayList<NestedProcessor>(1);
		processors.add(new BatchUpdateCSVAdapter(this, startingRow, startingColumn, columnSeparator, lineSeparator));

		if (purge) {
			this.purgeWorksheet(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
		}

		this.batchSetCellValue(spreadsheet, worksheet, csv, spreadsheetIndex, worksheetIndex, processors);
	}

	@Processor
	public List<Person> getAuthors(
			String spreadsheet,
			@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {

		return this.getSpreadsheetEntry(spreadsheet, spreadsheetIndex).getAuthors();
	}

	/**
     * Retrieves the columns headers from the cell feed of the worksheet
     * entry.
	 *
	 * @param worksheet worksheet entry containing the cell feed in question
	 * @return a list of column headers
	 * @throws Exception if error in retrieving the spreadsheet information
	 */
	@Processor
	public List<Cell> getColumnHeaders(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {


		WorksheetEntry worksheetEntry = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);

		// Get the appropriate URL for a cell feed
		URL cellFeedUrl = worksheetEntry.getCellFeedUrl();

		// Create a query for the top row of cells only (1-based)
		CellQuery cellQuery = new CellQuery(cellFeedUrl);
		cellQuery.setMaximumRow(1);

		// Get the cell feed matching the query
		CellFeed topRowCellFeed = this.getSsService().query(cellQuery, CellFeed.class);

		return ModelParser.parseCell(topRowCellFeed);
	}

	/**
	 * Gets the SpreadsheetEntry for the first spreadsheet with that name
	 * retrieved in the feed.
	 *
	 * @param spreadsheet the name of the spreadsheet
	 * @return the first SpreadsheetEntry in the returned feed, so latest
	 * spreadsheet with the specified name
     * @throws Exception if error is encountered, such as no spreadsheets with the
     * name
	 */
	@Processor
	public List<Spreadsheet> getSpreadsheetsByTitle(
			String title) throws IOException, ServiceException {

		SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
		spreadsheetQuery.setTitleQuery(title);
		return ModelParser.parseSpreadsheet(this.getSsService().query(spreadsheetQuery, SpreadsheetFeed.class));
	}

	/**
	 * Get the WorksheetEntry for the worksheet in the spreadsheet with the
	 * specified name.
	 *
	 * @param spreadsheet the name of the spreadsheet
	 * @param worksheet the name of the worksheet in the spreadsheet
	 * @return worksheet with the specified name in the spreadsheet with the
	 * specified name
     * @throws Exception if error is encountered, such as no spreadsheets with the
     * name, or no worksheet wiht the name in the spreadsheet
	 */
	@Processor
	public List<Worksheet> getWorksheetByTitle(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {

		return ModelParser.parseWorksheet(this.getWorksheetEntriesByTitle(spreadsheet, worksheet, spreadsheetIndex));
	}

	/**
	 * Clears all the cell entries in the worksheet.
	 *
	 * @param spreadsheet the name of the spreadsheet
	 * @param worksheet the name of the worksheet
	 * @throws Exception if error is encountered, such as bad permissions
	 */
	@Processor
	public void purgeWorksheet(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {

		WorksheetEntry worksheetEntry = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);

		SpreadsheetService service = this.getSsService();
		CellFeed cellFeed = service.getFeed(worksheetEntry.getCellFeedUrl(), CellFeed.class);

		for (CellEntry cell : cellFeed.getEntries()) {
			cell.delete();
		}
	}

	/**
	 * Inserts or overwrites a cell in the worksheet.
	 *
	 * @param spreadsheet the name of the spreadsheet
	 * @param worksheet the name of the worksheet
	 * @param row the index of the row
	 * @param column the index of the column
	 * @param formulaOrValue the input string for the cell
	 * @throws Exception if error is encountered, such as bad permissions
	 */
	@Processor
	public void setCellValue(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
			int row,
			int column,
			String formulaOrValue) throws IOException, ServiceException {

		URL cellFeedUrl = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();

		CellEntry newEntry = new CellEntry(row, column, formulaOrValue);
		this.getSsService().insert(cellFeedUrl, newEntry);
	}

	/**
	 *
	 *
	 * @param accessToken
	 * @param secretToken
	 * @param spreadsheet
	 * @param worksheet
	 * @param nestedProcessors
	 * @param spreadsheetIndex
	 * @param worksheetIndex
	 * @return
	 * @throws Exception
	 */
	@Processor
	public void batchSetCellValue(
			String spreadsheet,
			String worksheet,
			@Optional @Default("#[payload:]") Object payload,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
			List<NestedProcessor> nestedProcessors) throws Exception {

		URL cellFeedUrl = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();

		CellFeed batchRequest = new CellFeed();

		MuleMessage message = RequestContext.getEvent().getMessage();
		message.setInvocationProperty(BATCH_REQUEST, batchRequest);
		message.setInvocationProperty(CELL_FEED_URL, cellFeedUrl);

		for (NestedProcessor proc : nestedProcessors) {
			proc.process(payload);
		}

		// Get the batch feed URL and submit the batch requests
		CellFeed feed = this.getSsService().getFeed(cellFeedUrl, CellFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		this.getSsService().batch(batchUrl, batchRequest);
	}

	/**
	 *
	 * @param row
	 * @param col
	 * @param formulaOrValue
	 * @throws Exception
	 */
	@Processor
	public void cellValue(int row, int col, String formulaOrValue) throws Exception {

		MuleMessage message = RequestContext.getEvent().getMessage();

		CellFeed batchRequest = message.getInvocationProperty(BATCH_REQUEST);
		URL cellFeedUrl = message.getInvocationProperty(CELL_FEED_URL);

		String batchId = "R" + row + "C" + col;
		URL entryUrl = new URL(cellFeedUrl.toString() + "/" + batchId);
		CellEntry batchOperation = this.ssService.getEntry(entryUrl, CellEntry.class); //use the service directly to avoid this processor to work un-nested
		batchOperation.changeInputValueLocal(formulaOrValue);
		BatchUtils.setBatchId(batchOperation, batchId);
		BatchUtils.setBatchOperationType(batchOperation, BatchOperationType.UPDATE);

		batchRequest.getEntries().add(batchOperation);
	}

	/**
	 * Shows all cells that are in the spreadsheet.
	 *
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public List<Cell> getAllCells(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {

		WorksheetEntry worksheetEntry = this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);

		// Get the appropriate URL for a cell feed
		URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
		return ModelParser.parseCell(this.getSsService().getFeed(cellFeedUrl, CellFeed.class));
	}

	/**
	 * Shows a particular range of cells, limited by minimum/maximum rows and
	 * columns.
	 *
	 * @param minRow the minimum row, inclusive, 1-based
	 * @param maxRow the maximum row, inclusive, 1-based
	 * @param minCol the minimum column, inclusive, 1-based
	 * @param maxCol the maximum column, inclusive, 1-based
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public List<Row> getCellRange(
			String spreadsheet,
			String worksheet,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
			int minRow,
			int maxRow,
			int minCol,
			int maxCol) throws IOException, ServiceException {

		CellQuery query = new CellQuery(this.getCellFeedUrl(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex));
		query.setMinimumRow(minRow);
		query.setMaximumRow(maxRow);
		query.setMinimumCol(minCol);
		query.setMaximumCol(maxCol);

		return ModelParser.parseRows(this.getSsService().query(query, CellFeed.class));
	}

	/**
	 * Performs a full-text search on cells.
	 *
     * @param fullTextSearchString a full text search string, with space-separated
     *        keywords
	 * @throws ServiceException when the request causes an error in the Google
     *         Spreadsheets service.
	 * @throws IOException when an error occurs in communication with the Google
     *         Spreadsheets service.
	 */
	@Processor
	public List<Cell> search(
			String spreadsheet,
			String worksheet,
			String query,
			@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {

		CellQuery cellQuery = new CellQuery(this.getCellFeedUrl(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex));
		cellQuery.setFullTextQuery(query);

		return ModelParser.parseCell(this.getSsService().query(cellQuery, CellFeed.class));
	}

	private SpreadsheetEntry getSpreadsheetEntry(
			String spreadsheet,
			int spreadsheetIndex) throws IOException, ServiceException {

		SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
		spreadsheetQuery.setTitleQuery(spreadsheet);
		return this.getItem(this.getSsService().query(spreadsheetQuery, SpreadsheetFeed.class).getEntries(), spreadsheetIndex);
	}

	/**
	 * Utility method to get the low level atom representation of worksheets
	 * mathing a given title
	 *
	 * @param accessToken
	 * @param secretToken
	 * @param spreadsheet
	 * @param worksheet
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	private List<WorksheetEntry> getWorksheetEntriesByTitle(
			String spreadsheet,
			String worksheet,
			int spreadsheetIndex) throws IOException, ServiceException {

		SpreadsheetEntry spreadsheetEntry = this.getSpreadsheetEntry(spreadsheet, spreadsheetIndex);

		WorksheetQuery worksheetQuery = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
		worksheetQuery.setTitleQuery(worksheet);
		return this.getSsService().query(worksheetQuery, WorksheetFeed.class).getEntries();
	}

	private WorksheetEntry getWorksheetEntry(
			String spreadsheet,
			String worksheet,
			int spreadsheetIndex,
			int worksheetIndex) throws IOException, ServiceException {

		List<WorksheetEntry> worksheets = this.getWorksheetEntriesByTitle(spreadsheet, worksheet, spreadsheetIndex);
		return this.getItem(worksheets, worksheetIndex);
	}

	private URL getCellFeedUrl(
			String spreadsheet,
			String worksheet,
			int spreadsheetIndex,
			int worksheetIndex) throws IOException, ServiceException {

		return this.getWorksheetEntry(spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();
	}

	private <T> T getItem(List<T> list, int index) {
		if (list.isEmpty()) {
			throw new IllegalArgumentException("No item found for that name");
		} else if (index >= list.size()) {
			throw new IllegalArgumentException("You requested item index " + index
					+ " but only " + list.size() + " were found");
		}

		return list.get(index);
	}

	private SpreadsheetService getSsService() throws ServiceException {
		if (this.ssService == null) {
			this.ssService = this.connect(new SpreadsheetService("Ss-" + this.applicationName));

			// workaround for issue described in http://code.google.com/p/gdata-java-client/issues/detail?id=103
			// this.ssService.setHeader("If-Match", "*");
		}

		return this.ssService;
	}

	private DocsService getDocsService() throws ServiceException {
		if (this.docService == null) {
			this.docService = this.connect(new DocsService("Docs-" + this.applicationName));
		}
		return this.docService;
	}

	private <T extends GoogleService> T connect(T service) throws ServiceException {
		try {
			service.setUserCredentials(this.username, this.password);
		} catch (Exception e) {
			throw new ServiceException(e);
		}

		return service;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
