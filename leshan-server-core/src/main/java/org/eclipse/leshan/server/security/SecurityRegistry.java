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
package org.eclipse.leshan.server.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;

public interface SecurityRegistry extends SecurityStore {

    /**
     * Returns the {@link SecurityInfo} for all end-points.
     * 
     * @return an unmodifiable collection of {@link SecurityInfo}
     */
    Collection<SecurityInfo> getAll();

    /**
     * Registers new security information for a client end-point.
     * 
     * @param info the new security information
     * @return the {@link SecurityInfo} previously stored for the end-point or <code>null</code> if there was no
     *         security information for the end-point.
     * @throws NonUniqueSecurityInfoException if some identifiers (PSK identity, RPK public key...) are not unique among
     *         all end-points.
     */
    SecurityInfo add(SecurityInfo info) throws NonUniqueSecurityInfoException;

    /**
     * Removes the security information for a given end-point.
     * 
     * @param endpoint the client end-point
     * @return the removed {@link SecurityInfo} or <code>null</code> if no info for the end-point.
     */
    SecurityInfo remove(String endpoint);

    /**
     * Returns the Server Public Key
     */
    PublicKey getServerPublicKey();

    /**
     * Returns the Server Private Key
     */
    PrivateKey getServerPrivateKey();
}
