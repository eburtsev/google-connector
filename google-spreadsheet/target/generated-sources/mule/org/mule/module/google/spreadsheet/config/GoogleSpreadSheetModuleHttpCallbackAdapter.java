
package org.mule.module.google.spreadsheet.config;

import org.apache.log4j.Logger;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.transport.Connector;
import org.mule.util.NumberUtils;

public class GoogleSpreadSheetModuleHttpCallbackAdapter
    extends GoogleSpreadSheetModuleLifecycleAdapter
    implements Initialisable
{

    private Integer localPort;
    private Integer remotePort;
    private String domain;
    private Connector connector;
    private final static Logger LOGGER = Logger.getLogger(GoogleSpreadSheetModuleHttpCallbackAdapter.class);
    private Boolean async = false;

    /**
     * Retrieves localPort
     * 
     */
    public Integer getLocalPort() {
        return this.localPort;
    }

    /**
     * Sets localPort
     * 
     * @param value Value to set
     */
    public void setLocalPort(Integer value) {
        this.localPort = value;
    }

    /**
     * Retrieves remotePort
     * 
     */
    public Integer getRemotePort() {
        return this.remotePort;
    }

    /**
     * Sets remotePort
     * 
     * @param value Value to set
     */
    public void setRemotePort(Integer value) {
        this.remotePort = value;
    }

    /**
     * Retrieves domain
     * 
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Sets domain
     * 
     * @param value Value to set
     */
    public void setDomain(String value) {
        this.domain = value;
    }

    /**
     * Retrieves connector
     * 
     */
    public Connector getConnector() {
        return this.connector;
    }

    /**
     * Sets connector
     * 
     * @param value Value to set
     */
    public void setConnector(Connector value) {
        this.connector = value;
    }

    /**
     * Retrieves async
     * 
     */
    public Boolean getAsync() {
        return this.async;
    }

    /**
     * Sets async
     * 
     * @param value Value to set
     */
    public void setAsync(Boolean value) {
        this.async = value;
    }

    public void initialise() {
        super.initialise();
        if (localPort == null) {
            String portSystemVar = System.getProperty("http.port");
            if (NumberUtils.isDigits(portSystemVar)) {
                localPort = Integer.parseInt(portSystemVar);
            } else {
                LOGGER.warn("Environment variable 'http.port' not found, using default localPort: 8080");
                localPort = 8080;
            }
        }
        if (remotePort == null) {
            LOGGER.info("Using default remotePort: 80");
            remotePort = 80;
        }
        if (domain == null) {
            String domainSystemVar = System.getProperty("fullDomain");
            if (domainSystemVar!= null) {
                domain = domainSystemVar;
            } else {
                LOGGER.warn("Environment variable 'fullDomain' not found, using default: localhost");
                domain = "localhost";
            }
        }
    }

}
