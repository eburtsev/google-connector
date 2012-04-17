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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.RequestContext;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.oauth.OAuth;
import org.mule.api.annotations.oauth.OAuthAccessToken;
import org.mule.api.annotations.oauth.OAuthAccessTokenSecret;
import org.mule.api.annotations.oauth.OAuthConsumerKey;
import org.mule.api.annotations.oauth.OAuthConsumerSecret;
import org.mule.api.annotations.oauth.OAuthScope;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.module.google.spreadsheet.model.Cell;
import org.mule.module.google.spreadsheet.model.ModelParser;
import org.mule.module.google.spreadsheet.model.Row;
import org.mule.module.google.spreadsheet.model.Spreadsheet;
import org.mule.module.google.spreadsheet.model.Worksheet;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

/**
 * Generic module
 *
 * @author MuleSoft, Inc.
 */
@Module(name="googlespreadsheet", schemaVersion="1.0")
@OAuth(accessTokenUrl="https://www.google.com/accounts/OAuthGetAccessToken",
		authorizationUrl="https://www.google.com/accounts/OAuthAuthorizeToken",
		requestTokenUrl="https://www.google.com/accounts/OAuthGetRequestToken",
		callbackPath="oauth2callback")
public class GoogleSpreadSheetModule {
	
	private static Logger logger = Logger.getLogger(GoogleSpreadSheetModule.class);
	
	private static final String BATCH_REQUEST = "BATCH_REQUEST";
	private static final String CELL_FEED_URL = "CELL_FEED_URL";

	/**
     * The OAuth consumer key 
     */
    @Configurable
    @OAuthConsumerKey
    private String consumerKey;

    /**
     * The OAuth consumer secret 
     */
    @Configurable
    @OAuthConsumerSecret
    private String consumerSecret;
    
    @OAuthScope
    @Configurable
    @Optional
    @Default("https://docs.google.com/feeds/private/full https://spreadsheets.google.com/feeds http://spreadsheets.google.com/feeds")
    private String scope;
    
    private FeedURLFactory factory = FeedURLFactory.getDefault();
    
    @Configurable
    @Optional
    @Default("Mule-GoogleDocsConnector/1.0")
    private String applicationName;
    
    private SpreadsheetService ssService;
    
    private DocsService docService;
    
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
    public List<Spreadsheet> getAllSpreadsheets(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken) throws OAuthException, IOException, ServiceException {
    	
        return ModelParser.parseSpreadsheet(getSsService(accessToken, secretToken).getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class));
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
    public void createSpreadsheet(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken,
    		@Optional @Default("#[payload:]") Spreadsheet spreadsheet) throws OAuthException, IOException, ServiceException {

    	String title = spreadsheet.getTitle();
    	com.google.gdata.data.docs.SpreadsheetEntry newEntry = new com.google.gdata.data.docs.SpreadsheetEntry();
    	
    	newEntry.setTitle(new PlainTextConstruct(title));
        this.getDocsService(accessToken, secretToken).insert(new URL("https://docs.google.com/feeds/default/private/full"), newEntry);
        
        for (Worksheet ws : spreadsheet.getWorksheets()) {
        	this.createWorksheet(accessToken, secretToken, ws);
        }
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
    public List<Worksheet> getAllWorksheets(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Spreadsheet spreadsheet) throws IOException, ServiceException {
    	
    	SpreadsheetEntry ss = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet);
    	return ModelParser.parseWorksheet(getSsService(accessToken, secretToken).getFeed(ss.getWorksheetFeedUrl(), WorksheetFeed.class), spreadsheet);
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
    public void createWorksheet(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	SpreadsheetEntry ss = this.getSpreadsheetEntry(accessToken, secretToken, this.getSpreadsheet(worksheet));
    	WorksheetEntry ws = new WorksheetEntry();
    	ws.setTitle(new PlainTextConstruct(worksheet.getTitle()));
    	ws.setRowCount(worksheet.getRowCount());
    	ws.setColCount(worksheet.getColCount());
    	this.getSsService(accessToken, secretToken).insert(ss.getWorksheetFeedUrl(), ws);
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	WorksheetEntry ws = this.getWorksheetEntry(accessToken, secretToken, worksheet);
    	WorksheetEntry delegate = worksheet.delegate();
		
    	if (delegate.getTitle() != null) {
    		ws.setTitle(delegate.getTitle());
    	}
    	
		ws.setDraft(delegate.isDraft());
		ws.setRowCount(delegate.getRowCount());
		ws.setColCount(delegate.getColCount());
    	ws.setCanEdit(delegate.getCanEdit());
    	
    	if (delegate.getContent() != null) {
    		ws.setContent(delegate.getContent());
    	}
    	
    	delegate.setDraft(delegate.isDraft());
    	
    	if (delegate.getSummary() != null) {
    		ws.setSummary(delegate.getSummary());
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	this.getWorksheetEntry(accessToken, secretToken, worksheet).delete();
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
    public void updateWorksheetValues(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet,
			@Optional @Default("false") Boolean purge) throws Exception {
    	
    	List<Row> rows = worksheet.getRows();
    	if (rows == null || rows.isEmpty()) {
    		logger.warn("Worksheet contains no rows... skipping update and possible purge");
    		return;
    	}
    	
    	List<NestedProcessor> processors = new ArrayList<NestedProcessor>(1);
    	processors.add(new BatchUpdateRowAdapter(this));
    	
    	if (purge) {
    		this.purgeWorksheet(accessToken, secretToken, worksheet);
    	}
    	
    	this.batchSetCellValue(accessToken, secretToken, worksheet, processors);
    }
    
    
    @Processor
    public List<Person> getAuthors(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Spreadsheet spreadsheet) throws IOException, ServiceException {
    	
    	return this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet).getAuthors();
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
    public List<String> getColumnHeaders(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
		WorksheetEntry worksheetEntry = this.getWorksheetEntry(accessToken, secretToken, worksheet);
    	
      List<String> headers = new ArrayList<String>();

      // Get the appropriate URL for a cell feed
      URL cellFeedUrl = worksheetEntry.getCellFeedUrl();

      // Create a query for the top row of cells only (1-based)
      CellQuery cellQuery = new CellQuery(cellFeedUrl);
      cellQuery.setMaximumRow(1);

      // Get the cell feed matching the query
      CellFeed topRowCellFeed = this.getSsService(accessToken, secretToken).query(cellQuery, CellFeed.class);

      // Get the cell entries fromt he feed
      for (CellEntry entry : topRowCellFeed.getEntries()) {
        // Get the cell element from the entry
        com.google.gdata.data.spreadsheet.Cell cell = entry.getCell();
        headers.add(cell.getValue());
      }

      return headers;
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
    public List<Spreadsheet> getSpreadsheetByTitle(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken,
    		String title) throws IOException, ServiceException {
      
    	SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
        spreadsheetQuery.setTitleQuery(title);
        return ModelParser.parseSpreadsheet(this.getSsService(accessToken, secretToken).query(spreadsheetQuery, SpreadsheetFeed.class));
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
    							@OAuthAccessToken String accessToken,
    							@OAuthAccessTokenSecret String secretToken,
    							@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	Spreadsheet spreadsheet = this.getSpreadsheet(worksheet);
    	return ModelParser.parseWorksheet(this.getWorksheetEntriesByTitle(accessToken, secretToken, spreadsheet, worksheet), spreadsheet);
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	WorksheetEntry worksheetEntry = this.getWorksheetEntry(accessToken, secretToken, worksheet);
    	
    	CellFeed cellFeed = getSsService(accessToken, secretToken).getFeed(worksheetEntry.getCellFeedUrl(), CellFeed.class);

    	for (CellEntry cell : cellFeed.getEntries()) {
    		Link editLink = cell.getEditLink();
    		this.getSsService(accessToken, secretToken).delete(new URL(editLink.getHref()));
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet,
    		int row,
    		int column,
    		String formulaOrValue) throws IOException, ServiceException {
      
    	URL cellFeedUrl = this.getWorksheetEntry(accessToken, secretToken, worksheet).getCellFeedUrl();

    	CellEntry newEntry = new CellEntry(row, column, formulaOrValue);
    	this.getSsService(accessToken, secretToken).insert(cellFeedUrl, newEntry);
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet,
    		List<NestedProcessor> nestedProcessors) throws Exception {
    	
    	URL cellFeedUrl = this.getWorksheetEntry(accessToken, secretToken, worksheet).getCellFeedUrl();
    	
    	CellFeed batchRequest = new CellFeed();
    	
    	MuleMessage message = RequestContext.getEvent().getMessage();
    	message.setInvocationProperty(BATCH_REQUEST, batchRequest);
    	message.setInvocationProperty(CELL_FEED_URL, cellFeedUrl);
    	
    	for (NestedProcessor proc : nestedProcessors) {
    		proc.process();
    	}
    	
    	 // Get the batch feed URL and submit the batch request
        CellFeed feed = this.getSsService(accessToken, secretToken).getFeed(cellFeedUrl, CellFeed.class);
        Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
        URL batchUrl = new URL(batchLink.getHref());
        
        this.getSsService(accessToken, secretToken).batch(batchUrl, batchRequest);
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet) throws IOException, ServiceException {
    	
    	WorksheetEntry worksheetEntry = this.getWorksheetEntry(accessToken, secretToken, worksheet);

    	// Get the appropriate URL for a cell feed
    	URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
    	return ModelParser.parseCell(this.getSsService(accessToken, secretToken).getFeed(cellFeedUrl, CellFeed.class), worksheet);
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
    public List<Cell> getCellRange(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet,
    		int minRow,
    		int maxRow,
    		int minCol,
    		int maxCol) throws IOException, ServiceException {
      
    	CellQuery query = new CellQuery(this.getCellFeedUrl(accessToken, secretToken, worksheet));
    	query.setMinimumRow(minRow);
	    query.setMaximumRow(maxRow);
	    query.setMinimumCol(minCol);
	    query.setMaximumCol(maxCol);
	    return ModelParser.parseCell(this.getSsService(accessToken, secretToken).query(query, CellFeed.class), worksheet);
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
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
			@Optional @Default("#[payload:]") Worksheet worksheet,
    		String query) throws IOException, ServiceException {
    	
      CellQuery cellQuery = new CellQuery(this.getCellFeedUrl(accessToken, secretToken, worksheet));
      cellQuery.setFullTextQuery(query);
      
      return ModelParser.parseCell(this.getSsService(accessToken, secretToken).query(cellQuery, CellFeed.class), worksheet);
    }
    
    private SpreadsheetEntry getSpreadsheetEntry(
    		String accessToken,
			String secretToken,
    		Spreadsheet spreadsheet) throws IOException, ServiceException {
    	
    	SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
        spreadsheetQuery.setTitleQuery(spreadsheet.getTitle());
    	return this.getItem(this.getSsService(accessToken, secretToken).query(spreadsheetQuery, SpreadsheetFeed.class).getEntries(), spreadsheet.getIndex());
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
    										String accessToken,
											String secretToken,
											Spreadsheet spreadsheet,
											Worksheet worksheet) throws IOException, ServiceException {
    	
    	SpreadsheetEntry spreadsheetEntry = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet);
    	
    	WorksheetQuery worksheetQuery = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
    	worksheetQuery.setTitleQuery(worksheet.getTitle());
    	return this.getSsService(accessToken, secretToken).query(worksheetQuery, WorksheetFeed.class).getEntries();
    }
    
    private Spreadsheet getSpreadsheet(Worksheet worksheet) {
    	Spreadsheet spreadsheet = worksheet.getSpreadsheet();
    	
    	if (spreadsheet == null) {
    		throw new IllegalStateException("The worksheet is not associated with any spreadsheet");
    	}
    	
    	return spreadsheet;
    }
    
    private WorksheetEntry getWorksheetEntry(
    		String accessToken,
			String secretToken,
    		Worksheet worksheet) throws IOException, ServiceException {
    	
    	List<WorksheetEntry> worksheets = this.getWorksheetEntriesByTitle(accessToken, secretToken, this.getSpreadsheet(worksheet), worksheet);
    	return this.getItem(worksheets, worksheet.getIndex());
    }
    
    private URL getCellFeedUrl(String accessToken,
			String secretToken,
    		Worksheet worksheet) throws IOException, ServiceException {
    	
    	return this.getWorksheetEntry(accessToken, secretToken, worksheet).getCellFeedUrl();
    }
    
    private <T> T getItem(List<T> list, int index) {
    	if (list.isEmpty()) {
    		throw new IllegalArgumentException("No item found for that name");
    	} else if (index >= list.size()) {
    		throw new IllegalArgumentException("You requested item index " + index + 
    											" but only " + list.size() + " were found");
    	}
    	
    	return list.get(index);
    }
    
    private SpreadsheetService getSsService(String accessToken, String secretToken) throws ServiceException {
    	if (this.ssService == null) {
    		this.ssService = this.connect(new SpreadsheetService(this.applicationName), accessToken, secretToken);
    		
    		// workaround for issue described in http://code.google.com/p/gdata-java-client/issues/detail?id=103
    		this.ssService.setHeader("If-Match", "*");
    	}
    	
    	return this.ssService;
    }
    
    private DocsService getDocsService(String accessToken, String secretToken) throws ServiceException {
    	if (this.docService == null) {
    		this.docService = this.connect(new DocsService(this.applicationName), accessToken, secretToken);
    	}
    	return this.docService;
    }
    
    private <T extends GoogleService> T connect(T service, String accessToken, String secretToken) throws ServiceException {
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey(this.consumerKey);
		oauthParameters.setOAuthConsumerSecret(this.consumerSecret);
		oauthParameters.setOAuthToken(accessToken);
		oauthParameters.setOAuthTokenSecret(secretToken);
		
		try {
			service.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		} catch (OAuthException e) { 
			throw new ServiceException(e);
		}
    	
    	return service;
    }
    
	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
