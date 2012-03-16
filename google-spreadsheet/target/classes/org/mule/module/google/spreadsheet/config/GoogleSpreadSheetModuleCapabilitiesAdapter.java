
package org.mule.module.google.spreadsheet.config;

import org.mule.api.Capabilities;
import org.mule.api.Capability;


/**
 * A <code>GoogleSpreadSheetModuleCapabilitiesAdapter</code> is a wrapper around {@link org.mule.module.google.spreadsheet.GoogleSpreadSheetModule } that implements {@link org.mule.api.Capabilities} interface.
 * 
 */
public class GoogleSpreadSheetModuleCapabilitiesAdapter
    extends org.mule.module.google.spreadsheet.GoogleSpreadSheetModule
    implements Capabilities
{


    /**
     * Returns true if this module implements such capability
     * 
     */
    public boolean isCapableOf(Capability capability) {
        if (capability == Capability.LIFECYCLE_CAPABLE) {
            return true;
        }
        if (capability == Capability.OAUTH1_CAPABLE) {
            return true;
        }
        return false;
    }

}
