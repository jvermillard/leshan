package leshan.client.lwm2m.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ReadResponseTest {

	@Test
	public void testEqualityRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("hello".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.success("hello".getBytes()), ReadResponse.failure());
		assertNotEquals(ReadResponse.success("hello".getBytes()), null);
	}

	@Test
	public void testHashCodeRobustnessForSuccesses() {
		assertEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("hello".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.success("goodbye".getBytes()).hashCode());
		assertNotEquals(ReadResponse.success("hello".getBytes()).hashCode(), ReadResponse.failure().hashCode());
	}

	@Test
	public void testEqualityRobustnessForFailures() {
		assertEquals(ReadResponse.failure(), ReadResponse.failure());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()));
		assertNotEquals(ReadResponse.failure(), null);
	}

	@Test
	public void testHashCodeRobustnessForFailures() {
		assertEquals(ReadResponse.failure().hashCode(), ReadResponse.failure().hashCode());
		assertNotEquals(ReadResponse.failure(), ReadResponse.success("goodbye".getBytes()).hashCode());
	}

}
