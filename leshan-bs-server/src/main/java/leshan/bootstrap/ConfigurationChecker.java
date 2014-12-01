/*
 * Copyright (c) 2014, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.bootstrap;

import java.util.Map;

import leshan.server.bootstrap.BootstrapConfig;

import org.apache.commons.lang.ArrayUtils;

/**
 * Check it's a BoostrapConfig is correct. this is a complex process, we need to check if the different objects are in
 * coherence with each others.
 */
public class ConfigurationChecker {

    public static void verify(BootstrapConfig config) throws ConfigurationException {
        // check security configurations
        for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> e : config.security.entrySet()) {
            BootstrapConfig.ServerSecurity sec = e.getValue();
            switch (sec.securityMode) {
            case NO_SEC:
                assertIf(!ArrayUtils.isEmpty(sec.secretKey), "NO-SEC mode, secret key must be empty");
                assertIf(!ArrayUtils.isEmpty(sec.publicKeyOrId), "NO-SEC mode, public key or ID must be empty");
                assertIf(!ArrayUtils.isEmpty(sec.serverPublicKeyOrId),
                        "NO-SEC mode, server public key or ID must be empty");
                break;
            case PSK:
                assertIf(ArrayUtils.isEmpty(sec.secretKey), "pre-shared-key mode, secret key must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.publicKeyOrId),
                        "pre-shared-key mode, public key or id must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.serverPublicKeyOrId),
                        "pre-shared-key mode, server public key or ID must not be empty");
                break;
            case RPK:
                assertIf(ArrayUtils.isEmpty(sec.secretKey), "pre-shared-key mode, secret key must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.publicKeyOrId),
                        "pre-shared-key mode, public key or id must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.serverPublicKeyOrId),
                        "pre-shared-key mode, server public key or ID must not be empty");
                break;
            case X509:
                assertIf(ArrayUtils.isEmpty(sec.secretKey), "pre-shared-key mode, secret key must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.publicKeyOrId),
                        "pre-shared-key mode, public key or id must not be empty");
                assertIf(ArrayUtils.isEmpty(sec.serverPublicKeyOrId),
                        "pre-shared-key mode, server public key or ID must not be empty");
            default:
                break;
            }
        }

        // does each server have a corresponding security entry?
        for (Map.Entry<Integer, BootstrapConfig.ServerConfig> e : config.servers.entrySet()) {
            BootstrapConfig.ServerConfig srvCfg = e.getValue();

            // shortId checks
            if (srvCfg.shortId == 0) {
                throw new ConfigurationException("short ID must not be 0");
            }

            // look for security entry
            BootstrapConfig.ServerSecurity security = getSecurityEntry(config, srvCfg.shortId);

            if (security == null) {
                throw new ConfigurationException("no security entry for server instance: " + e.getKey());
            }

            if (security.bootstrapServer) {
                throw new ConfigurationException("the security entry for server  " + e.getKey()
                        + " should not be a boostrap server");
            }
        }
    }

    private static void assertIf(boolean condition, String message) throws ConfigurationException {
        if (!condition) {
            throw new ConfigurationException(message);
        }

    }

    private static BootstrapConfig.ServerSecurity getSecurityEntry(BootstrapConfig config, int shortId) {
        for (Map.Entry<Integer, BootstrapConfig.ServerSecurity> es : config.security.entrySet()) {
            if (es.getValue().serverId == shortId) {
                return es.getValue();
            }
        }
        return null;
    }

    public static class ConfigurationException extends Exception {

        private static final long serialVersionUID = 1L;

        public ConfigurationException(String message) {
            super(message);
        }
    }
}
