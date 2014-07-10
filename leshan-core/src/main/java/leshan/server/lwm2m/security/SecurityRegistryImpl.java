/*
 * Copyright (c) 2013, Sierra Wireless
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
package leshan.server.lwm2m.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory security registry.
 */
public class SecurityRegistryImpl implements SecurityRegistry {

    // by client end-point
    private Map<String, SecurityInfo> securityByEp = new ConcurrentHashMap<>();

    // by PSK identity
    private Map<String, SecurityInfo> securityByIdentity = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityInfo get(String endpoint) {
        return securityByEp.get(endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, SecurityInfo> getAll() {
        return Collections.unmodifiableMap(securityByEp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SecurityInfo add(String endpoint, SecurityInfo info) {
        SecurityInfo previous = securityByEp.put(endpoint, info);

        if (previous != null) {
            securityByIdentity.remove(previous.getIdentity());
        }
        if (info.getIdentity() != null) {
            securityByIdentity.put(info.getIdentity(), info);
        }

        return previous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SecurityInfo remove(String endpoint) {
        SecurityInfo info = securityByEp.get(endpoint);
        if (info != null) {
            if (info.getIdentity() != null) {
                securityByIdentity.remove(info.getIdentity());
            }
            return securityByEp.remove(endpoint);
        }
        return null;
    }

    // /////// PSK store

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getKey(String identity) {
        SecurityInfo info = securityByIdentity.get(identity);
        if (info == null || info.getPreSharedKey() == null) {
            return null;
        } else {
            // defensive copy
            return Arrays.copyOf(info.getPreSharedKey(), info.getPreSharedKey().length);
        }
    }

}
