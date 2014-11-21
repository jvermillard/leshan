/*
 * Copyright (c) 2014, Bosch Software Innovations GmbH
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
package leshan.osgi.test;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;

import java.net.InetAddress;

import javax.inject.Inject;

import leshan.osgi.OsgiBasedClientRegistry;
import leshan.server.request.LwM2mRequestSender;

import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

/**
 * The Setup of running OSGi Container and deploying required bundles. Inject
 * BundleContext, EventAdmin. Initializing global OsgiBasedClientRegistry.
 *
 */
public class TestSetupConfig {

    OsgiBasedClientRegistry osgiRegistry;
    LwM2mRequestSender requestSender;
    InetAddress address;

    @Inject
    BundleContext context;

    @Inject
    EventAdmin eventAdmin;

    @Configuration
    public Option[] config() {
        final Option[] options = new Option[] { systemPackage("com.sun.org.apache.xerces.internal.impl.dv.util"),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-service").versionAsInProject(),
                mavenBundle("commons-codec", "commons-codec").versionAsInProject(),
                mavenBundle("commons-io", "commons-io").versionAsInProject(),
                mavenBundle("commons-lang", "commons-lang").versionAsInProject(),
                mavenBundle("com.google.code.gson", "gson").versionAsInProject(),
                mavenBundle("org.osgi", "org.osgi.compendium").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin").versionAsInProject(),
                mavenBundle("org.github.leshan", "leshan-core").versionAsInProject(),
                mavenBundle("org.github.leshan", "leshan-osgi").versionAsInProject(),
                mavenBundle("org.github.leshan", "leshan-server-core").versionAsInProject(),
                mavenBundle("org.github.leshan", "leshan-server-cf").versionAsInProject(),
                mavenBundle("org.eclipse.californium", "californium-osgi").versionAsInProject(),
                mavenBundle("org.eclipse.californium", "scandium").versionAsInProject(), junitBundles() };

        return options;
    }

    @Before
    public void setUp() throws Exception {
        osgiRegistry = new OsgiBasedClientRegistry(context, null);
        address = InetAddress.getLocalHost();
    }

}
