Google Connectors
=================

1. Create your connector
    
    <gcontacts:config name="google-contacts" clientId="${client.id}" clientSecret="${client.secret}">
        <gcontacts:oauth-callback-config domain="localhost" localPort="${http.port}" remotePort="${http.port}"/>
    </gcontacts:config>
    
You can get your client ID and secret here:
https://code.google.com/apis/console

2. You can then authorize your connector by creating a flow like this:

    <flow name="authorize">
        <inbound-endpoint address="http://localhost:${http.port}/authorize"/>
        <gcontacts:authorize/>
    </flow>
    
And browsing to the URL  http://localhost:${http.port}/authorize

3. You can invoke your connector like so:

    <flow name="get contacts">
        <gcontacts:get-contacts/>
        <logger level="INFO" message="Contacts: #[payload]"/> 
    </flow>
    