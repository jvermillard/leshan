package leshan.client.lwm2m.resource;

import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CONTENT;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.CREATED;
import static ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode.METHOD_NOT_ALLOWED;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import leshan.client.lwm2m.operation.AggregatedLwM2mExchange;
import leshan.client.lwm2m.operation.CaliforniumBasedLwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mExchange;
import leshan.client.lwm2m.operation.LwM2mObjectReadResponseAggregator;
import leshan.client.lwm2m.operation.LwM2mResponseAggregator;
import leshan.client.lwm2m.operation.ReadResponse;
import leshan.server.lwm2m.tlv.Tlv;
import leshan.server.lwm2m.tlv.TlvDecoder;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ClientObject extends ResourceBase implements LinkFormattable{

	private final Map<Integer, LwM2mResourceDefinition> definitionMap;
	private final AtomicInteger instanceCounter;

	public ClientObject(final int objectId, final LwM2mResourceDefinition... definitions) {
		super(Integer.toString(objectId));
		if (definitions == null || definitions.length == 0) {
			throw new IllegalArgumentException("Must provide at least one resource definition");
		}
		definitionMap = new HashMap<>();
		for (final LwM2mResourceDefinition def : definitions) {
			definitionMap.put(def.getId(), def);
		}
		instanceCounter = new AtomicInteger(0);
	}

	@Override
	public void handleGET(final CoapExchange exchange) {
		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT){
			handleDiscover(exchange);
		}
		else{
			handleRead(exchange);
		}
	}

	private void handleDiscover(final CoapExchange exchange) {
		exchange.respond(CONTENT, asLinkFormat());
	}


	private void handleRead(final CoapExchange exchange) {
		handleNormalRead(new CaliforniumBasedLwM2mExchange(exchange));
	}

	private void handleNormalRead(final LwM2mExchange exchange) {
		if (getChildren().isEmpty()) {
			exchange.respond(ReadResponse.success(new byte[0]));
			return;
		}

		final LwM2mResponseAggregator aggr = new LwM2mObjectReadResponseAggregator(
				exchange,
				getChildren().size());
		for (final Resource child : getChildren()) {
			final ClientObjectInstance res = (ClientObjectInstance)child;
			res.handleNormalRead(new AggregatedLwM2mExchange(aggr, res.getId()));
		}
	}

	@Override
	public void handlePOST(final CoapExchange exchange) {
		final int instanceId = getNewInstanceId(exchange);

		final Map<Integer, ClientResource> resources = new HashMap<>();

		final Tlv[] tlvs = TlvDecoder.decode(ByteBuffer.wrap(exchange.getRequestPayload()));
		for (final Tlv tlv : tlvs) {
			final int resourceId = tlv.getIdentifier();
			final LwM2mResourceDefinition def = definitionMap.get(resourceId);
			if (def == null) {
				exchange.respond(METHOD_NOT_ALLOWED);
				return;
			} else {
				final ClientResource res = new ClientResource(resourceId, def.createResource(tlv.getValue()));
				resources.put(resourceId, res);
			}
		}

		final ClientObjectInstance instance = new ClientObjectInstance(instanceId, resources);
		add(instance);

		exchange.setLocationPath(getName() + "/" + instanceId);
		exchange.respond(CREATED);
	}

	private int getNewInstanceId(final CoapExchange exchange) {
		if (exchange.advanced().getRequest().getOptions().getURIPaths().size() > 1) {
			return Integer.parseInt(exchange.advanced().getRequest().getOptions().getURIPaths().get(1));
		}
		return instanceCounter.getAndIncrement();
	}

	@Override
	public String asLinkFormat() {
		final StringBuilder linkFormat = LinkFormat.serializeResource(this).append(LinkFormat.serializeAttributes(getAttributes()));
		for(final Resource child : getChildren()){
			for(final Resource grandchild : child.getChildren()){
				linkFormat.append(LinkFormat.serializeResource(grandchild));
			}
		}
		linkFormat.deleteCharAt(linkFormat.length() - 1);
		return linkFormat.toString();
	}
}
