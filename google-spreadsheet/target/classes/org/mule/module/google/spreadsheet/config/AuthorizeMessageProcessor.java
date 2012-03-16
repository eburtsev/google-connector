
package org.mule.module.google.spreadsheet.config;

import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.oauth.UnableToAcquireAccessTokenException;
import org.mule.api.oauth.UnableToAcquireRequestTokenException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;

public class AuthorizeMessageProcessor
    implements FlowConstructAware, MuleContextAware, Disposable, Initialisable, Startable, Stoppable, MessageProcessor
{

    /**
     * Module object
     * 
     */
    private Object moduleObject;
    /**
     * Mule Context
     * 
     */
    private MuleContext muleContext;
    /**
     * Flow construct
     * 
     */
    private FlowConstruct flowConstruct;

    /**
     * Obtains the expression manager from the Mule context and initialises the connector. If a target object  has not been set already it will search the Mule registry for a default one.
     * 
     * @throws InitialisationException
     */
    public void initialise()
        throws InitialisationException
    {
        if (moduleObject == null) {
            try {
                moduleObject = muleContext.getRegistry().lookupObject(GoogleSpreadSheetModuleOAuth1Adapter.class);
                if (moduleObject == null) {
                    throw new InitialisationException(MessageFactory.createStaticMessage("Cannot find object"), this);
                }
            } catch (RegistrationException e) {
                throw new InitialisationException(CoreMessages.initialisationFailure("org.mule.module.google.spreadsheet.config.GoogleSpreadSheetModuleOAuth1Adapter"), e, this);
            }
        }
        if (moduleObject instanceof String) {
            moduleObject = muleContext.getRegistry().lookupObject(((String) moduleObject));
            if (moduleObject == null) {
                throw new InitialisationException(MessageFactory.createStaticMessage("Cannot find object by config name"), this);
            }
        }
    }

    public void start()
        throws MuleException
    {
    }

    public void stop()
        throws MuleException
    {
    }

    public void dispose() {
    }

    /**
     * Set the Mule context
     * 
     * @param context Mule context to set
     */
    public void setMuleContext(MuleContext context) {
        this.muleContext = context;
    }

    /**
     * Sets flow construct
     * 
     * @param flowConstruct Flow construct to set
     */
    public void setFlowConstruct(FlowConstruct flowConstruct) {
        this.flowConstruct = flowConstruct;
    }

    /**
     * Sets the instance of the object under which the processor will execute
     * 
     * @param moduleObject Instace of the module
     */
    public void setModuleObject(Object moduleObject) {
        this.moduleObject = moduleObject;
    }

    /**
     * Starts the OAuth authorization process
     * 
     * @param event MuleEvent to be processed
     * @throws MuleException
     */
    public MuleEvent process(MuleEvent event)
        throws MuleException
    {
        GoogleSpreadSheetModuleOAuth1Adapter castedModuleObject = null;
        if (moduleObject instanceof String) {
            castedModuleObject = ((GoogleSpreadSheetModuleOAuth1Adapter) muleContext.getRegistry().lookupObject(((String) moduleObject)));
            if (castedModuleObject == null) {
                throw new MessagingException(CoreMessages.failedToCreate("authorize"), event, new RuntimeException("Cannot find the configuration specified by the config-ref attribute."));
            }
        } else {
            castedModuleObject = ((GoogleSpreadSheetModuleOAuth1Adapter) moduleObject);
        }
        try {
            if (castedModuleObject.getOauthVerifier() == null) {
                String authorizationUrl = castedModuleObject.getAuthorizationUrl();
                event.getMessage().setOutboundProperty("http.status", "302");
                event.getMessage().setOutboundProperty("Location", authorizationUrl);
                return event;
            }
            if (castedModuleObject.getAccessToken() == null) {
                castedModuleObject.fetchAccessToken();
            }
            return event;
        } catch (UnableToAcquireRequestTokenException e) {
            throw new MessagingException(CoreMessages.failedToInvoke("authorize"), event, e);
        } catch (UnableToAcquireAccessTokenException e2) {
            throw new MessagingException(CoreMessages.failedToInvoke("authorize"), event, e2);
        }
    }

}
