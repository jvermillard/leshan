package leshan.server.lwm2m.message.californium;

import java.io.IOException;

import leshan.server.lwm2m.message.ContentFormat;
import leshan.server.lwm2m.message.ResourceSpec;
import leshan.server.lwm2m.observation.Observation;
import leshan.server.lwm2m.observation.ObservationRegistryImpl;
import leshan.server.lwm2m.observation.ResourceObserver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.inf.vs.californium.coap.Request;

public class ObservationRegistryImplTest extends BasicTestSupport {

    ObservationRegistryImpl registry;

    @Before
    public void setUp() throws Exception {
        this.registry = new ObservationRegistryImpl();
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
        registry.addObservation(obs);

        Observation duplicate = new CaliforniumBasedObservation(Request.newGet(), observer, new ResourceSpec(
                this.client, 3, 0, 15));

        registry.addObservation(duplicate);
        Assert.assertSame(1, this.registry.cancelObservations(this.client));
    }
}
