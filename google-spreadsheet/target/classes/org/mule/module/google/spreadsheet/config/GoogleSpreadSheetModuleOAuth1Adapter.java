
package org.mule.module.google.spreadsheet.config;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
import oauth.signpost.signature.HmacSha1MessageSigner;
import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.callback.HttpCallback;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.oauth.OAuth1Adapter;
import org.mule.api.oauth.RestoreAccessTokenCallback;
import org.mule.api.oauth.SaveAccessTokenCallback;
import org.mule.api.oauth.UnableToAcquireAccessTokenException;
import org.mule.api.oauth.UnableToAcquireRequestTokenException;
import org.mule.api.processor.MessageProcessor;
import org.mule.config.i18n.MessageFactory;
import org.mule.module.google.spreadsheet.GoogleSpreadSheetModule;
import org.mule.module.google.spreadsheet.config.spring.DefaultHttpCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@code GoogleSpreadSheetModuleOAuth1Adapter} is a wrapper around {@link GoogleSpreadSheetModule } that adds OAuth capabilites to the pojo.
 * 
 */
public class GoogleSpreadSheetModuleOAuth1Adapter
    extends GoogleSpreadSheetModuleHttpCallbackAdapter
    implements MuleContextAware, Initialisable, Startable, Stoppable, OAuth1Adapter
{

    private final static Pattern AUTH_CODE_PATTERN = Pattern.compile("oauth_verifier=([^&]+)");
    private MuleContext muleContext;
    private static Logger logger = LoggerFactory.getLogger(GoogleSpreadSheetModuleOAuth1Adapter.class);
    private String requestToken;
    private String requestTokenSecret;
    private String oauthVerifier;
    private SaveAccessTokenCallback oauthSaveAccessToken;
    private RestoreAccessTokenCallback oauthRestoreAccessToken;
    private String redirectUrl;
    private String accessToken;
    private String accessTokenSecret;
    private OAuthConsumer consumer;
    private HttpCallback oauthCallback;

    /**
     * Sets muleContext
     * 
     * @param value Value to set
     */
    public void setMuleContext(MuleContext value) {
        this.muleContext = value;
    }

    /**
     * Retrieves oauthVerifier
     * 
     */
    public String getOauthVerifier() {
        return this.oauthVerifier;
    }

    /**
     * Sets oauthVerifier
     * 
     * @param value Value to set
     */
    public void setOauthVerifier(String value) {
        this.oauthVerifier = value;
    }

    /**
     * Retrieves oauthSaveAccessToken
     * 
     */
    public SaveAccessTokenCallback getOauthSaveAccessToken() {
        return this.oauthSaveAccessToken;
    }

    /**
     * Sets oauthSaveAccessToken
     * 
     * @param value Value to set
     */
    public void setOauthSaveAccessToken(SaveAccessTokenCallback value) {
        this.oauthSaveAccessToken = value;
    }

    /**
     * Retrieves oauthRestoreAccessToken
     * 
     */
    public RestoreAccessTokenCallback getOauthRestoreAccessToken() {
        return this.oauthRestoreAccessToken;
    }

    /**
     * Sets oauthRestoreAccessToken
     * 
     * @param value Value to set
     */
    public void setOauthRestoreAccessToken(RestoreAccessTokenCallback value) {
        this.oauthRestoreAccessToken = value;
    }

    /**
     * Retrieves redirectUrl
     * 
     */
    public String getRedirectUrl() {
        return this.redirectUrl;
    }

    /**
     * Retrieves accessToken
     * 
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Sets accessToken
     * 
     * @param value Value to set
     */
    public void setAccessToken(String value) {
        this.accessToken = value;
    }

    /**
     * Retrieves accessTokenSecret
     * 
     */
    public String getAccessTokenSecret() {
        return this.accessTokenSecret;
    }

    /**
     * Sets accessTokenSecret
     * 
     * @param value Value to set
     */
    public void setAccessTokenSecret(String value) {
        this.accessTokenSecret = value;
    }

    private void createConsumer() {
        consumer = new DefaultOAuthConsumer(getConsumerKey(), getConsumerSecret());
        consumer.setMessageSigner(new HmacSha1MessageSigner());
        consumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
    }

    public void start()
        throws MuleException
    {
        super.start();
        oauthCallback.start();
        redirectUrl = oauthCallback.getUrl();
    }

    public void stop()
        throws MuleException
    {
        super.stop();
        oauthCallback.stop();
    }

    public void initialise() {
        super.initialise();
        oauthCallback = new DefaultHttpCallback(new GoogleSpreadSheetModuleOAuth1Adapter.OnOAuthCallbackMessageProcessor(), muleContext, getDomain(), getLocalPort(), getRemotePort(), "oauth2callback", getAsync());
        createConsumer();
    }

    public String getAuthorizationUrl()
        throws UnableToAcquireRequestTokenException
    {
        String requestTokenUrl = "https://www.google.com/accounts/OAuthGetRequestToken";
        String scope = getScope();
        if (scope!= null) {
            try {
                String scopeParam = "?scope=".concat(URLEncoder.encode(scope, "UTF-8"));
                requestTokenUrl = requestTokenUrl.concat(scopeParam);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        OAuthProvider provider = new DefaultOAuthProvider(requestTokenUrl, "https://www.google.com/accounts/OAuthGetAccessToken", "https://www.google.com/accounts/OAuthAuthorizeToken");
        provider.setOAuth10a(true);
        String authorizationUrl;
        try {
            if (logger.isDebugEnabled()) {
                StringBuilder messageStringBuilder = new StringBuilder();
                messageStringBuilder.append("Attempting to acquire a request token ");
                messageStringBuilder.append("[consumer = ");
                messageStringBuilder.append(consumer.getConsumerKey());
                messageStringBuilder.append("] ");
                messageStringBuilder.append("[consumerSecret = ");
                messageStringBuilder.append(consumer.getConsumerSecret());
                messageStringBuilder.append("] ");
                logger.debug(messageStringBuilder.toString());
            }
            authorizationUrl = provider.retrieveRequestToken(consumer, redirectUrl);
            if (logger.isDebugEnabled()) {
                StringBuilder messageStringBuilder = new StringBuilder();
                messageStringBuilder.append("Request token acquired ");
                messageStringBuilder.append("[requestToken = ");
                messageStringBuilder.append(consumer.getToken());
                messageStringBuilder.append("] ");
                messageStringBuilder.append("[requestTokenSecret = ");
                messageStringBuilder.append(consumer.getTokenSecret());
                messageStringBuilder.append("] ");
                logger.debug(messageStringBuilder.toString());
            }
        } catch (OAuthMessageSignerException e) {
            throw new UnableToAcquireRequestTokenException(e);
        } catch (OAuthNotAuthorizedException e) {
            throw new UnableToAcquireRequestTokenException(e);
        } catch (OAuthExpectationFailedException e) {
            throw new UnableToAcquireRequestTokenException(e);
        } catch (OAuthCommunicationException e) {
            throw new UnableToAcquireRequestTokenException(e);
        }
        requestToken = consumer.getToken();
        requestTokenSecret = consumer.getTokenSecret();
        return authorizationUrl;
    }

    public void fetchAccessToken()
        throws UnableToAcquireAccessTokenException
    {
        if (oauthRestoreAccessToken!= null) {
            if (logger.isDebugEnabled()) {
                StringBuilder messageStringBuilder = new StringBuilder();
                messageStringBuilder.append("Attempting to restore access token...");
                logger.debug(messageStringBuilder.toString());
            }
            try {
                oauthRestoreAccessToken.restoreAccessToken();
                accessToken = oauthRestoreAccessToken.getAccessToken();
                accessTokenSecret = oauthRestoreAccessToken.getAccessTokenSecret();
            } catch (Exception e) {
                logger.error("Cannot restore access token, an unexpected error occurred", e);
            }
            if (logger.isDebugEnabled()) {
                StringBuilder messageStringBuilder = new StringBuilder();
                messageStringBuilder.append("Access token and secret has been restored successfully ");
                messageStringBuilder.append("[accessToken = ");
                messageStringBuilder.append(oauthRestoreAccessToken.getAccessToken());
                messageStringBuilder.append("] ");
                messageStringBuilder.append("[accessTokenSecret = ");
                messageStringBuilder.append(oauthRestoreAccessToken.getAccessTokenSecret());
                messageStringBuilder.append("] ");
                logger.debug(messageStringBuilder.toString());
            }
        }
        if ((accessToken == null)||(accessTokenSecret == null)) {
            String requestTokenUrl = "https://www.google.com/accounts/OAuthGetRequestToken";
            String scope = getScope();
            if (scope!= null) {
                try {
                    String scopeParam = "?scope=".concat(URLEncoder.encode(scope, "UTF-8"));
                    requestTokenUrl = requestTokenUrl.concat(scopeParam);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            OAuthProvider provider = new DefaultOAuthProvider(requestTokenUrl, "https://www.google.com/accounts/OAuthGetAccessToken", "https://www.google.com/accounts/OAuthAuthorizeToken");
            provider.setOAuth10a(true);
            consumer.setTokenWithSecret(requestToken, requestTokenSecret);
            try {
                if (logger.isDebugEnabled()) {
                    StringBuilder messageStringBuilder = new StringBuilder();
                    messageStringBuilder.append("Retrieving access token...");
                    logger.debug(messageStringBuilder.toString());
                }
                provider.retrieveAccessToken(consumer, oauthVerifier);
            } catch (OAuthMessageSignerException e) {
                throw new UnableToAcquireAccessTokenException(e);
            } catch (OAuthNotAuthorizedException e) {
                throw new UnableToAcquireAccessTokenException(e);
            } catch (OAuthExpectationFailedException e) {
                throw new UnableToAcquireAccessTokenException(e);
            } catch (OAuthCommunicationException e) {
                throw new UnableToAcquireAccessTokenException(e);
            }
            accessToken = consumer.getToken();
            accessTokenSecret = consumer.getTokenSecret();
            if (logger.isDebugEnabled()) {
                StringBuilder messageStringBuilder = new StringBuilder();
                messageStringBuilder.append("Access token retrieved successfully ");
                messageStringBuilder.append("[accessToken = ");
                messageStringBuilder.append(accessToken);
                messageStringBuilder.append("] ");
                messageStringBuilder.append("[accessTokenSecret = ");
                messageStringBuilder.append(accessTokenSecret);
                messageStringBuilder.append("] ");
                logger.debug(messageStringBuilder.toString());
            }
            if (oauthSaveAccessToken!= null) {
                try {
                    oauthSaveAccessToken.saveAccessToken(accessToken, accessTokenSecret);
                } catch (Exception e) {
                    logger.error("Cannot save access token, an unexpected error occurred", e);
                }
                if (logger.isDebugEnabled()) {
                    StringBuilder messageStringBuilder = new StringBuilder();
                    messageStringBuilder.append("Attempting to save access token...");
                    messageStringBuilder.append("[accessToken = ");
                    messageStringBuilder.append(accessToken);
                    messageStringBuilder.append("] ");
                    messageStringBuilder.append("[accessTokenSecret = ");
                    messageStringBuilder.append(accessTokenSecret);
                    messageStringBuilder.append("] ");
                    logger.debug(messageStringBuilder.toString());
                }
            }
        }
    }

    private class OnOAuthCallbackMessageProcessor
        implements MessageProcessor
    {


        public MuleEvent process(MuleEvent event)
            throws MuleException
        {
            try {
                oauthVerifier = extractAuthorizationCode(event.getMessageAsString());
                fetchAccessToken();
            } catch (Exception e) {
                throw new MessagingException(MessageFactory.createStaticMessage("Could not extract OAuth verifier"), event, e);
            }
            return event;
        }

        private String extractAuthorizationCode(String response)
            throws Exception
        {
            Matcher matcher = AUTH_CODE_PATTERN.matcher(response);
            if (matcher.find()&&(matcher.groupCount()>= 1)) {
                return URLDecoder.decode(matcher.group(1), "UTF-8");
            } else {
                throw new Exception(String.format("OAuth authorization code could not be extracted from: %s", response));
            }
        }

    }

}
