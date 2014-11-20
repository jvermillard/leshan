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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;

/**
 * Tests to check the running OSGi container, deployed bundles and loading
 * Required classes.
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SetupTest extends TestSetupConfig {

    @Test
    public void checkBundleContext() {
        assertNotNull(context);
    }

    @Test
    public void checkEventAdmin() {
        assertNotNull(eventAdmin);
    }

    @Test
    public void checkLwm2mBundles() {

        boolean leshancoreFound = false;
        boolean leshancoreActive = false;

        boolean leshanosgiFound = false;
        boolean leshanosgiActive = false;

        boolean leshanservercoreF = false;
        boolean leshanservercoreA = false;

        boolean leshanservercfF = false;
        boolean leshanservercfA = false;

        final Bundle[] bundles = context.getBundles();

        for (final Bundle bundle : bundles) {
            if (bundle != null && bundle.getSymbolicName() != null) {
                if (bundle.getSymbolicName().equals("leshan-core")) {
                    leshancoreFound = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshancoreActive = true;
                    }
                }
                if (bundle.getSymbolicName().equals("leshan-osgi")) {
                    leshanosgiFound = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshanosgiActive = true;
                    }
                }
                if (bundle.getSymbolicName().equals("leshan-server-core")) {
                    leshanservercoreF = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshanservercoreA = true;
                    }
                }
                if (bundle.getSymbolicName().equals("leshan-server-cf")) {
                    leshanservercfF = true;
                    if (bundle.getState() == Bundle.ACTIVE) {
                        leshanservercfA = true;
                    }
                }
            }
        }

        assertTrue(leshancoreFound);
        assertTrue(leshancoreActive);
        assertTrue(leshanosgiFound);
        assertTrue(leshanosgiActive);
        assertTrue(leshanservercoreF);
        assertTrue(leshanservercoreA);
        assertTrue(leshanservercfF);
        assertTrue(leshanservercfA);
    }

    @Test
    public void testLeshanClassesCanBeLoaded() {

        final String[] leshanClasses = new String[] { "leshan.server.LwM2mServer",
                "leshan.server.bootstrap.SecurityMode", "leshan.server.client.Client",
                "leshan.server.impl.ClientRegistryImpl", "leshan.server.californium.impl.RegisterResource",
                "leshan.server.californium.impl.SecureEndpoint", "leshan.core.node.codec.LwM2mNodeEncoder",
                "leshan.core.objectspec.ResourceSpec", "leshan.tlv.Tlv", "leshan.core.node.LwM2mNode",
                "leshan.server.observation.Observation", "leshan.server.request.LwM2mRequest",
                "leshan.server.security.SecurityInfo" };

        for (final String clazzName : leshanClasses) {
            try {
                context.getBundle().loadClass(clazzName);
            } catch (final ClassNotFoundException e) {
                Assert.fail(String.format("Should have been able to load class %s", clazzName));
            }
        }
    }
}
