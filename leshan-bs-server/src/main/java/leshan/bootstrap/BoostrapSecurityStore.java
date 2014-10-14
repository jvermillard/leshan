package leshan.bootstrap;

import java.util.Arrays;
import java.util.Map;

import leshan.server.lwm2m.bootstrap.BootstrapConfig;
import leshan.server.lwm2m.bootstrap.BootstrapConfig.ServerSecurity;
import leshan.server.lwm2m.bootstrap.SecurityMode;
import leshan.server.lwm2m.security.SecurityInfo;
import leshan.server.lwm2m.security.SecurityStore;

import org.apache.commons.io.Charsets;

/**
 * A DTLS security store using the provisioned bootstrap information for finding the DTLS/PSK credentials.
 */
public class BoostrapSecurityStore implements SecurityStore {

    private final BootstrapStoreImpl bsStore;

    public BoostrapSecurityStore(BootstrapStoreImpl bsStore) {
        this.bsStore = bsStore;
    }

    @Override
    public SecurityInfo getByIdentity(String identity) {
        byte[] identityBytes = identity.getBytes(Charsets.UTF_8);
        for (Map.Entry<String, BootstrapConfig> e : bsStore.getBootstrapConfigs().entrySet()) {
            for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> ec : e.getValue().security.entrySet()) {
                if (ec.getValue().bootstrapServer && ec.getValue().securityMode == SecurityMode.PSK
                        && Arrays.equals(ec.getValue().publicKeyOrId, identityBytes)) {
                    return SecurityInfo.newPreSharedKeyInfo(e.getKey(), identity, ec.getValue().secretKey);
                }
            }
        }
        return null;
    }

    @Override
    public SecurityInfo getByEndpoint(String endpoint) {
        BootstrapConfig bootstrap = bsStore.getBootstrap(endpoint);

        for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> e : bootstrap.security.entrySet()) {
            ServerSecurity value = e.getValue();
            if (value.bootstrapServer && value.securityMode == SecurityMode.PSK) {
                // got it!
                return SecurityInfo.newPreSharedKeyInfo(endpoint, new String(value.publicKeyOrId, Charsets.UTF_8),
                        value.secretKey);
            }
        }
        return null;
    }
}
