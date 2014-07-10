package leshan.server.lwm2m.security;

public class SecurityInfo {

    // PSK
    private final String identity;
    private final byte[] preSharedKey;

    private SecurityInfo(String identity, byte[] preSharedKey) {
        this.identity = identity;
        this.preSharedKey = preSharedKey;
    }

    /**
     * Construct a {@link SecurityInfo} when using DTLS with Pre-Shared Keys.
     */
    public static SecurityInfo newPreSharedKeyInfo(String identity, byte[] preSharedKey) {
        return new SecurityInfo(identity, preSharedKey);
    }

    public String getIdentity() {
        return identity;
    }

    public byte[] getPreSharedKey() {
        return preSharedKey;
    }

}
