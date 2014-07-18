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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory security store.
 * <p>
 * This implementation serializes the registry content into a file to be able to re-load the security infos when the
 * server is restarted.
 * </p>
 */
public class SecurityRegistry implements SecurityStore {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRegistry.class);

    // by client end-point
    private Map<String, SecurityInfo> securityByEp = new ConcurrentHashMap<>();

    // by PSK identity
    private Map<String, SecurityInfo> securityByIdentity = new ConcurrentHashMap<>();

    // the name of the file used to persist the registry content
    private final String filename;

    // default location for persistence
    private static final String DEFAULT_FILE = "data/security.data";

    public SecurityRegistry() {
        this(DEFAULT_FILE);
    }

    /**
     * @param file the file path to persist the registry
     */
    public SecurityRegistry(String file) {
        Validate.notEmpty(file);

        this.filename = file;
        this.loadFromFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityInfo get(String endpoint) {
        return securityByEp.get(endpoint);
    }

    /**
     * Returns the {@link SecurityInfo} for all end-points.
     * 
     * @return an unmodifiable collection of {@link SecurityInfo}
     */
    public Collection<SecurityInfo> getAll() {
        return Collections.unmodifiableCollection(securityByEp.values());
    }

    /**
     * Registers new security information for a client end-point.
     * 
     * @param info the new security information
     * @return the {@link SecurityInfo} previously stored for the end-point or <code>null</code> if there was no
     *         security information for the end-point.
     * @throws NonUniqueSecurityInfoException if some identifiers (PSK identity, RPK public key...) are not unique among
     *         all end-points.
     */
    public synchronized SecurityInfo add(SecurityInfo info) throws NonUniqueSecurityInfoException {

        SecurityInfo infoByIdentity = securityByIdentity.get(info.getIdentity());
        if (infoByIdentity != null && !info.getEndpoint().equals(infoByIdentity.getEndpoint())) {
            throw new NonUniqueSecurityInfoException("PSK Identity " + info.getIdentity() + " is already used");
        }

        SecurityInfo previous = securityByEp.put(info.getEndpoint(), info);
        if (previous != null) {
            securityByIdentity.remove(previous.getIdentity());
        }
        if (info.getIdentity() != null) {
            securityByIdentity.put(info.getIdentity(), info);
        }

        this.saveToFile();

        return previous;
    }

    /**
     * Removes the security information for a given end-point.
     * 
     * @param endpoint the client end-point
     * @return the removed {@link SecurityInfo} or <code>null</code> if no info for the end-point.
     */
    public synchronized SecurityInfo remove(String endpoint) {
        SecurityInfo info = securityByEp.get(endpoint);
        if (info != null) {
            if (info.getIdentity() != null) {
                securityByIdentity.remove(info.getIdentity());
            }
            securityByEp.remove(endpoint);

            this.saveToFile();
        }
        return info;
    }

    // /////// PSK store

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getPsk(String identity) {
        SecurityInfo info = securityByIdentity.get(identity);
        if (info == null || info.getPreSharedKey() == null) {
            return null;
        } else {
            // defensive copy
            return Arrays.copyOf(info.getPreSharedKey(), info.getPreSharedKey().length);
        }
    }

    // /////// File persistence

    private void loadFromFile() {

        FileInputStream fileIn = null;
        ObjectInputStream in = null;

        try {
            File file = new File(filename);

            if (!file.exists()) {
                // create parents if needed
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                file.createNewFile();

            } else {

                fileIn = new FileInputStream(file);
                in = new ObjectInputStream(fileIn);

                SecurityInfo[] infos = (SecurityInfo[]) in.readObject();

                for (SecurityInfo info : infos) {
                    try {
                        this.add(info);
                    } catch (NonUniqueSecurityInfoException e) {
                        // ignore it (should not occur)
                    }
                }

                if (infos != null && infos.length > 0) {
                    LOG.info("{} security infos loaded", infos.length);
                }
            }
        } catch (FileNotFoundException e) {
            // fine
        } catch (Exception e) {
            LOG.debug("Could not load security infos from file", e);
        } finally {
            IOUtils.closeQuietly(fileIn);
            IOUtils.closeQuietly(in);
        }
    }

    private void saveToFile() {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;

        try {
            fileOut = new FileOutputStream(filename);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(this.getAll().toArray(new SecurityInfo[0]));

        } catch (Exception e) {
            LOG.debug("Could not save security infos to file", e);
        } finally {
            IOUtils.closeQuietly(fileOut);
            IOUtils.closeQuietly(out);
        }
    }

}
