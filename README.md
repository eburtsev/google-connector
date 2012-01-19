Google Connectors
=================

1. Create your connector
    
    &lt;gcontacts:config name="google-contacts" clientId="${client.id}" clientSecret="${client.secret}"&gt;
        &lt;gcontacts:oauth-callback-config domain="localhost" localPort="${http.port}" remotePort="${http.port}"/&gt;
    &lt;/gcontacts:config&gt;
    
You can get your client ID and secret here:
https://code.google.com/apis/console

2. You can then authorize your connector by creating a flow like this:

    &lt;flow name="authorize"&gt;
        &lt;inbound-endpoint address="http://localhost:${http.port}/authorize"/&gt;
        &lt;gcontacts:authorize/&gt;
    &lt;/flow&gt;
    
And browsing to the URL  http://localhost:${http.port}/authorize

3. You can invoke your connector like so:

    &lt;flow name="get contacts"&gt;
        &lt;gcontacts:get-contacts/&gt;
        &lt;logger level="INFO" message="Contacts: #[payload]"/&gt; 
    &lt;/flow&gt;
    