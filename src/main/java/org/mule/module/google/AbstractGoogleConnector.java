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
package org.mule.module.google;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

/**
 * Base Google connector functionality. Unfortunately devkit doesn't support putting a lot of common
 * stuff here yet like annotations or configuration. See https://github.com/mulesoft/mule-devkit/issues/73
 *
 * @author MuleSoft, Inc.
 */
       
public abstract class AbstractGoogleConnector 
{

    private boolean tokenInitialized;
    
    /**
     * This is a hack until this gets fixed: https://github.com/mulesoft/mule-devkit/issues/64
     * Technically this could result in multiple initialization, but that's rare and harmless
     * since there is only ever one token per connector anyway. 
     */
    protected synchronized void initAccess(String accessToken, String accessTokenSecret) throws OAuthException {
        if (tokenInitialized) {
            return;
        }
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(getClientId());
        oauthParameters.setOAuthConsumerSecret(getClientSecret());
        oauthParameters.setOAuthToken(accessToken);
        oauthParameters.setOAuthTokenSecret(accessTokenSecret);
        getGoogleService().setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
        
        tokenInitialized = true;
    }

    protected abstract String getClientId();

    protected abstract String getClientSecret();

    protected abstract GoogleService getGoogleService();
}