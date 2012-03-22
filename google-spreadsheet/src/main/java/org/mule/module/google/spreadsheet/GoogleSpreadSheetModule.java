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

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.TableEntry;
import com.google.gdata.data.spreadsheet.TableFeed;
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
    private String scope = "https://docs.google.com/feeds/private/full https://spreadsheets.google.com/feeds http://spreadsheets.google.com/feeds";
    
    private FeedURLFactory factory = FeedURLFactory.getDefault();
    
    @Configurable
    @Optional
    private String applicationName = "Mule-GoogleDocsConnector/1.0";
    
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
    public List<SpreadsheetEntry> getAllSpreadsheets(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken) throws OAuthException, IOException, ServiceException {
    	
        SpreadsheetFeed feed = getSsService(accessToken, secretToken).getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
        return feed.getEntries();
    }
    
    /**
     * 
     * @param accessToken
     * @param secretToken
     * @param title
     * @throws OAuthException
     * @throws IOException
     * @throws ServiceException
     */
    @Processor
    public void createSpreadsheet(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken,
    		String title) throws OAuthException, IOException, ServiceException {

    	com.google.gdata.data.docs.SpreadsheetEntry newEntry = new com.google.gdata.data.docs.SpreadsheetEntry();
        newEntry.setTitle(new PlainTextConstruct(title));
        this.getDocsService(accessToken, secretToken).insert(new URL("https://docs.google.com/feeds/default/private/full"), newEntry);
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
    public List<WorksheetEntry> getAllWorksheets(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {
    	
    	SpreadsheetEntry ss = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet, spreadsheetIndex);
    	return getSsService(accessToken, secretToken).getFeed(ss.getWorksheetFeedUrl(), WorksheetFeed.class).getEntries();
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
    		String spreadsheet,
    		@Optional @Default("0") int spreadsheetIndex,
    		String title,
    		int rowCount,
    		int colCount) throws IOException, ServiceException {
    	
    	SpreadsheetEntry ss = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet, spreadsheetIndex);
    	WorksheetEntry worksheet = new WorksheetEntry();
    	worksheet.setTitle(new PlainTextConstruct(title));
    	worksheet.setRowCount(rowCount);
    	worksheet.setColCount(colCount);
    	getSsService(accessToken, secretToken).insert(ss.getWorksheetFeedUrl(), worksheet);
    }
    
    /**
     * Updates the worksheet specified by the oldTitle parameter, with the given
     * title and sizes. Note that worksheet titles are not unique, so this method
     * just updates the first worksheet it finds. Hey, it's just sample code - no
     * refunds!
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
    public void updateWorksheet(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional String title,
    		@Optional @Default("0") int rowCount,
    		@Optional @Default("0") int colCount,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
    	
    	WorksheetEntry ws = this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
    	
    	if (title != null) {
    		ws.setTitle(new PlainTextConstruct(title));
    	}
    	
    	if (rowCount > 0) {
    		ws.setRowCount(rowCount);
    	}
    	
    	if (colCount > 0) {
    		ws.setColCount(colCount);
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
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
    	
    	this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).delete();
    }
    
    @Processor
    public List<Person> getAuthors(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {
    	
    	return this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet, spreadsheetIndex).getAuthors();
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
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
    	
    	WorksheetEntry worksheetEntry = this.getWorksheetEntry(
    						accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
    	
    	
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
        Cell cell = entry.getCell();
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
    public List<SpreadsheetEntry> getSpreadsheet(
    		@OAuthAccessToken String accessToken,
    		@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet) throws IOException, ServiceException {
      
    	SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
        spreadsheetQuery.setTitleQuery(spreadsheet);
        SpreadsheetFeed spreadsheetFeed = getSsService(accessToken, secretToken).query(spreadsheetQuery, SpreadsheetFeed.class);
        return spreadsheetFeed.getEntries();
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
    public List<WorksheetEntry> getWorksheet(
    							@OAuthAccessToken String accessToken,
    							@OAuthAccessTokenSecret String secretToken,
    							String spreadsheet,
    							String worksheet,
    							@Optional @Default("0") int spreadsheetIndex)
    							throws IOException, ServiceException {
    	
    	SpreadsheetEntry spreadsheetEntry = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet, spreadsheetIndex);
    	
    	WorksheetQuery worksheetQuery = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
    	worksheetQuery.setTitleQuery(worksheet);
    	WorksheetFeed worksheetFeed = getSsService(accessToken, secretToken).query(worksheetQuery, WorksheetFeed.class);
      
    	return worksheetFeed.getEntries();
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
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex)
			throws IOException, ServiceException {
    	
    	WorksheetEntry worksheetEntry = this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
    	
    	CellFeed cellFeed = getSsService(accessToken, secretToken).getFeed(worksheetEntry.getCellFeedUrl(), CellFeed.class);

    	for (CellEntry cell : cellFeed.getEntries()) {
    		Link editLink = cell.getEditLink();
    		getSsService(accessToken, secretToken).delete(new URL(editLink.getHref()));
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
    		String spreadsheet,
    		String worksheet,
    		int row,
    		int column,
    		String formulaOrValue,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
      
    	URL cellFeedUrl = this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();

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
    public List<CellEntry> batchSetCellValue(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		List<NestedProcessor> nestedProcessors,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws Exception {
    	
    	URL cellFeedUrl = this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();
    	
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
        CellFeed batchResponse = this.getSsService(accessToken, secretToken).batch(batchUrl, batchRequest);;
        
        return batchResponse.getEntries();
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
    public List<CellEntry> getAllCells(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
    	
    	WorksheetEntry worksheetEntry = this.getWorksheetEntry(
				accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);

    	// Get the appropriate URL for a cell feed
    	URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
    	return this.getSsService(accessToken, secretToken).getFeed(cellFeedUrl, CellFeed.class).getEntries();
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
    public List<CellEntry> getCellRange(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
    		int minRow,
    		int maxRow,
    		int minCol,
    		int maxCol) throws IOException, ServiceException {
      
    	CellQuery query = new CellQuery(this.getCellFeedUrl(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex));
    	query.setMinimumRow(minRow);
	    query.setMaximumRow(maxRow);
	    query.setMinimumCol(minCol);
	    query.setMaximumCol(maxCol);
	    return getSsService(accessToken, secretToken).query(query, CellFeed.class).getEntries();
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
    public List<CellEntry> search(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
    		String query) throws IOException, ServiceException {
    	
      CellQuery cellQuery = new CellQuery(this.getCellFeedUrl(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex));
      cellQuery.setFullTextQuery(query);
      
      return getSsService(accessToken, secretToken).query(cellQuery, CellFeed.class).getEntries();
    }
    
    /**
     * Performs a full database-like query on the rows.
     * 
     * @param accessToken
     * @param secretToken
     * @param spreadsheet
     * @param worksheet
     * @param spreadsheetIndex
     * @param worksheetIndex
     * @param query a query like: name = "Bob" and phone != "555-1212"
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    @Processor
    public List<ListEntry> structuredQuery(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex,
    		String query) throws IOException, ServiceException {
    	
    	WorksheetEntry ws = this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex);
    	
    	ListQuery listQuery = new ListQuery(ws.getListFeedUrl());
    	listQuery.setSpreadsheetQuery(query);
        return getSsService(accessToken, secretToken).query(listQuery, ListFeed.class).getEntries();
    }
    
    /**
     * 
     * @param accessToken
     * @param secretToken
     * @param spreadsheet
     * @param worksheet
     * @param spreadsheetIndex
     * @param worksheetIndex
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    @Processor
    public List<ListEntry> getAllListEntries(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		String worksheet,
    		@Optional @Default("0") int spreadsheetIndex,
			@Optional @Default("0") int worksheetIndex) throws IOException, ServiceException {
    	
    	return getSsService(accessToken, secretToken).getFeed(this.getListFeedURL(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex), ListFeed.class).getEntries();
    }
    
    /**
     * Lists the tables currently available in the sheet.
     * 
     * @param accessToken
     * @param secretToken
     * @param spreadsheet
     * @param spreadsheetIndex
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    @Processor
    public List<TableEntry> getAllTables(
    		@OAuthAccessToken String accessToken,
			@OAuthAccessTokenSecret String secretToken,
    		String spreadsheet,
    		@Optional @Default("0") int spreadsheetIndex) throws IOException, ServiceException {
    	
    	return getSsService(accessToken, secretToken).getFeed(this.getRecordFeedURL(accessToken, secretToken, spreadsheet, spreadsheetIndex), TableFeed.class).getEntries();
    }
    
    private SpreadsheetEntry getSpreadsheetEntry(
    		String accessToken,
			String secretToken,
    		String spreadsheet,
    		int spreadsheetIndex) throws IOException, ServiceException {
    	return this.getItem(this.getSpreadsheet(accessToken, secretToken, spreadsheet), spreadsheetIndex);
    }
    
    private URL getRecordFeedURL(
    		String accessToken,
			String secretToken,
    		String spreadsheet,
			int spreadsheetIndex) throws IOException, ServiceException {
    	
    	SpreadsheetEntry ss = this.getSpreadsheetEntry(accessToken, secretToken, spreadsheet, spreadsheetIndex);
    	URL spreadsheetUrl = new java.net.URL(ss.getSpreadsheetLink().getHref());
    	return new java.net.URL(spreadsheetUrl.getProtocol() + "://" + spreadsheetUrl.getHost() + "/feeds/"
    							+ ss.getKey() + "/tables");
    }
    
    private URL getListFeedURL(
    		String accessToken,
			String secretToken,
    		String spreadsheet,
    		String worksheet,
			int spreadsheetIndex,
    		int worksheetIndex) throws IOException, ServiceException {
    	
    	return this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getListFeedUrl();
    }
    
    private WorksheetEntry getWorksheetEntry(
    		String accessToken,
			String secretToken,
    		String spreadsheet,
    		String worksheet,
			int spreadsheetIndex,
    		int worksheetIndex) throws IOException, ServiceException {
    	
    	List<WorksheetEntry> worksheets = this.getWorksheet(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex);
    	return this.getItem(worksheets, worksheetIndex);
    }
    
    private URL getCellFeedUrl(String accessToken,
			String secretToken,
    		String spreadsheet,
    		String worksheet,
			int spreadsheetIndex,
    		int worksheetIndex) throws IOException, ServiceException {
    	
    	return this.getWorksheetEntry(accessToken, secretToken, spreadsheet, worksheet, spreadsheetIndex, worksheetIndex).getCellFeedUrl();
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
    	if (this.getDocsService(accessToken, secretToken) == null) {
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
