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
package leshan.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leshan.server.lwm2m.bootstrap.BootstrapConfig;
import leshan.server.lwm2m.bootstrap.BootstrapStore;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple bootstrap store implementation storing bootstrap information in memory
 */
public class BootstrapStoreImpl implements BootstrapStore {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapStoreImpl.class);

    // default location for persistence
    private static final String DEFAULT_FILE = "data/bootstrap.data";

    private final String filename;

    public BootstrapStoreImpl() {
        this(DEFAULT_FILE);
    }

    /**
     * @param file the file path to persist the registry
     */
    public BootstrapStoreImpl(String filename) {
        Validate.notEmpty(filename);

        this.filename = filename;
        this.loadFromFile();
    }

    private Map<String, BootstrapConfig> bootstrapByEndpoint = new ConcurrentHashMap<>();

    @Override
    public BootstrapConfig getBootstrap(String endpoint) {
        return bootstrapByEndpoint.get(endpoint);
    }

    public void addConfig(String endpoint, BootstrapConfig config) {
        bootstrapByEndpoint.put(endpoint, config);
        saveToFile();
    }

    public Map<String, BootstrapConfig> getBootstrapConfigs() {
        return Collections.unmodifiableMap(bootstrapByEndpoint);
    }

    public boolean deleteConfig(String enpoint) {
        BootstrapConfig res = bootstrapByEndpoint.remove(enpoint);
        saveToFile();
        return res != null;
    }

    // /////// File persistence

    @SuppressWarnings("unchecked")
    private void loadFromFile() {

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

                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    bootstrapByEndpoint.putAll((Map<String, BootstrapConfig>) in.readObject());
                }
            }
        } catch (FileNotFoundException e) {
            // fine
        } catch (Exception e) {
            LOG.debug("Could not load bootstrap infos from file", e);
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            Map<String, BootstrapConfig> copy = new HashMap<>(bootstrapByEndpoint);
            out.writeObject(copy);
        } catch (Exception e) {
            LOG.debug("Could not save bootstrap infos to file", e);
        }
    }
}
