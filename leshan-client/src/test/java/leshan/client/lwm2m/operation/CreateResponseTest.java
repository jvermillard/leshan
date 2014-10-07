package leshan.client.lwm2m.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import leshan.client.lwm2m.response.CreateResponse;

import org.junit.Test;

public class CreateResponseTest {

	@Test
	public void testEqualityRobustnessForSuccesses() {
		assertEquals(CreateResponse.success(1), CreateResponse.success(1));
		assertNotEquals(CreateResponse.success(1), CreateResponse.success(2));
		assertNotEquals(CreateResponse.success(2), CreateResponse.success(1));
		assertNotEquals(CreateResponse.success(1), CreateResponse.invalidResource());
		assertNotEquals(CreateResponse.success(1), null);
	}

	@Test
	public void testHashCodeRobustnessForSuccesses() {
		assertEquals(CreateResponse.success(1).hashCode(), CreateResponse.success(1).hashCode());
		assertNotEquals(CreateResponse.success(1).hashCode(), CreateResponse.success(2).hashCode());
		assertNotEquals(CreateResponse.success(1).hashCode(), CreateResponse.invalidResource().hashCode());
	}

	@Test
	public void testEqualityRobustnessForFailures() {
		assertEquals(CreateResponse.invalidResource(), CreateResponse.invalidResource());
		assertNotEquals(CreateResponse.invalidResource(), CreateResponse.success(2));
		assertNotEquals(CreateResponse.invalidResource(), null);
	}

	@Test
	public void testHashCodeRobustnessForFailures() {
		assertEquals(CreateResponse.invalidResource().hashCode(), CreateResponse.invalidResource().hashCode());
		assertNotEquals(CreateResponse.invalidResource(), CreateResponse.success(2).hashCode());
	}

}
