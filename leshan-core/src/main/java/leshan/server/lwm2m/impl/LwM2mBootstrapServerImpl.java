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
package leshan.server.lwm2m.impl;

import leshan.server.lwm2m.bootstrap.BootstrapStore;
import leshan.server.lwm2m.bootstrap.LwM2mBootstrapServer;
import leshan.server.lwm2m.impl.bridge.bootstrap.BootstrapServerImplementor;
import leshan.server.lwm2m.security.SecurityStore;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Lightweight M2M server, serving bootstrap information on /bs.
 */
public class LwM2mBootstrapServerImpl implements LwM2mBootstrapServer {

    private final static Logger LOG = LoggerFactory.getLogger(LwM2mBootstrapServerImpl.class);

	private final BootstrapServerImplementor implementor;

    public LwM2mBootstrapServerImpl(BootstrapServerImplementor implementor) {
        Validate.notNull(implementor, "bootstrap store must not be null");
        this.implementor = implementor;

    }

    @Override
    public BootstrapStore getBoostrapStore() {
        return implementor.getBootstrapStore();
    }

    @Override
    public SecurityStore getSecurityStore() {
        return implementor.getSecurityStore();
    }

    /**
     * Starts the server and binds it to the specified port.
     */
    public void start() {
        implementor.start();
        LOG.info("LW-M2M server started");
    }

    /**
     * Stops the server and unbinds it from assigned ports (can be restarted).
     */
    public void stop() {
    	implementor.stop();
    }

    /**
     * Stops the server and unbinds it from assigned ports.
     */
    public void destroy() {
    	implementor.destroy();
    }
}