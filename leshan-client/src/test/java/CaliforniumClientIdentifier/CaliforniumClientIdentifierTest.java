package CaliforniumClientIdentifier;

import static org.junit.Assert.*;
import leshan.client.californium.impl.CaliforniumClientIdentifier;

import org.junit.Test;

public class CaliforniumClientIdentifierTest {

    @Test
    public void two_instances_are_equal() {
        String oneLocation = "/rd/something";
        String oneEndpoint = "dfasdfs";
        CaliforniumClientIdentifier idOne = new CaliforniumClientIdentifier(oneLocation, oneEndpoint);
        CaliforniumClientIdentifier idTwo = new CaliforniumClientIdentifier(oneLocation, oneEndpoint);

        assertEquals(idOne, idTwo);
        assertEquals(idOne.hashCode(), idTwo.hashCode());
    }

}
