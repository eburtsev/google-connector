
package org.mule.module.google.spreadsheet.config.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.mule.MessageExchangePattern;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.callback.HttpCallback;
import org.mule.api.construct.FlowConstructInvalidException;
import org.mule.api.endpoint.EndpointFactory;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.transport.Connector;
import org.mule.config.spring.factories.AsyncMessageProcessorsFactoryBean;
import org.mule.construct.Flow;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.processor.strategy.AsynchronousProcessingStrategy;

public class DefaultHttpCallback
    implements HttpCallback
{

    private final static Logger LOGGER = Logger.getLogger(DefaultHttpCallback.class);
    private Integer localPort;
    /**
     * The port number to be used in the dynamic http inbound endpoint that will receive the callback
     * 
     */
    private Integer remotePort;
    /**
     * The domain to be used in the dynamic http inbound endpoint that will receive the callback
     * 
     */
    private String domain;
    /**
     * The dynamically generated url to pass on to the cloud connector. When this url is called the callback flow will be executed
     * 
     */
    private String url;
    private String localUrl;
    /**
     * Mule Context
     * 
     */
    private MuleContext muleContext;
    /**
     * The flow to be called upon the http callback
     * 
     */
    private Flow callbackFlow;
    /**
     * The dynamically created flow
     * 
     */
    private Flow flowConstruct;
    /**
     * The message processor to be called upon the http callback
     * 
     */
    private MessageProcessor callbackMessageProcessor;
    /**
     * Optional path to set up the endpoint
     * 
     */
    private String callbackPath;
    /**
     * Whether the the message processor that invokes the callback flow is asynchronous
     * 
     */
    private Boolean async;
    /**
     * HTTP connector
     * 
     */
    private Connector connector;

    public DefaultHttpCallback(Flow callbackFlow, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, Boolean async) {
        this.callbackFlow = callbackFlow;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.domain = callbackDomain;
        this.async = async;
        this.connector = null;
    }

    public DefaultHttpCallback(MessageProcessor callbackMessageProcessor, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, Boolean async) {
        this.callbackMessageProcessor = callbackMessageProcessor;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.domain = callbackDomain;
        this.async = async;
        this.connector = null;
    }

    public DefaultHttpCallback(MessageProcessor callbackMessageProcessor, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, String callbackPath, Boolean async) {
        this.callbackMessageProcessor = callbackMessageProcessor;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.domain = callbackDomain;
        this.remotePort = remotePort;
        this.callbackPath = callbackPath;
        this.async = async;
        this.connector = null;
    }

    public DefaultHttpCallback(Flow callbackFlow, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, Boolean async, Connector connector) {
        this.callbackFlow = callbackFlow;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.domain = callbackDomain;
        this.async = async;
        this.connector = connector;
    }

    public DefaultHttpCallback(MessageProcessor callbackMessageProcessor, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, Boolean async, Connector connector) {
        this.callbackMessageProcessor = callbackMessageProcessor;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.domain = callbackDomain;
        this.async = async;
        this.connector = connector;
    }

    public DefaultHttpCallback(MessageProcessor callbackMessageProcessor, MuleContext muleContext, String callbackDomain, Integer localPort, Integer remotePort, String callbackPath, Boolean async, Connector connector) {
        this.callbackMessageProcessor = callbackMessageProcessor;
        this.muleContext = muleContext;
        this.localPort = localPort;
        this.domain = callbackDomain;
        this.remotePort = remotePort;
        this.callbackPath = callbackPath;
        this.async = async;
        this.connector = connector;
    }

    /**
     * Retrieves url
     * 
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets muleContext
     * 
     * @param value Value to set
     */
    public void setMuleContext(MuleContext value) {
        this.muleContext = value;
    }

    private String buildUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        if (!domain.contains("://")) {
            if (connector!= null) {
                urlBuilder.append((connector.getProtocol()+"://"));
            } else {
                urlBuilder.append("http://");
            }
        }
        urlBuilder.append(domain);
        urlBuilder.append(":");
        urlBuilder.append(remotePort);
        urlBuilder.append("/");
        if (callbackPath!= null) {
            urlBuilder.append(callbackPath);
        } else {
            urlBuilder.append(UUID.randomUUID());
        }
        return urlBuilder.toString();
    }

    private MessageProcessor wrapMessageProcessorInAsyncChain(MessageProcessor messageProcessor)
        throws MuleException
    {
        AsyncMessageProcessorsFactoryBean asyncMessageProcessorsFactoryBean = new AsyncMessageProcessorsFactoryBean();
        asyncMessageProcessorsFactoryBean.setMuleContext(muleContext);
        asyncMessageProcessorsFactoryBean.setMessageProcessors(Arrays.asList(messageProcessor));
        asyncMessageProcessorsFactoryBean.setProcessingStrategy(new AsynchronousProcessingStrategy());
        try {
            return ((MessageProcessor) asyncMessageProcessorsFactoryBean.getObject());
        } catch (Exception e) {
            throw new FlowConstructInvalidException(e);
        }
    }

    private Connector createConnector()
        throws MuleException
    {
        if (connector!= null) {
            return this.connector;
        }
        MuleRegistry muleRegistry = muleContext.getRegistry();
        Connector httpConnector = muleRegistry.lookupConnector("connector.http.mule.default");
        if (httpConnector!= null) {
            return httpConnector;
        } else {
            LOGGER.error("Could not find connector with name 'connector.http.mule.default'");
            throw new DefaultMuleException("Could not find connector with name 'connector.http.mule.default'");
        }
    }

    private InboundEndpoint createHttpInboundEndpoint()
        throws MuleException
    {
        EndpointURIEndpointBuilder inBuilder = new EndpointURIEndpointBuilder(localUrl, muleContext);
        inBuilder.setConnector(createConnector());
        inBuilder.setExchangePattern(MessageExchangePattern.REQUEST_RESPONSE);
        EndpointFactory endpointFactory = muleContext.getEndpointFactory();
        return endpointFactory.getInboundEndpoint(inBuilder);
    }

    public void start()
        throws MuleException
    {
        this.url = buildUrl();
        this.localUrl = url.replaceFirst(domain, "localhost");
        this.localUrl = localUrl.replaceFirst(String.valueOf(remotePort), String.valueOf(localPort));
        String dynamicFlowName = String.format("DynamicFlow-%s", localUrl);
        flowConstruct = new Flow(dynamicFlowName, muleContext);
        flowConstruct.setMessageSource(createHttpInboundEndpoint());
        MessageProcessor messageProcessor;
        if (callbackFlow!= null) {
            messageProcessor = new DefaultHttpCallback.FlowRefMessageProcessor();
        } else {
            messageProcessor = callbackMessageProcessor;
        }
        if (async) {
            messageProcessor = wrapMessageProcessorInAsyncChain(messageProcessor);
        }
        List<MessageProcessor> messageProcessors = new ArrayList<MessageProcessor>();
        messageProcessors.add(messageProcessor);
        flowConstruct.setMessageProcessors(messageProcessors);
        flowConstruct.initialise();
        flowConstruct.start();
        LOGGER.debug(String.format("Created flow with http inbound endpoint listening at: %s", url));
    }

    public void stop()
        throws MuleException
    {
        if (flowConstruct!= null) {
            flowConstruct.stop();
            flowConstruct.dispose();
            LOGGER.debug("Http callback flow stopped");
        }
    }

    public class FlowRefMessageProcessor
        implements MessageProcessor
    {


        public MuleEvent process(MuleEvent event)
            throws MuleException
        {
            return callbackFlow.process(event);
        }

    }

}
