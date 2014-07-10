package leshan.server.lwm2m.security;

import java.util.Map;

import org.eclipse.californium.scandium.dtls.pskstore.PskStore;

/**
 * A registry for {@link SecurityInfo}.
 */
public interface SecurityRegistry extends PskStore {

    /**
     * Returns the security information for a given end-point.
     * 
     * @param endpoint the client end-point
     * @return the security information of <code>null</code> if not found.
     */
    SecurityInfo get(String endpoint);

    /**
     * Returns the {@link SecurityInfo} for all end-points.
     */
    Map<String, SecurityInfo> getAll();

    /**
     * Registers new security information for a given end-point.
     * 
     * @param endpoint the client end-point
     * @param info the new security information
     * @return the {@link SecurityInfo} previously stored for this end-point or <code>null</code> if there was no
     *         security information for this end-point.
     */
    SecurityInfo add(String endpoint, SecurityInfo info);

    /**
     * Removes the security information for a given end-point.
     * 
     * @param endpoint the client end-point
     * @return the removed {@link SecurityInfo} or <code>null</code> if no info for the end-point.
     */
    SecurityInfo remove(String endpoint);
}
