package leshan.client.lwm2m.operation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import leshan.client.lwm2m.exchange.LwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.AggregatedLwM2mExchange;
import leshan.client.lwm2m.exchange.aggregate.LwM2mObjectInstanceReadResponseAggregator;
import leshan.client.lwm2m.exchange.aggregate.LwM2mResponseAggregator;
import leshan.client.lwm2m.response.LwM2mResponse;
import leshan.client.lwm2m.response.ReadResponse;
import leshan.server.lwm2m.impl.tlv.Tlv;
import leshan.server.lwm2m.impl.tlv.Tlv.TlvType;
import leshan.server.lwm2m.impl.tlv.TlvEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadResourceLwM2mResponseAggregatorTest {

	@Mock
	private LwM2mExchange coapExchange;

	@Test
	public void testSingleSuccessfulRead() {
		final int numExpectedResults = 1;
		final int resourceId = 45;
		final byte[] resourceValue = "value".getBytes();

		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange, numExpectedResults);
		final LwM2mExchange exchange = new AggregatedLwM2mExchange(aggr, resourceId);

		exchange.respond(ReadResponse.success(resourceValue));

		final Tlv[] tlvs = new Tlv[numExpectedResults];
		tlvs[0] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue, resourceId);
		verify(coapExchange).respond(ReadResponse.success(TlvEncoder.encode(tlvs).array()));
	}

	@Test
	public void testMultipleSuccessfulReads() {
		final int numExpectedResults = 2;
		final int resourceId1 = 45;
		final int resourceId2 = 78;
		final byte[] resourceValue1 = "hello".getBytes();
		final byte[] resourceValue2 = "world".getBytes();

		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange, numExpectedResults);
		final LwM2mExchange exchange1 = new AggregatedLwM2mExchange(aggr, resourceId1);
		final LwM2mExchange exchange2 = new AggregatedLwM2mExchange(aggr, resourceId2);

		exchange1.respond(ReadResponse.success(resourceValue1));
		exchange2.respond(ReadResponse.success(resourceValue2));

		final Tlv[] tlvs = new Tlv[numExpectedResults];
		tlvs[0] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue1, resourceId1);
		tlvs[1] = new Tlv(TlvType.RESOURCE_VALUE, null, resourceValue2, resourceId2);
		verify(coapExchange).respond(ReadResponse.success(TlvEncoder.encode(tlvs).array()));
	}

	@Test
	public void testIncompleteReadDoesNotSend() {
		final int numExpectedResults = 2;
		final int resourceId = 45;
		final byte[] resourceValue = "hello".getBytes();

		final LwM2mResponseAggregator aggr = new LwM2mObjectInstanceReadResponseAggregator(coapExchange, numExpectedResults);
		final LwM2mExchange exchange = new AggregatedLwM2mExchange(aggr, resourceId);

		exchange.respond(ReadResponse.success(resourceValue));

		verify(coapExchange, never()).respond(any(LwM2mResponse.class));
	}

}
