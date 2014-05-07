package leshan.server.lwm2m.message.californium;

import java.io.IOException;

import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ResourceObserver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.Request;

public class InMemoryObservationRegistryTest extends BasicTestSupport {

    InMemoryObservationRegistry registry;

    @Before
    public void setUp() throws Exception {
        this.registry = new InMemoryObservationRegistry();
    }

    @Test
    public void testAddObservationHandlesDuplicateRegistration() throws IOException {
        givenASimpleClient();
        ResourceObserver observer = new ResourceObserver() {

            @Override
            public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
                // do nothing
            }
        };

        Observation obs = new CaliforniumBasedObservation(Request.newGet(), observer, new ResourceSpec(this.client, 3,
                0, 15));
        String id = this.registry.addObservation(obs);

        Observation duplicate = new CaliforniumBasedObservation(Request.newGet(), observer, new ResourceSpec(this.client, 3,
                0, 15));

        Assert.assertEquals(id, this.registry.addObservation(duplicate));
        Assert.assertSame(1, this.registry.cancelObservations(this.client));
    }

    @Test
    public void testCreateDigestConsidersResourceObserver() throws IOException {
        givenASimpleClient();
        ResourceObserver observer1 = new ResourceObserver() {

            @Override
            public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
                // do nothing
            }
        };

        ResourceObserver observer2 = new ResourceObserver() {

            @Override
            public void notify(byte[] content, ContentFormat contentFormat, ResourceSpec target) {
                // do nothing
            }
        };

        Observation observation1 = new CaliforniumBasedObservation(Request.newGet(), observer1, new ResourceSpec(
                this.client, 3, 0, 15));
        String digest1 = this.registry.createDigest(observation1);

        Observation observation2 = new CaliforniumBasedObservation(Request.newGet(), observer2, new ResourceSpec(
                this.client, 3, 0, 15));
        String digest2 = this.registry.createDigest(observation2);

        Assert.assertNotEquals(digest1, digest2);
    }
}
